package com.firefly.ui.prefs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.firefly.utils.TurnipUtils;

import java.util.Arrays;
import java.util.List;

public class ChooseTurnipListPref extends ListPreference {

    private static final int FILE_SELECT_CODE = 100;
    private List<String> defaultLibs;
    private OnPreferenceChangeListener preferenceChangeListener;

    public ChooseTurnipListPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadDefaultLibs(context);
    }

    private void loadDefaultLibs(Context context) {
        defaultLibs = Arrays.asList(context.getResources().getStringArray(R.array.turnip_values));
    }

    @Override
    protected void onClick() {
        String initialValue = getValue();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getDialogTitle());

        CharSequence[] entriesCharSequence = getEntries();
        String[] entries = new String[entriesCharSequence.length];
        for (int i = 0; i < entriesCharSequence.length; i++) {
            entries[i] = entriesCharSequence[i].toString();
        }

        builder.setItems(entries, (dialog, which) -> {
            String newValue = getEntryValues()[which].toString();
            if (!newValue.equals(initialValue)) {
                if (preferenceChangeListener != null) {
                    if (preferenceChangeListener.onPreferenceChange(this, newValue)) {
                        setValue(newValue);
                    }
                } else {
                    setValue(newValue);
                }
            }
            dialog.dismiss();
        });
        builder.setPositiveButton("新建", (d, i) -> selectTurnipDriverFile());

        AlertDialog dialog = builder.create();
        dialog.show();

        ListView listView = dialog.getListView();
        listView.setOnItemLongClickListener((adapterView, view, position, id) -> {
            String selectedVersion = getEntryValues()[position].toString();
            if (defaultLibs.contains(selectedVersion)) {
                Toast.makeText(getContext(), R.string.preference_rendererexp_mesa_delete_defaultlib, Toast.LENGTH_SHORT).show();
            } else {
                showDeleteConfirmationDialog(selectedVersion);
            }
            dialog.dismiss();
            return true;
        });
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
        this.preferenceChangeListener = listener;
        super.setOnPreferenceChangeListener(listener);
    }

    private void showDeleteConfirmationDialog(String version) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.preference_rendererexp_mesa_delete_title)
                .setMessage(getContext().getString(R.string.preference_rendererexp_mesa_delete_message, version))
                .setPositiveButton(R.string.alertdialog_done, (dialog, which) -> {
                    boolean success = TurnipUtils.INSTANCE.deleteTurnipDriver(version);
                    if (success) {
                        Toast.makeText(getContext(), R.string.preference_rendererexp_mesa_deleted, Toast.LENGTH_SHORT).show();
                        setEntriesAndValues();
                    } else {
                        Toast.makeText(getContext(), R.string.preference_rendererexp_mesa_delete_fail, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.alertdialog_cancel, null)
                .show();
    }

    private void setEntriesAndValues() {
        Tools.IListAndArry array = Tools.getCompatibleCTurnipDriver(getContext());
        setEntries(array.getArray());
        setEntryValues(array.getList().toArray(new String[0]));
        String currentValue = getValue();
        if (!array.getList().contains(currentValue)) {
            setValueIndex(0);
        }
    }

    private void selectTurnipDriverFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        ((Activity) getContext()).startActivityForResult(Intent.createChooser(intent, "Select .so file"), FILE_SELECT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                showFolderNameDialog(fileUri);
            }
        }
    }

    private void showFolderNameDialog(Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Folder Name");

        EditText input = new EditText(getContext());
        input.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '.') {
                    return "";
                }
            }
            return null;
        }});
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                boolean success = TurnipUtils.INSTANCE.saveTurnipDriver(getContext(), fileUri, folderName);
                Toast.makeText(getContext(), success ? "Driver saved successfully" : "Failed to save driver", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

}
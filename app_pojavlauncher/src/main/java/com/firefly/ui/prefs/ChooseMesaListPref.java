package com.firefly.ui.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.MesaUtils;

import java.util.Arrays;
import java.util.List;

public class ChooseMesaListPref extends ListPreference {

    private List<String> defaultLibs;
    private OnPreferenceChangeListener preferenceChangeListener;
    private View.OnClickListener importClickListener;
    private View.OnClickListener downloadClickListener;

    public ChooseMesaListPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadDefaultLibs(context);
    }

    private void loadDefaultLibs(Context context) {
        defaultLibs = Arrays.asList(context.getResources().getStringArray(R.array.osmesa_values));
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
                if (getOnPreferenceChangeListener() != null) {
                    if (getOnPreferenceChangeListener().onPreferenceChange(this, newValue)) {
                        setValue(newValue);
                    }
                } else {
                    setValue(newValue);
                }
            }
            dialog.dismiss();
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(50, 20, 50, 20);
        buttonLayout.setGravity(android.view.Gravity.CENTER);

        Button importButton = new Button(getContext());
        importButton.setText(R.string.pgw_settings_custom_turnip_creat);
        importButton.setOnClickListener(v -> {
            if (importClickListener != null) {
                importClickListener.onClick(v);
            }
            dialog.dismiss();
        });

        Button downloadButton = new Button(getContext());
        downloadButton.setText(R.string.preference_extra_mesa_download);
        downloadButton.setOnClickListener(v -> {
            if (downloadClickListener != null) {
                downloadClickListener.onClick(v);
            }
            dialog.dismiss();
        });

        buttonLayout.addView(importButton);
        buttonLayout.addView(downloadButton);

        dialog.getWindow().addContentView(buttonLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
        this.preferenceChangeListener = listener;
        super.setOnPreferenceChangeListener(listener);
    }

    public void setImportButton(View.OnClickListener listener) {
        this.importClickListener = listener;
    }

    public void setDownloadButton(View.OnClickListener listener) {
        this.downloadClickListener = listener;
    }

    private void showDeleteConfirmationDialog(String version) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.preference_rendererexp_mesa_delete_title)
                .setMessage(getContext().getString(R.string.preference_rendererexp_mesa_delete_message, version))
                .setPositiveButton(R.string.alertdialog_done, (dialog, which) -> {
                    boolean success = MesaUtils.INSTANCE.deleteMesaLib(version);
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
        Tools.IListAndArry array = Tools.getCompatibleCMesaLib(getContext());
        setEntries(array.getArray());
        setEntryValues(array.getList().toArray(new String[0]));
        String currentValue = getValue();
        if (!array.getList().contains(currentValue)) {
            setValueIndex(0);
        }
    }
}
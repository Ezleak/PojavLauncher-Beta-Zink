package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.Architecture.is32BitsDevice;
import static net.kdt.pojavlaunch.Tools.getTotalDeviceMemory;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.EditTextPreference;

import com.firefly.ui.dialog.CustomDialog;
import com.movtery.utils.MemoryUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.util.Timer;
import java.util.TimerTask;

public class LauncherPreferenceJavaFragment extends LauncherPreferenceFragment {
    private EditText mSetJavaMemory;
    private final Timer timer = new Timer();
    private MultiRTConfigDialog mDialogScreen;
    private final ActivityResultLauncher<Object> mVmInstallLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("xz"), (data) -> {
                if (data != null) Tools.installRuntimeFromUri(getContext(), data);
            });

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_java);
        int ramAllocation = LauncherPreferences.PREF_RAM_ALLOCATION;

        CustomSeekBarPreference seek7 = requirePreference("allocation",
                CustomSeekBarPreference.class);

        int maxRAM;
        int deviceRam = getTotalDeviceMemory(seek7.getContext());

        if (is32BitsDevice() || deviceRam < 2048) maxRAM = Math.min(1024, deviceRam);
        else maxRAM = deviceRam - (deviceRam < 3064 ? 800 : 1024);

        seek7.setRange(256, maxRAM);
        seek7.setValue(ramAllocation);
        seek7.setSuffix(" MB");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!seek7.isUserSeeking()) {
                        updateMemoryInfo(requireContext(), seek7);
                    }
                } catch (Exception e) {
                    Log.e("updateMemoryInfo", e.toString());
                }
            }
        }, 0, 1000);

        seek7.setOnPreferenceClickListener(preference -> {
            setMemoryAllocationDialog(seek7, maxRAM);
            return true;
        });
        updateMemoryInfo(requireContext(), seek7);

        EditTextPreference editJVMArgs = findPreference("javaArgs");
        if (editJVMArgs != null) {
            editJVMArgs.setOnBindEditTextListener(TextView::setSingleLine);
        }

        requirePreference("install_jre").setOnPreferenceClickListener(preference -> {
            openMultiRTDialog();
            return true;
        });
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void updateMemoryInfo(Context context, CustomSeekBarPreference seek) {
        long seekValue = (long) seek.getValue() * 1024 * 1024;
        long freeDeviceMemory = MemoryUtils.getFreeDeviceMemory(context);

        boolean isMemorySizeExceeded = seekValue > freeDeviceMemory;

        String summary = getString(R.string.zh_setting_java_memory_desc);
        summary += "\r\n" + getMemoryInfoText(context, freeDeviceMemory);
        if (isMemorySizeExceeded)
            summary += "\r\n" + getString(R.string.zh_setting_java_memory_exceeded);

        String finalSummary = summary;
        runOnUiThread(() -> seek.setSummary(finalSummary));
    }

    private String getMemoryInfoText(Context context) {
        return getMemoryInfoText(context, MemoryUtils.getFreeDeviceMemory(context));
    }

    private String getMemoryInfoText(Context context, long freeDeviceMemory) {
        return getString(
                R.string.zh_setting_java_memory_info,
                Tools.formatFileSize(MemoryUtils.getUsedDeviceMemory(context)),
                Tools.formatFileSize(MemoryUtils.getTotalDeviceMemory(context)),
                Tools.formatFileSize(freeDeviceMemory)
        );
    }

    private void openMultiRTDialog() {
        if (mDialogScreen == null) {
            mDialogScreen = new MultiRTConfigDialog();
            mDialogScreen.prepare(getContext(), mVmInstallLauncher);
        }
        mDialogScreen.show();
    }

    private void setMemoryAllocationDialog(CustomSeekBarPreference seek, int maxRAM) {
        mSetJavaMemory = new EditText(requireContext());
        mSetJavaMemory.setMaxLines(1);
        mSetJavaMemory.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSetJavaMemory.setText(String.valueOf(seek.getValue()));
        new CustomDialog.Builder(requireContext())
                .setTitle(getString(R.string.mcl_memory_allocation))
                .setMessage(getMemoryInfoText(requireContext()) + "\r\n" + getString(R.string.zh_setting_java_memory_max, String.format("%s MB", maxRAM)))
                .setCustomView(mSetJavaMemory)
                .setConfirmListener(R.string.alertdialog_done, customView -> {
                    String checkValue = mSetJavaMemory.getText().toString();
                    if (checkValue.isEmpty()) {
                        mSetJavaMemory.setError(getString(R.string.global_error_field_empty));
                        return false;
                    }
                    int Memory;
                    try {
                        Memory = Integer.parseInt(checkValue);
                    } catch (NumberFormatException e) {
                        Log.e("allocationSeek", e.toString());
                        // mSetJavaMemory.setError(e.toString());
                        mSetJavaMemory.setError(requireContext().getString(R.string.setting_java_memory_outofrange, checkValue));
                        return false;
                    }
                    if (Memory < 256 || Memory > maxRAM) {
                        if (Memory < 256) {
                            mSetJavaMemory.setError(requireContext().getString(R.string.zh_setting_java_memory_too_small, 256));
                        }
                        if (Memory > maxRAM) {
                            mSetJavaMemory.setError(requireContext().getString(R.string.zh_setting_java_memory_too_big, maxRAM));
                        }
                        return false;
                    }
                    seek.setValue(Memory);
                    return true;
                })
                .setCancelListener(R.string.alertdialog_cancel, customView -> true)
                .setDraggable(true)
                .build()
                .show();
    }

}

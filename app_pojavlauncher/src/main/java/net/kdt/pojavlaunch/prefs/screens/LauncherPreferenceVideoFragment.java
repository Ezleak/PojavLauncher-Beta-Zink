package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_NOTCH_SIZE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.firefly.utils.PGWTools;
import com.firefly.utils.TurnipUtils;
import com.firefly.ui.dialog.CustomDialog;
import com.firefly.ui.prefs.ChooseMesaListPref;
import com.firefly.ui.prefs.ChooseTurnipListPref;

import android.content.SharedPreferences;
import android.os.Build;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.MesaUtils;

import java.util.Set;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends LauncherPreferenceFragment {

    private static final int FILE_SELECT_CODE = 100;
    private EditText mSetVideoResolution;
    private EditText mMesaGLVersion;
    private EditText mMesaGLSLVersion;
    private String expRenderer;


    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);
        // Get values
        int scaleFactor = LauncherPreferences.PREF_SCALE_FACTOR;

        //Disable notch checking behavior on android 8.1 and below.
        requirePreference("ignoreNotch").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && PREF_NOTCH_SIZE > 0);

        CustomSeekBarPreference seek5 = requirePreference("resolutionRatio",
                CustomSeekBarPreference.class);

        if (scaleFactor > 100) {
            seek5.setRange(25, scaleFactor);
        } else {
            seek5.setRange(25, 100);
        }

        seek5.setValue(scaleFactor);
        seek5.setSuffix(" %");

        // #724 bug fix
        if (scaleFactor < 25) {
            seek5.setValue(100);
        }

        seek5.setOnPreferenceClickListener(preference -> {
            setVideoResolutionDialog(seek5);
            return true;
        });

        // Sustained performance is only available since Nougat
        SwitchPreference sustainedPerfSwitch = requirePreference("sustainedPerformance",
                SwitchPreference.class);
        sustainedPerfSwitch.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);

        SwitchPreference useSystemVulkan = requirePreference("zinkPreferSystemDriver", SwitchPreference.class);
        if (!Tools.checkVulkanSupport(useSystemVulkan.getContext().getPackageManager())) {
            useSystemVulkan.setVisible(false);
        }
        useSystemVulkan.setOnPreferenceChangeListener((p, v) -> {
            boolean set = (boolean) v;
            boolean isAdreno = PGWTools.isAdrenoGPU();
            if (set && isAdreno) {
                onCheckGPUDialog(p);
            } else {
                return true;
            }
            return false;
        });

        final Preference downloadMesa = requirePreference("DownloadMesa", Preference.class);
        downloadMesa.setOnPreferenceClickListener((a) -> {
            loadMesaList();
            return true;
        });

        final ListPreference rendererListPref = requirePreference("renderer", ListPreference.class);
        final ChooseMesaListPref CMesaLibP = requirePreference("CMesaLibrary", ChooseMesaListPref.class);
        final ChooseTurnipListPref CTurnipP = requirePreference("chooseTurnipDriver", ChooseTurnipListPref.class);
        final ListPreference CDriverModelP = requirePreference("CDriverModels", ListPreference.class);
        final ListPreference CMesaLDOP = requirePreference("ChooseMldo", ListPreference.class);

        setListPreference(rendererListPref, "renderer");
        setListPreference(CMesaLibP, "CMesaLibrary");
        setListPreference(CTurnipP, "chooseTurnipDriver");
        setListPreference(CDriverModelP, "CDriverModels");
        setListPreference(CMesaLDOP, "ChooseMldo");

        rendererListPref.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.LOCAL_RENDERER = (String) obj;
            return true;
        });

        CMesaLibP.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.MESA_LIBS = (String) obj;
            setListPreference(CDriverModelP, "CDriverModels");
            CDriverModelP.setValueIndex(0);
            return true;
        });

        CTurnipP.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.TURNIP_LIBS = (String) obj;
            return true;
        });
        CTurnipP.setConfirmButton(R.string.pgw_settings_custom_turnip_creat, view -> selectTurnipDriverFile());

        CDriverModelP.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.DRIVER_MODEL = (String) obj;
            return true;
        });

        CMesaLDOP.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.LOADER_OVERRIDE = (String) obj;
            return true;
        });

        SwitchPreference expRendererPref = requirePreference("ExperimentalSetup", SwitchPreference.class);
        expRendererPref.setOnPreferenceChangeListener((p, v) -> {
            boolean isExpRenderer = (boolean) v;
            if (isExpRenderer) {
                onExpRendererDialog(p, rendererListPref);
                return false;
            } else {
                ((SwitchPreference) p).setChecked(false);
                onChangeRenderer(rendererListPref);
                setListPreference(rendererListPref, "renderer");
                return true;
            }
        });

        // Custom GL/GLSL
        final PreferenceCategory customMesaVersionPref = requirePreference("customMesaVersionPref", PreferenceCategory.class);
        SwitchPreference setSystemVersion = requirePreference("ebSystem", SwitchPreference.class);
        setSystemVersion.setOnPreferenceChangeListener((p, v) -> {
            boolean set = (boolean) v;
            if (!set) return false;
            closeOtherCustomMesaPref(customMesaVersionPref);
            return true;
        });

        SwitchPreference setSpecificVersion = requirePreference("ebSpecific", SwitchPreference.class);
        setSpecificVersion.setOnPreferenceChangeListener((p, v) -> {
            boolean set = (boolean) v;
            if (!set) return false;
            closeOtherCustomMesaPref(customMesaVersionPref);
            return true;
        });

        SwitchPreference setGLVersion = requirePreference("ebCustom", SwitchPreference.class);
        setGLVersion.setOnPreferenceChangeListener((p, v) -> {
            boolean set = (boolean) v;
            if (!set) return false;
            closeOtherCustomMesaPref(customMesaVersionPref);
            return true;
        });
        setGLVersion.setOnPreferenceClickListener(preference -> {
            showSetGLVersionDialog();
            return true;
        });

        computeVisibility();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        computeVisibility();
    }

    private void computeVisibility() {
        requirePreference("force_vsync", SwitchPreferenceCompat.class).setVisible(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
        requirePreference("SpareFrameBuffer").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("MesaRendererChoose").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("customMesaVersionPref").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("customMesaLoaderDriverOverride").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("ChooseMldo").setVisible(LauncherPreferences.PREF_LOADER_OVERRIDE);
    }

    private void setVideoResolutionDialog(CustomSeekBarPreference seek) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_video_resolution, null);
        mSetVideoResolution = view.findViewById(R.id.set_resolution);
        mSetVideoResolution.setText(String.valueOf(seek.getValue()));
        new CustomDialog.Builder(requireContext())
                .setCustomView(view)
                .setConfirmListener(R.string.alertdialog_done, customView -> {
                    String checkValue = mSetVideoResolution.getText().toString();
                    if (checkValue.isEmpty()) {
                        mSetVideoResolution.setError(getString(R.string.global_error_field_empty));
                        return false;
                    }
                    int Value;
                    try {
                        Value = Integer.parseInt(checkValue);
                    } catch (NumberFormatException e) {
                        Log.e("VideoResolution", e.toString());
                        // mSetVideoResolution.setError(e.toString());
                        mSetVideoResolution.setError(requireContext().getString(R.string.setting_set_resolution_outofrange, checkValue));
                        return false;
                    }
                    if (Value < 25 || Value > 1000) {
                        if (Value < 25) {
                            mSetVideoResolution.setError(requireContext().getString(R.string.setting_set_resolution_too_small, 25));
                        }
                        if (Value > 1000) {
                            mSetVideoResolution.setError(requireContext().getString(R.string.setting_set_resolution_too_big, 1000));
                        }
                        return false;
                    }
                    if (Value > 100) {
                        seek.setRange(25, Value);
                    } else {
                        seek.setRange(25, 100);
                    }
                    seek.setValue(Value);
                    return true;
                })
                .setCancelListener(R.string.alertdialog_cancel, customView -> true)
                .setDraggable(true)
                .build()
                .show();
    }

    private void setListPreference(ListPreference listPreference, String preferenceKey) {
        Tools.IListAndArry array = null;
        String value = listPreference.getValue();
        if (preferenceKey.equals("CMesaLibrary")) {
            array = Tools.getCompatibleCMesaLib(getContext());
            boolean have = false;
            for (int a = 0; a < array.getList().size(); a++) {
                if (array.getList().get(a).equalsIgnoreCase(value)) {
                    have = true;
                    break;
                }
            }
            if (!have) {
                value = array.getList().get(0);
                listPreference.setValue(value);
            }
            Tools.MESA_LIBS = value;
        } else if (preferenceKey.equals("CDriverModels")) {
            array = Tools.getCompatibleCDriverModel(getContext());
            Tools.DRIVER_MODEL = value;
        } else if (preferenceKey.equals("ChooseMldo")) {
            array = Tools.getCompatibleCMesaLDO(getContext());
            Tools.LOADER_OVERRIDE = value;
        } else if (preferenceKey.equals("renderer")) {
            array = Tools.getCompatibleRenderers(getContext());
            Tools.LOCAL_RENDERER = value;
        } else if (preferenceKey.equals("chooseTurnipDriver")) {
            array = Tools.getCompatibleCTurnipDriver(getContext());
            Tools.TURNIP_LIBS = value;
        }
        listPreference.setEntries(array.getArray());
        listPreference.setEntryValues(array.getList().toArray(new String[0]));
    }

    private void closeOtherCustomMesaPref(PreferenceCategory customMesaVersionPref) {
        for (int i = 0; i < customMesaVersionPref.getPreferenceCount(); i++) {
            Preference closepref = customMesaVersionPref.getPreference(i);
            if (closepref instanceof SwitchPreference) {
                ((SwitchPreference) closepref).setChecked(false);
            }
        }
    }

    
    private void onCheckGPUDialog(Preference pre) {
        new CustomDialog.Builder(getContext())
                .setTitle("No No No No No!")
                .setMessage(getString(R.string.worning_system_vulkan_adreno))
                .setConfirmListener(R.string.preference_rendererexp_alertdialog_done, customView -> {
                    ((SwitchPreference) pre).setChecked(true);
                    return true;
                })
                .setCancelListener(R.string.alertdialog_cancel, customView -> true)
                .setCancelable(false)
                .setDraggable(true)
                .build()
                .show();
    }

    private void onExpRendererDialog(Preference pre, ListPreference rendererListPref) {
        new CustomDialog.Builder(getContext())
                .setTitle(getString(R.string.preference_rendererexp_alertdialog_warning))
                .setMessage(getString(R.string.preference_rendererexp_alertdialog_message))
                .setConfirmListener(R.string.preference_rendererexp_alertdialog_done, customView -> {
                    ((SwitchPreference) pre).setChecked(true);
                    onChangeRenderer(rendererListPref);
                    setListPreference(rendererListPref, "renderer");
                    return true;
                })
                .setCancelListener(R.string.preference_rendererexp_alertdialog_cancel, customView -> true)
                .setCancelable(false)
                .setDraggable(true)
                .build()
                .show();
    }

    // Custom Mesa GL/GLSL Version
    private void showSetGLVersionDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_mesa_version, null);

        mMesaGLVersion = view.findViewById(R.id.mesa_gl_version);
        mMesaGLSLVersion = view.findViewById(R.id.mesa_glsl_version);

        mMesaGLVersion.setText(LauncherPreferences.PREF_MESA_GL_VERSION);
        mMesaGLSLVersion.setText(LauncherPreferences.PREF_MESA_GLSL_VERSION);

        new CustomDialog.Builder(getContext())
                .setCustomView(view)
                .setCancelable(false)
                .setConfirmListener(R.string.alertdialog_done, customView -> {
                    String glVersion = mMesaGLVersion.getText().toString();
                    String glslVersion = mMesaGLSLVersion.getText().toString();

                    boolean validGLVersion = isValidVersion(glVersion, "2.8", "4.6");
                    boolean validGLSLVersion = isValidVersion(glslVersion, "280", "460");

                    if (!validGLVersion || !validGLSLVersion) {
                        if (!validGLVersion) {
                            mMesaGLVersion.setError(getString(R.string.customglglsl_alertdialog_error_gl));
                            mMesaGLVersion.requestFocus();
                        }
                        if (!validGLSLVersion) {
                            mMesaGLSLVersion.setError(getString(R.string.customglglsl_alertdialog_error_glsl));
                            mMesaGLSLVersion.requestFocus();
                        }
                        return false;
                    }

                    LauncherPreferences.PREF_MESA_GL_VERSION = glVersion;
                    LauncherPreferences.PREF_MESA_GLSL_VERSION = glslVersion;

                    LauncherPreferences.DEFAULT_PREF.edit()
                            .putString("mesaGLVersion", LauncherPreferences.PREF_MESA_GL_VERSION)
                            .putString("mesaGLSLVersion", LauncherPreferences.PREF_MESA_GLSL_VERSION)
                            .apply();

                    return true;
                })
                .setCancelListener(R.string.alertdialog_cancel, customView -> true)
                .setDraggable(true)
                .build()
                .show();
    }

    // Check whether the GL/GLSL version is within the acceptable range
    private boolean isValidVersion(String version, String minVersion, String maxVersion) {
        try {
            float versionNumber = Float.parseFloat(version);
            float minVersionNumber = Float.parseFloat(minVersion);
            float maxVersionNumber = Float.parseFloat(maxVersion);

            return versionNumber >= minVersionNumber && versionNumber <= maxVersionNumber;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void onChangeRenderer(ListPreference rendererListPref) {
        String rendererValue = LauncherPreferences.DEFAULT_PREF.getString("renderer", null);
        if ("mesa_3d".equals(rendererValue)) {
            LauncherPreferences.DEFAULT_PREF.edit().putString("renderer", expRenderer).apply();
            if (expRenderer != null) {
                rendererListPref.setValue(expRenderer);
            } else rendererListPref.setValueIndex(0);
        } else if ("vulkan_zink".equals(rendererValue)
                || "virglrenderer".equals(rendererValue)
                || "freedreno".equals(rendererValue)
                || "panfrost".equals(rendererValue)) {
            expRenderer = rendererValue;
            LauncherPreferences.DEFAULT_PREF.edit().putString("renderer", "mesa_3d").apply();
            rendererListPref.setValue("mesa_3d");
        }
    }

    private void loadMesaList() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setMessage(R.string.preference_rendererexp_mesa_download_load)
                .show();
        PojavApplication.sExecutorService.execute(() -> {
            Set<String> list = MesaUtils.INSTANCE.getMesaList();
            requireActivity().runOnUiThread(() -> {
                dialog.dismiss();

                if (list == null) {
                    AlertDialog alertDialog1 = new AlertDialog.Builder(requireActivity())
                            .setMessage(R.string.preference_rendererexp_mesa_get_fail)
                            .create();
                    alertDialog1.show();
                } else {
                    final String[] items3 = new String[list.size()];
                    list.toArray(items3);
                    // Add List
                    AlertDialog alertDialog3 = new AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.preference_rendererexp_mesa_select_download)
                            .setItems(items3, (dialogInterface, i) -> {
                                if (i < 0 || i > items3.length)
                                    return;
                                dialogInterface.dismiss();
                                downloadMesa(items3[i]);
                            })
                            .create();
                    alertDialog3.show();
                }
            });
        });
    }

    private void downloadMesa(String version) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setMessage(R.string.preference_rendererexp_mesa_downloading)
                .setCancelable(false)
                .show();
        PojavApplication.sExecutorService.execute(() -> {
            boolean data = MesaUtils.INSTANCE.downloadMesa(version);
            requireActivity().runOnUiThread(() -> {
                dialog.dismiss();
                if (data) {
                    Toast.makeText(requireContext(), R.string.preference_rendererexp_mesa_downloaded, Toast.LENGTH_SHORT)
                            .show();
                    setListPreference(requirePreference("CMesaLibrary", ChooseMesaListPref.class), "CMesaLibrary");
                } else {
                    AlertDialog alertDialog1 = new AlertDialog.Builder(requireActivity())
                            .setMessage(R.string.preference_rendererexp_mesa_download_fail)
                            .create();
                    alertDialog1.show();
                }
            });
        });
    }

    private void selectTurnipDriverFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select .so file"), FILE_SELECT_CODE);
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
        EditText input = new EditText(getActivity());
        input.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '.') {
                    return "";
                }
            }
            return null;
        }});
        new CustomDialog.Builder(getActivity())
            .setTitle(R.string.pgw_settings_ctu_version_name)
            .setCustomView(input)
            .setConfirmListener(android.R.string.confirm, customView -> {
                String folderName = input.getText().toString().trim();
                if (!folderName.isEmpty()) {
                    boolean success = TurnipUtils.INSTANCE.saveTurnipDriver(getActivity(), fileUri, folderName);
                    setListPreference(requirePreference("chooseTurnipDriver", ChooseTurnipListPref.class), "chooseTurnipDriver");
                    String message = success ? R.string.pgw_settings_ctu_saved : R.string.pgw_settings_ctu_save_fail;
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                } else {
                    input.setError(getString(R.string.global_error_field_empty));
                    return false;
                }
                return true;
            })
            .setCancelListener(android.R.string.cancel, customView -> true)
            .build()
            .show();
    }

}

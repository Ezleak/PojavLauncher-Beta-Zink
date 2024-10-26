package net.kdt.pojavlaunch.prefs.screens;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.firefly.ui.dialog.CustomDialog;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.DeletableListPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.MesaUtils;

import java.util.Set;

// Experimental Settings for Mesa renderer
public class LauncherPreferenceExperimentalFragment extends LauncherPreferenceFragment {

    private EditText mMesaGLVersion;
    private EditText mMesaGLSLVersion;
    private String expRenderer;

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_experimental);
        computeVisibility();

        final Preference downloadMesa = requirePreference("DownloadMesa", Preference.class);
        downloadMesa.setOnPreferenceClickListener((a) -> {
            loadMesaList();
            return true;
        });

        final DeletableListPreference CMesaLibP = requirePreference("CMesaLibrary", DeletableListPreference.class);
        final ListPreference CDriverModelP = requirePreference("CDriverModels", ListPreference.class);
        final ListPreference CMesaLDOP = requirePreference("ChooseMldo", ListPreference.class);

        setListPreference(CMesaLibP, "CMesaLibrary");
        setListPreference(CDriverModelP, "CDriverModels");
        setListPreference(CMesaLDOP, "ChooseMldo");

        CMesaLibP.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.MESA_LIBS = (String) obj;
            setListPreference(CDriverModelP, "CDriverModels");
            CDriverModelP.setValueIndex(0);
            return true;
        });

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
            onChangeRenderer();
            boolean isExpRenderer = (boolean) v;
            if (isExpRenderer) {
                onExpRendererDialog(p);
            }
            return true;
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

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        computeVisibility();
    }

    private void computeVisibility() {
        requirePreference("SpareFrameBuffer").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("MesaRendererChoose").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("customMesaVersionPref").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("customMesaLoaderDriverOverride").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("ChooseMldo").setVisible(LauncherPreferences.PREF_LOADER_OVERRIDE);
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

    private void onExpRendererDialog(Preference pre) {
        new CustomDialog.Builder(getContext())
                .setTitle(getString(R.string.preference_rendererexp_alertdialog_warning))
                .setMessage(getString(R.string.preference_rendererexp_alertdialog_message))
                .setConfirmListener(R.string.preference_rendererexp_alertdialog_done, customView -> true)
                .setCancelListener(R.string.preference_rendererexp_alertdialog_cancel, customView -> {
                    onChangeRenderer();
                    ((SwitchPreference) pre).setChecked(false);
                    return true;
                })
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

    private void onChangeRenderer() {
        String rendererValue = LauncherPreferences.DEFAULT_PREF.getString("renderer", null);
        if ("mesa_3d".equals(rendererValue)) {
            LauncherPreferences.DEFAULT_PREF.edit().putString("renderer", expRenderer).apply();
        } else if ("vulkan_zink".equals(rendererValue)
                || "virglrenderer".equals(rendererValue)
                || "freedreno".equals(rendererValue)
                || "panfrost".equals(rendererValue)) {
            expRenderer = LauncherPreferences.DEFAULT_PREF.getString("renderer", null);
            LauncherPreferences.DEFAULT_PREF.edit().putString("renderer", "mesa_3d").apply();
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
                    setListPreference(requirePreference("CMesaLibrary", DeletableListPreference.class), "CMesaLibrary");
                } else {
                    AlertDialog alertDialog1 = new AlertDialog.Builder(requireActivity())
                            .setMessage(R.string.preference_rendererexp_mesa_download_fail)
                            .create();
                    alertDialog1.show();
                }
            });
        });
    }

}

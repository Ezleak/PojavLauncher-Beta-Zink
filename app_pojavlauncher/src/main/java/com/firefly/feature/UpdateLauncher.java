package com.firefly.feature;

import static net.kdt.pojavlaunch.Architecture.ARCH_ARM;
import static net.kdt.pojavlaunch.Architecture.ARCH_ARM64;
import static net.kdt.pojavlaunch.Architecture.ARCH_X86;
import static net.kdt.pojavlaunch.Architecture.ARCH_X86_64;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.firefly.ui.dialog.CustomDialog;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLauncher {

    private static final String GITHUB_API = "https://api.github.com/repos/Vera-Firefly/Pojav-Glow-Worm/releases/latest";
    private static final String GITHUB_RELEASE_URL = "github.com/Vera-Firefly/Pojav-Glow-Worm/releases/download/%s/Pojav-Glow-Worm-%s-%s.apk";
    private static final String CACHE_APK_NAME = "cache.apk";
    private static final String APK_VERSION_FILE_NAME = "apk_version";
    private static final String IGNORE_VERSION_FILE_NAME = "ignore_version";

    private final Context context;
    private final File dir;
    private final int localVersionCode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UpdateLauncher(Context context) {
        this.context = context;
        this.dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        this.localVersionCode = getLocalVersionCode();
    }

    public void checkForUpdates(boolean ignore) {
        executor.execute(() -> {
            JSONObject releaseInfo = fetchReleaseInfo();
            if (releaseInfo != null) handleUpdateCheck(releaseInfo, ignore);
        });
    }

    private int getLocalVersionCode() {
        try {
            return Integer.parseInt(context.getString(R.string.base_version_code));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private JSONObject fetchReleaseInfo() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GITHUB_API).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return new JSONObject(response.body().string());
            }
        } catch (IOException | JSONException e) {
            handleException(e);
        }
        return null;
    }

    private void handleUpdateCheck(JSONObject releaseInfo, boolean ignore) {
        try {
            int remoteVersionCode = Integer.parseInt(releaseInfo.getString("tag_name").replaceAll("[^\\d]", ""));
            if (remoteVersionCode > localVersionCode) {
                handleCachedApk(releaseInfo, ignore, false);
            } else {
                if (!ignore) showToast(R.string.pgw_settings_updatelauncher_updated);
                handleCachedApk(releaseInfo, ignore, true);
            }
        } catch (JSONException e) {
            handleException(e);
        }
    }

    private void handleCachedApk(JSONObject releaseInfo, boolean ignore, boolean check) throws JSONException {
        String tagName = releaseInfo.getString("tag_name");
        String versionName = releaseInfo.getString("name");
        String releaseNotes = releaseInfo.getString("body");
        File apkFile = new File(dir, CACHE_APK_NAME);
        File apkVersionFile = new File(dir, APK_VERSION_FILE_NAME);
        File ignoreVersionFile = new File(dir, IGNORE_VERSION_FILE_NAME);

        if (ignoreVersionFile.exists() && shouldIgnoreVersion(ignoreVersionFile, tagName) && ignore && !check) return;

        if (apkFile.exists() && apkVersionFile.exists() && cachedVersionIsValid(apkVersionFile, tagName) && !check) {
            showInstallDialog(apkFile);
        } else {
            deleteFileIfExists(apkFile);
            deleteFileIfExists(apkVersionFile);
            if (!check) showUpdateDialog(tagName, versionName, releaseNotes);
        }
    }

    private boolean shouldIgnoreVersion(File ignoreVersionFile, String tagName) throws IOException {
        String savedIgnoreVersion = readFile(ignoreVersionFile);
        int savedIgnoreVersionCode = Integer.parseInt(savedIgnoreVersion.replaceAll("[^\\d]", ""));
        int releaseVersionCode = Integer.parseInt(tagName.replaceAll("[^\\d]", ""));
        return savedIgnoreVersionCode >= releaseVersionCode;
    }

    private boolean cachedVersionIsValid(File apkVersionFile, String tagName) throws IOException {
        String savedTagName = readFile(apkVersionFile); 
        int savedVersionCode = Integer.parseInt(savedTagName.replaceAll("[^\\d]", ""));
        int releaseVersionCode = Integer.parseInt(tagName.replaceAll("[^\\d]", ""));
        return savedVersionCode >= releaseVersionCode;
    }

    private void showUpdateDialog(String tagName, String versionName, String releaseNotes) {
        String archModel = getArchModel();

        new CustomDialog.Builder(context)
            .setTitle(context.getString(R.string.pgw_settings_updatelauncher_new_version, versionName))
            .setScrollMessage(releaseNotes)
            .setConfirmListener(R.string.pgw_settings_updatelauncher_update, customView -> {
                showDownloadSourceDialog(tagName, versionName, archModel);
                return true;
            })
            .setButton1Listener(context.getString(R.string.pgw_settings_updatelauncher_cancel), customView -> {
                saveIgnoreVersion(tagName);
                return true;
            })
            .setCancelListener(R.string.alertdialog_cancel, customView -> true)
            .build()
            .show();
    }

    private void showDownloadSourceDialog(String tagName, String versionName, String archModel) {
        String githubUrl = String.format(GITHUB_RELEASE_URL, tagName, versionName, archModel);

        new CustomDialog.Builder(context)
            .setTitle(context.getString(R.string.pgw_settings_updatelauncher_source))
            .setCancelable(false)
            .setItems(new String[]{"GitHub", "GHPROXY"}, selectedSource -> {
                String apkUrl = selectedSource.equals("GitHub") ?
                        "https://" + githubUrl :
                        "https://mirror.ghproxy.com/" + githubUrl;
                startDownload(apkUrl, tagName);
            })
            .setConfirmListener(R.string.alertdialog_cancel, customView -> true)
            .build()
            .show();
    }

    private void startDownload(String apkUrl, String tagName) {
        executor.execute(() -> downloadApk(apkUrl, tagName));
    }

    private void downloadApk(String apkUrl, String tagName) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apkUrl).build();
        File apkFile = new File(dir, CACHE_APK_NAME);
        File apkVersionFile = new File(dir, APK_VERSION_FILE_NAME);

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Files.write(apkFile.toPath(), response.body().bytes());
                writeFile(apkVersionFile, tagName);
                showDownloadCompleteDialog(apkFile);
            } else {
                showToast(R.string.pgw_settings_updatelauncher_download_fail);
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void showDownloadCompleteDialog(File apkFile) {
        new CustomDialog.Builder(context)
            .setTitle(context.getString(R.string.pgw_settings_updatelauncher_download_complete))
            .setMessage(context.getString(R.string.pgw_settings_updatelauncher_file_location, apkFile.getAbsolutePath()))
            .setConfirmListener(R.string.pgw_settings_updatelauncher_install, customView -> {
                installApk(apkFile);
                return true;
            })
            .setCancelListener(R.string.alertdialog_cancel, customView -> true)
            .setCancelable(false)
            .build()
            .show();
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private String getArchModel() {
        int arch = Tools.DEVICE_ARCHITECTURE;
        if (arch == ARCH_ARM64) return "arm64-v8a";
        if (arch == ARCH_ARM) return "armeabi-v7a";
        if (arch == ARCH_X86_64) return "x86_64";
        if (arch == ARCH_X86) return "x86";
        return "all";
    }

    private void saveIgnoreVersion(String tagName) {
        File ignoreVersionFile = new File(dir, IGNORE_VERSION_FILE_NAME);
        try {
            writeFile(ignoreVersionFile, tagName);
            LauncherPreferences.DEFAULT_PREF.edit().putString("ignoreVersion", tagName).apply();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        new Handler(Looper.getMainLooper()).post(() ->
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showToast(int messageResId) {
        new Handler(Looper.getMainLooper()).post(() ->
            Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show());
    }

    private void deleteFileIfExists(File file) {
        if (file.exists()) file.delete();
    }

    private String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void writeFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

    private void showInstallDialog(File apkFile) {
        new CustomDialog.Builder(context)
            .setTitle(context.getString(R.string.pgw_settings_updatelauncher_install_prompt_title))
            .setMessage(context.getString(R.string.pgw_settings_updatelauncher_install_prompt_message))
            .setConfirmListener(R.string.pgw_settings_updatelauncher_install, customView -> {
                installApk(apkFile);
                return true;
            })
            .setCancelListener(R.string.alertdialog_cancel, customView -> true)
            .setCancelable(false)
            .build()
            .show();
    }
}
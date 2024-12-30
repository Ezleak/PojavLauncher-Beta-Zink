package com.firefly.feature;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TurnipDownloader {

    private static final String BASE_URL = "https://github.com/Vera-Firefly/TurnipDriver-CI/releases";
    private static final String VERSION_JSON_URL = BASE_URL + "/tag/100000/version.json";
    private static final String DOWNLOAD_URL_FORMAT = BASE_URL + "/download/%s/%s.zip";

    private final Context context;
    private final Map<String, String> versionTagMap = new HashMap<>();

    public TurnipDownloader(Context context) {
        this.context = context;
    }

    public void fetchVersionList(VersionListCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(VERSION_JSON_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                try {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                        StringBuilder result = new StringBuilder();
                        int byteRead;
                        while ((byteRead = inputStream.read()) != -1) {
                            result.append((char) byteRead);
                        }
                        inputStream.close();

                        parseVersionList(result.toString());
                        runOnMainThread(callback::onSuccess);
                    } else {
                        runOnMainThread(() -> callback.onError("Failed to fetch version list: " + connection.getResponseCode()));
                    }
                } catch (Exception e) {
                    // .e
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("TurnipDownloader", "Error fetching version list", e);
                runOnMainThread(() -> callback.onError("Error fetching version list: " + e.getMessage()));
            }
        }).start();
    }

    public void downloadVersionFile(String version, File destination, DownloadCallback callback) {
        new Thread(() -> {
            try {
                String tag = versionTagMap.get(version);
                if (tag == null) {
                    runOnMainThread(() -> callback.onError("Tag not found for version: " + version));
                    return;
                }

                String downloadUrl = String.format(DOWNLOAD_URL_FORMAT, tag, version);
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                try {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        FileOutputStream outputStream = new FileOutputStream(destination);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();

                        runOnMainThread(callback::onSuccess);
                    } else {
                        runOnMainThread(() -> callback.onError("Failed to download file: " + connection.getResponseCode()));
                    }
                } catch (Exception e) {
                    Log.e("TurnipDownloader", "Error downloading version file", e);
                    runOnMainThread(() -> callback.onError("Error downloading file: " + e.getMessage()));
                }
            } catch (Exception e) {
              // .e
            } finally {
                connection.disconnect();
            }
        }).start();
    }

    private void parseVersionList(String json) throws Exception {
        versionTagMap.clear();
        JSONObject root = new JSONObject(json);
        JSONArray versionArray = root.getJSONArray("versions");

        for (int i = 0; i < versionArray.length(); i++) {
            JSONObject versionObject = versionArray.getJSONObject(i);
            String version = versionObject.getString("version");
            String tag = versionObject.getString("tag");
            versionTagMap.put(version, tag);
        }
    }

    private void runOnMainThread(Runnable runnable) {
        new android.os.Handler(context.getMainLooper()).post(runnable);
    }

    public interface VersionListCallback {
        void onSuccess();

        void onError(String error);
    }

    public interface DownloadCallback {
        void onSuccess();

        void onError(String error);
    }
}
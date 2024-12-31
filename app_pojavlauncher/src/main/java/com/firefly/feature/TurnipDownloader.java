package com.firefly.feature;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TurnipDownloader {
    private static final String BASE_URL = "https://github.com/Vera-Firefly/TurnipDriver-CI/releases/download/";
    private static final String VERSION_JSON_URL = BASE_URL + "100000/version.json";
    private static final String DOWNLOAD_URL_TEMPLATE = BASE_URL + "%s/%s.zip";

    private static File dir;
    private static final Map<String, String> versionMap = new HashMap<>();

    private static void initDownloadDir(Context context) {
        if (dir == null) {
            dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Turnip");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Failed to create download directory: " + dir.getAbsolutePath());
            }
        }
    }

    public static Set<String> getTurnipList(Context context) {
        File tempFile = null;
        initDownloadDir(context);

        try {
            tempFile = new File(dir, "version.json");

            URL url = new URL(VERSION_JSON_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray versions = jsonObject.getJSONArray("versions");

            Set<String> versionSet = new HashSet<>();
            versionMap.clear();
            for (int i = 0; i < versions.length(); i++) {
                JSONObject versionObject = versions.getJSONObject(i);
                String version = versionObject.getString("version");
                String tag = versionObject.getString("tag");
                versionMap.put(version, tag);
                versionSet.add(version);
            }

            return versionSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    public static boolean downloadTurnipFile(Context context, String version) {
        initDownloadDir(context);

        try {
            String tag = versionMap.get(version);
            if (tag == null) return false;

            String fileUrl = String.format(DOWNLOAD_URL_TEMPLATE, tag, version);
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                File targetFile = new File(dir, version + ".zip");

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
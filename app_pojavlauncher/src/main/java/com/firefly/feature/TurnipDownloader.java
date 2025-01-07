package com.firefly.feature;

import android.content.Context;
import android.os.Environment;

import com.firefly.utils.TurnipUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TurnipDownloader {
    private static final String BASE_URL = "github.com/Vera-Firefly/TurnipDriver-CI/releases/download";
    private static final String FALLBACK_BASE_URL = "github.com/K11MCH1/AdrenoToolsDrivers/releases/download";
    private static final String VERSION_JSON_PATH = "/100000/version.json";
    private static final String DOWNLOAD_URL_TEMPLATE = "%s/%s/%s.zip";

    private static String DLS = "https://";
    private static File downloadDir;
    private static final Map<String, String> versionNameMap = new HashMap<>();
    private static final Map<String, String> turnipNameMap = new HashMap<>();
    private static volatile boolean isCancelled = false;

    // Initialize download directory
    private static void initDownloadDir(Context context) {
        if (downloadDir == null) {
            downloadDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Turnip");
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                throw new IllegalStateException("Failed to create download directory: " + downloadDir.getAbsolutePath());
            }
        }
    }

    public static void cancelDownload() {
        isCancelled = true;
    }

    public static boolean isDownloadCancelled() {
        return isCancelled;
    }

    // Get the list of Turnip versions
    public static List<String> getTurnipList(Context context, int sourceType) {
        isCancelled = false;
        initDownloadDir(context);

        String versionUrl = resolveVersionUrl(sourceType);
        if (versionUrl == null) {
            System.err.println("No valid source URL found.");
            return null;
        }

        File versionFile = new File(downloadDir, "version.json");
        if (!downloadFile(versionUrl, versionFile)) {
            return null;
        }

        return parseVersionFile(versionFile);
    }

    // Download and extract the specified version of Turnip
    public static boolean downloadTurnipFile(Context context, String version) {
        initDownloadDir(context);

        String tag = versionNameMap.get(version);
        if (tag == null) {
            System.err.println("Version tag not found for version: " + version);
            return false;
        }

        String fileUrl = resolveDownloadUrl(tag, version);
        if (fileUrl == null) {
            System.err.println("No valid URL found for downloading version: " + version);
            return false;
        }

        File zipFile = new File(downloadDir, version + ".zip");
        if (!downloadFile(fileUrl, zipFile)) {
            return false;
        }

        File extractDir = new File(downloadDir, version);
        if (!unzipFile(zipFile, extractDir)) {
            System.err.println("Failed to unzip file: " + zipFile.getAbsolutePath());
            return false;
        }

        // Clean up the zip file after extraction
        if (!zipFile.delete()) {
            System.err.println("Failed to delete zip file: " + zipFile.getAbsolutePath());
        }

        return true;
    }

    // Save the Turnip file to the target directory
    public static boolean saveTurnipFile(Context context, String version) {
        String tag = versionNameMap.get(version);
        String fileName = turnipNameMap.get(tag);
        if (tag == null || fileName == null) {
            System.err.println("Invalid version or file name.");
            return false;
        }

        File sourceFile = new File(downloadDir, version + "/" + fileName);
        return copyFileToTurnipDir(sourceFile, version);
    }

    // Resolve the URL to fetch the version.json file
    private static String resolveVersionUrl(int sourceType) {
        String[] sources = {
            "https://",
            "https://mirror.ghproxy.com/"
        };
        if (sourceType > 0 && sourceType <= sources.length) {
            DLS = sources[sourceType - 1];
            return DLS + BASE_URL + VERSION_JSON_PATH;
        }
        for (String source : sources) {
            String testUrl = source + BASE_URL + VERSION_JSON_PATH;
            if (checkUrlAvailability(testUrl)) {
                DLS = source;
                return testUrl;
            }
        }
        return null;
    }

    // Resolve the download URL for the given version and tag
    private static String resolveDownloadUrl(String tag, String version) {
        String[] baseUrls = {
            DLS + BASE_URL,
            DLS + FALLBACK_BASE_URL
        };
        for (String baseUrl : baseUrls) {
            String testUrl = String.format(DOWNLOAD_URL_TEMPLATE, baseUrl, tag, version);
            if (checkUrlAvailability(testUrl)) {
                return testUrl;
            }
        }
        return null;
    }

    // Download a file from the given URL to the target file
    private static boolean downloadFile(String fileUrl, File targetFile) {
        try (InputStream inputStream = new URL(fileUrl).openStream();
             FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (isCancelled) {
                    targetFile.delete();
                    return false;
                }
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Unzip the downloaded file
    private static boolean unzipFile(File zipFile, File targetDir) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + outFile.getAbsolutePath());
                    }
                } else {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if a URL is available
    private static boolean checkUrlAvailability(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 400;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Copy file to the target directory
    private static boolean copyFileToTurnipDir(File sourceFile, String folderName) {
        File targetDir = new File(TurnipUtils.INSTANCE.getTurnipDir(), folderName);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return false;
        }
        File targetFile = new File(targetDir, "libvulkan_freedreno.so");
        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
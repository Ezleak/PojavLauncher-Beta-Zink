package com.firefly.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TurnipUtils {

    public static final TurnipUtils INSTANCE = new TurnipUtils();
    private final File turnipDir;

    private TurnipUtils() {
        this.turnipDir = new File(Tools.TURNIP_DIR);
        if (!turnipDir.exists() && !turnipDir.mkdirs()) {
            throw new RuntimeException("Failed to create Turnip directory");
        }
    }

    public File getTurnipDir() {
        return turnipDir;
    }

    public List<String> getTurnipDriverList() {
        List<String> list = new ArrayList<>();
        File[] files = turnipDir.listFiles();
        for (File file : files) {
            if (file.isDirectory() && new File(file.getAbsolutePath() + "/libvulkan_freedreno.so").exists()) {
                list.add(file.getName());
            }
        }
        return list;
    }

    public String getTurnipDriver(String version) {
        return Tools.TURNIP_DIR + "/" + version;
    }

    /** 首先我们有考虑过在启动器内下载
     * 但启动器内下载的不一定满足所有用户
     * 经过慎重考虑,还是决定让用户自己选择,并非在启动器内下载
     * 也许后面会出一个在启动器内下载的方法
     * 但直觉告诉我,这将止步于此,启动器内下载要重新考虑方案
     * 而且满足不了所有用户
     */
    public boolean saveTurnipDriver(Context context, Uri fileUri, String folderName) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null || !PGWTools.isELFFile(inputStream)) {
                return false;
            }

            inputStream.close(); // Close an open validation file stream
            InputStream newInputStream = context.getContentResolver().openInputStream(fileUri);

            File targetDir = new File(turnipDir, folderName);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                return false;
            }

            File targetFile = new File(targetDir, "libvulkan_freedreno.so");
            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = newInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTurnipDriver(String version) {
        File libDir = new File(turnipDir, version);
        if (libDir.exists()) {
            return deleteDirectory(libDir);
        }
        return false;
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
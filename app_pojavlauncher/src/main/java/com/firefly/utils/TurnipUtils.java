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
    private Map<String, MesaObj> mesaObjMap;

    private TurnipUtils() {
        this.turnipDir = new File(Tools.TURNIP_DIR);
        if (!turnipDir.exists() && !turnipDir.mkdirs()) {
            throw new RuntimeException("Failed to create Turnip directory");
        }
    }

    public List<String> getTurnipDriverList() {
        List<String> list = new ArrayList<>();
        File[] files = turnipDir.listFiles();
        for (File file : files) {
            if (file.isDirectory() && new File(file.getAbsolutePath() + "/turnip.so").exists()) {
                list.add(file.getName());
            }
        }
        return list;
    }

    public String getTurnipDriver(String version) {
        return Tools.TURNIP_DIR + "/" + version + "/turnip.so";
    }

    public boolean saveTurnipDriver(Context context, Uri fileUri, String folderName) {
        try {
            File targetDir = new File(turnipDir, folderName);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                return false;
            }

            File targetFile = new File(targetDir, "turnip.so");

            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                 OutputStream outputStream = new FileOutputStream(targetFile)) {
                if (inputStream == null) return false;

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
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
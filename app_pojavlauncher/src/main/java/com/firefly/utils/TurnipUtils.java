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

public class TurnipUtils {

    private Context context;
    private File turnipDir;

    public TurnipUtils(Context context) {
        this.context = context;
        // Define TURNIP_DIR in external storage
        this.turnipDir = new File(Tools.TURNIP_DIR);
        if (!turnipDir.exists() && !turnipDir.mkdirs()) {
            throw new RuntimeException("Failed to create Turnip directory");
        }
    }

    /**
     * 将选择的驱动文件重命名为 turnip.so 并保存到指定文件夹中
     *
     * @param fileUri    选择的驱动文件 URI
     * @param folderName 用户输入的文件夹名称
     * @return 操作是否成功
     */
    public boolean saveTurnipDriver(Uri fileUri, String folderName) {
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
}
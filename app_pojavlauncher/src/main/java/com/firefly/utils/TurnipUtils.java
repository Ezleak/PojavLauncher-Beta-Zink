package com.firefly.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TurnipUtils {

    private Context context;
    private File turnipDir;

    public TurnipUtils(Context context) {
        this.context = context;
        this.turnipDir = new File(Tools.TURNIP_DIR);
        if (!turnipDir.exists() && !turnipDir.mkdirs()) {
            // Toast.makeText(context, R.string.turnip_dir_creation_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleTurnipFile(Uri uri) {
        if (!isValidSoFile(uri)) {
            // Toast.makeText(context, R.string.invalid_so_file, Toast.LENGTH_SHORT).show();
            return;
        }

        File destinationFile = new File(turnipDir, "turnip.so");

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                // Toast.makeText(context, R.string.file_read_error, Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Toast.makeText(context, R.string.file_move_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("TurnipUtils", "Error handling turnip file", e);
            // Toast.makeText(context, R.string.file_move_error, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isValidSoFile(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        return documentFile != null && documentFile.getName() != null && documentFile.getName().endsWith(".so");
    }
}
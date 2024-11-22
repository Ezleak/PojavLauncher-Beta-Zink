package com.firefly.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.firefly.ui.dialog.CustomDialog;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class ResolutionAdjuster {

    private static float mScaleFactor;
    private final Context context;
    private final OnResolutionChangeListener listener;

    public ResolutionAdjuster(Context context, OnResolutionChangeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // 显示滑动条弹窗
    public void showSeekBarDialog() {
        if (mScaleFactor == 0.0f) mScaleFactor = LauncherPreferences.PREF_SCALE_FACTOR / 100f;
        int percentage = Math.round(mScaleFactor * 100);

        // 动态创建一个LinearLayout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 8, 8, 8);
        layout.setGravity(Gravity.CENTER);

        // 动态创建 "-" 按钮
        final TextView minusButton = new TextView(context);
        minusButton.setText("-");
        minusButton.setTextSize(18);
        minusButton.setGravity(Gravity.CENTER);
        minusButton.setPadding(16, 16, 16, 16);
        layout.addView(minusButton);

        // 动态创建一个 SeekBar 用于调整缩放因子
        final SeekBar scaleSeekBar = new SeekBar(context);
        scaleSeekBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); // 权重 1
        int maxScaleFactor = Math.max(LauncherPreferences.PREF_SCALE_FACTOR, 100);
        scaleSeekBar.setMax(maxScaleFactor - 25);
        scaleSeekBar.setProgress((int) (mScaleFactor * 100) - 25);
        layout.addView(scaleSeekBar);

        // 动态创建 "+" 按钮
        final TextView plusButton = new TextView(context);
        plusButton.setText("+");
        plusButton.setTextSize(18);
        plusButton.setGravity(Gravity.CENTER);
        plusButton.setPadding(16, 16, 16, 16);
        layout.addView(plusButton);

        // 动态创建一个TextView用于显示当前分辨率
        final TextView resolutionTextView = new TextView(context);
        changeResolutionRatioPreview(percentage, resolutionTextView);
        resolutionTextView.setTextSize(14);
        resolutionTextView.setPadding(10, 0, 0, 0);
        layout.addView(resolutionTextView);

        // 动态创建一个TextView用于显示缩放百分数
        final TextView scaleTextView = new TextView(context);
        scaleTextView.setText(percentage + "%");
        scaleTextView.setTextSize(14);
        scaleTextView.setPadding(10, 0, 0, 0);
        layout.addView(scaleTextView);

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mScaleFactor = (progress + 25) / 100f;
                listener.onChange(mScaleFactor);
                int scaleFactor = Math.round(mScaleFactor * 100);
                scaleTextView.setText(scaleFactor + "%");
                changeResolutionRatioPreview(scaleFactor, resolutionTextView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        minusButton.setOnClickListener(v -> {
            int currentProgress = scaleSeekBar.getProgress();
            if (currentProgress > 0) {
                scaleSeekBar.setProgress(currentProgress - 1); // 微调 -1
            }
        });

        plusButton.setOnClickListener(v -> {
            int currentProgress = scaleSeekBar.getProgress();
            if (currentProgress < scaleSeekBar.getMax()) {
                scaleSeekBar.setProgress(currentProgress + 1); // 微调 +1
            }
        });

        // 创建并显示弹窗
        new CustomDialog.Builder(context)
                .setTitle(context.getString(R.string.mcl_setting_title_resolution_scaler))
                .setCustomView(layout)
                .setCancelable(false)
                .setDraggable(true)
                .setConfirmListener(android.R.string.ok, customView -> true)
                .build()
                .show();
    }

    private void changeResolutionRatioPreview(int progress, TextView resolutionTextView) {
        DisplayMetrics metrics = Tools.currentDisplayMetrics;
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        boolean isLandscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || width > height;

        double progressDouble = (double) progress / 100;
        // 计算要显示的宽高,用Tools现有的方案getDisplayFriendlyRes()确保是偶数
        int previewWidth = Tools.getDisplayFriendlyRes(isLandscape ? width : height, (float) progressDouble);
        int previewHeight = Tools.getDisplayFriendlyRes(isLandscape ? height : width, (float) progressDouble);

        String preview = previewWidth + " x " + previewHeight;
        resolutionTextView.setText(preview);  // 实时更新TextView中的分辨率
    }

    public interface OnResolutionChangeListener {
        void onChange(float value);
    }
}
package com.firefly.ui.dialog;

import androidx.annotation.Nullable;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.movtery.ui.dialog.DraggableDialog;

import net.kdt.pojavlaunch.R;

public class CustomDialog implements DraggableDialog.DialogInitializationListener {
    private final AlertDialog dialog;
    private final String[] items;
    private final OnItemClickListener itemClickListener;
    private int titleHeight;

    private CustomDialog(Context context, String title, String message, String scrollmessage,
                         View customView, String confirmButtonText, String cancelButtonText,
                         OnCancelListener cancelListener, OnConfirmListener confirmListener,
                         String button1Text, String button2Text, String button3Text, String button4Text,
                         OnButtonClickListener button1Listener, OnButtonClickListener button2Listener,
                         OnButtonClickListener button3Listener, OnButtonClickListener button4Listener,
                         String[] items, OnItemClickListener itemClickListener,
                         boolean cancelable, boolean draggable) {

        this.items = items;
        this.itemClickListener = itemClickListener;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 20, 50, 20);

        TextView titleTextView = new TextView(context);
        TextView messageTextView = new TextView(context);
        ScrollView scrollView = new ScrollView(context);
        TextView scrollMessageTextView = new TextView(context);
        FrameLayout customContainer = new FrameLayout(context);
        ListView listView = new ListView(context);

        if (title != null && !title.isEmpty()) {
            titleTextView.setText(title);
            titleTextView.setGravity(Gravity.CENTER);
            titleTextView.setTextSize(26);
            mainLayout.addView(titleTextView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            titleTextView.post(() -> {
                titleHeight = titleTextView.getHeight();
                adjustHeights(scrollView, scrollmessage, listView, items);
            });
        }

        if (message != null && !message.isEmpty()) {
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.START);
            titleTextView.setTextSize(22);
            mainLayout.addView(messageTextView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        }

        if (scrollmessage != null && !scrollmessage.isEmpty()) {
            scrollMessageTextView.setText(scrollmessage);
            scrollView.addView(scrollMessageTextView);
            mainLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        }

        if (customView != null) {
            customContainer.addView(customView);
            mainLayout.addView(customContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        }

        if (items != null && items.length > 0) {
            mainLayout.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_custom_layout, null);

        Button button1 = view.findViewById(R.id.custom_dialog_button_1);
        Button button2 = view.findViewById(R.id.custom_dialog_button_2);
        Button button3 = view.findViewById(R.id.custom_dialog_button_3);
        Button button4 = view.findViewById(R.id.custom_dialog_button_4);
        Button confirmButton = view.findViewById(R.id.custom_dialog_confirm_button);
        Button cancelButton = view.findViewById(R.id.custom_dialog_cancel_button);

        mainLayout.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        if (confirmButtonText != null) confirmButton.setText(confirmButtonText);

        builder.setView(mainLayout);
        dialog = builder.create();

        if (draggable) dialog.setOnShowListener(dialogInterface -> DraggableDialog.initDialog(this));

        if (!cancelable) dialog.setCancelable(false);

        if (button1Listener != null) {
            button1.setVisibility(View.VISIBLE);
            if (button1Text != null) button1.setText(button1Text);
            button1.setOnClickListener(v -> {
                boolean shouldDismiss = button1Listener.onClick(customView);
                if (shouldDismiss) dialog.dismiss();
            });
        }

        if (button2Listener != null) {
            button2.setVisibility(View.VISIBLE);
            if (button2Text != null) button2.setText(button2Text);
            button2.setOnClickListener(v -> {
                boolean shouldDismiss = button2Listener.onClick(customView);
                if (shouldDismiss) dialog.dismiss();
            });
        }

        if (button3Listener != null) {
            button3.setVisibility(View.VISIBLE);
            if (button3Text != null) button3.setText(button3Text);
            button3.setOnClickListener(v -> {
                boolean shouldDismiss = button3Listener.onClick(customView);
                if (shouldDismiss) dialog.dismiss();
            });
        }

        if (button4Listener != null) {
            button4.setVisibility(View.VISIBLE);
            if (button4Text != null) button4.setText(button4Text);
            button4.setOnClickListener(v -> {
                boolean shouldDismiss = button4Listener.onClick(customView);
                if (shouldDismiss) dialog.dismiss();
            });
        }

        if (cancelListener != null) {
            cancelButton.setVisibility(View.VISIBLE);
            if (cancelButtonText != null) cancelButton.setText(cancelButtonText);
            cancelButton.setOnClickListener(v -> {
                boolean shouldDismiss = cancelListener.onCancel(customView);
                if (shouldDismiss) dialog.dismiss();
            });
        }

        confirmButton.setOnClickListener(v -> {
            boolean shouldDismiss = true;
            if (confirmListener != null) shouldDismiss = confirmListener.onConfirm(customView);
            if (shouldDismiss) dialog.dismiss();
        });

        if (itemClickListener != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                String item = items[position];
                itemClickListener.onItemClick(item, position);
                dialog.dismiss();
            });
        }
    }

    private void adjustHeights(ScrollView scrollView, String scrollmessage, ListView listView, String[] items) {
        if (scrollmessage != null && !scrollmessage.isEmpty()) {
            scrollView.post(() -> {
                int scrollHeight = Math.min(scrollView.getHeight(), titleHeight);
                scrollView.getLayoutParams().height = scrollHeight;
                scrollView.requestLayout();
            });
        }

        if (items != null && items.length > 0) {
            listView.post(() -> {
                int listHeight = Math.min(listView.getHeight(), titleHeight);
                listView.getLayoutParams().height = listHeight;
                listView.requestLayout();
            });
        }
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public Window onInit() {
        return dialog.getWindow();
    }

    public interface OnButtonClickListener {
        boolean onClick(View view);
    }

    public interface OnConfirmListener {
        boolean onConfirm(View view);
    }

    public interface OnCancelListener {
        boolean onCancel(View view);
    }

    public interface OnItemClickListener {
        void onItemClick(String item, @Nullable Integer index);
    }

    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private String scrollmessage;
        private View customView;
        private String button1Text;
        private String button2Text;
        private String button3Text;
        private String button4Text;
        private String confirmButtonText;
        private String cancelButtonText;
        private OnButtonClickListener button1Listener;
        private OnButtonClickListener button2Listener;
        private OnButtonClickListener button3Listener;
        private OnButtonClickListener button4Listener;
        private OnCancelListener cancelListener;
        private OnConfirmListener confirmListener;
        private String[] items;
        private OnItemClickListener itemClickListener;
        private boolean cancelable = true;
        private boolean draggable = false;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setScrollMessage(String scrollmessage) {
            this.scrollmessage = scrollmessage;
            return this;
        }

        public Builder setCustomView(View customView) {
            this.customView = customView;
            return this;
        }

        public Builder setItems(String[] items, OnItemClickListener listener) {
            this.items = items;
            this.itemClickListener = listener;
            return this;
        }

        public Builder setButton1Listener(String buttonText, OnButtonClickListener listener) {
            this.button1Text = buttonText;
            this.button1Listener = listener;
            return this;
        }

        public Builder setButton2Listener(String buttonText, OnButtonClickListener listener) {
            this.button2Text = buttonText;
            this.button2Listener = listener;
            return this;
        }

        public Builder setButton3Listener(String buttonText, OnButtonClickListener listener) {
            this.button3Text = buttonText;
            this.button3Listener = listener;
            return this;
        }

        public Builder setButton4Listener(String buttonText, OnButtonClickListener listener) {
            this.button4Text = buttonText;
            this.button4Listener = listener;
            return this;
        }

        public Builder setConfirmListener(int confirmButtonTextResId, OnConfirmListener confirmListener) {
            this.confirmButtonText = context.getString(confirmButtonTextResId);
            this.confirmListener = confirmListener;
            return this;
        }

        public Builder setCancelListener(int cancelButtonTextResId, OnCancelListener cancelListener) {
            this.cancelButtonText = context.getString(cancelButtonTextResId);
            this.cancelListener = cancelListener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        public CustomDialog build() {
            return new CustomDialog(context, title, message, scrollmessage, customView,
                    confirmButtonText, cancelButtonText, cancelListener, confirmListener,
                    button1Text, button2Text, button3Text, button4Text,
                    button1Listener, button2Listener, button3Listener, button4Listener,
                    items, itemClickListener, cancelable, draggable);
        }
    }
}
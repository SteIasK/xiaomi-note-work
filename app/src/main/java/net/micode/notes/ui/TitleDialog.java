package net.micode.notes.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

public class TitleDialog {

    public interface TitleDialogListener {
        void onTitleEntered(String title);
    }

    public static void show(Context context, TitleDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("输入标题");

        // 设置输入框
        final EditText input = new EditText(context);
        builder.setView(input);

        // 设置按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                if (!TextUtils.isEmpty(title)) {
                    listener.onTitleEntered(title);
                } else {
                    Toast.makeText(context, "标题不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}

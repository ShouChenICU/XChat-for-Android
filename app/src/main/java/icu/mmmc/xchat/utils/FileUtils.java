package icu.mmmc.xchat.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;

import icu.mmmc.xchat.R;
import icu.xchat.core.Identity;
import icu.xchat.core.utils.IdentityUtils;

public final class FileUtils {

    public static void storeIdentity(Activity activity, Identity identity) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.store_identity_dialog, null);
        EditText password = layout.findViewById(R.id.password);
        EditText fileName = layout.findViewById(R.id.file_name);
        TextView storePath = layout.findViewById(R.id.store_path);
        storePath.setText(path);
        fileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                storePath.setText(path + "/" + editable.toString() + ".xid");
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("保存身份")
                .setView(layout)
                .setPositiveButton("保存", (dialogInterface, i) -> {
                    fileName.setText(fileName.getText().toString().trim());
                    if (fileName.getText().toString().isEmpty()) {
                        Toast.makeText(activity, "文件名为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File file = new File(path, fileName.getText().toString() + ".xid");
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] data = IdentityUtils.encodeIdentity(identity, password.getText().toString());
                        outputStream.write(data);
                        Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
}
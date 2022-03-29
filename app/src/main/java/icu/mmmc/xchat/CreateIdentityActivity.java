package icu.mmmc.xchat;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;

import icu.xchat.core.Identity;
import icu.xchat.core.constants.UserAttributes;
import icu.xchat.core.utils.IdentityUtils;

public class CreateIdentityActivity extends AppCompatActivity {
    private Identity identity;
    private TextView uidCode;
    private EditText nickName;
    private int rsaLength;

    private void loadViews() {
        findViewById(R.id.back_btn).setOnClickListener(e -> finish());
        uidCode = findViewById(R.id.uid_code);
        nickName = findViewById(R.id.nickname);
        findViewById(R.id.create_btn).setOnClickListener(e -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("设置RSA长度");
            builder.setSingleChoiceItems(new String[]{"2048", "4096"},
                    0, (dialogInterface, i) -> rsaLength = (i == 0 ? 2048 : 4096))
                    .setPositiveButton("确定",
                            (dialogInterface, i) -> {
                                try {
                                    identity = IdentityUtils.genIdentity("RSA", rsaLength);
                                    refresh();
                                } catch (Exception exception) {
                                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                    .show();
        });
        findViewById(R.id.store_btn).setOnClickListener(v -> {
            if (identity == null) {
                Toast.makeText(this, "请生成一个身份", Toast.LENGTH_SHORT).show();
                return;
            }
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.store_identity_dialog, null);
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
                    storePath.setText(path + "/" + editable.toString());
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("保存身份")
                    .setView(layout)
                    .setPositiveButton("保存", (dialogInterface, i) -> {
                        fileName.setText(fileName.getText().toString().trim());
                        if (fileName.getText().toString().isEmpty()) {
                            Toast.makeText(this, "文件名为空！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try (FileOutputStream outputStream = new FileOutputStream(new File(path, fileName.getText().toString()))) {
                            identity.setAttribute(UserAttributes.$NICK, nickName.getText().toString());
                            byte[] data = IdentityUtils.encodeIdentity(identity, password.getText().toString());
                            outputStream.write(data);
                            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });
    }

    private void refresh() {
        if (identity != null) {
            uidCode.setText(identity.getUidCode());
        } else {
            uidCode.setText("点左下随机生成");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_identity);
        rsaLength = 2048;
        loadViews();
        refresh();
    }
}
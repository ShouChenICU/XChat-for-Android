package icu.mmmc.xchat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import icu.mmmc.xchat.utils.FileUtils;
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
            try {
                identity.setAttribute(UserAttributes.$NICK, nickName.getText().toString());
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            FileUtils.storeIdentity(this, identity);
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
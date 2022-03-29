package icu.mmmc.xchat;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import icu.mmmc.xchat.utils.FileUtils;
import icu.xchat.core.Identity;
import icu.xchat.core.XChatCore;
import icu.xchat.core.constants.UserAttributes;

public class EditIdentityActivity extends AppCompatActivity {
    private TextView uidCode;
    private EditText nickname;
    private Uri identityUri;

    private void loadViews() {
        this.uidCode = findViewById(R.id.uid_code);
        this.nickname = findViewById(R.id.nickname);
        findViewById(R.id.store_btn).setOnClickListener(v -> {
            Identity identity = XChatCore.getIdentity();
            if (identity == null) {
                Toast.makeText(this, "未加载身份！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (identityUri == null) {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                identity.setAttribute(UserAttributes.$NICK, nickname.getText().toString());
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            FileUtils.storeIdentity(this, identity);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_identity);
        this.identityUri = IdentityManagerActivity.getIdentityUri();
        loadViews();
        refresh();

    }

    private void refresh() {
        Identity identity = XChatCore.getIdentity();
        if (identity == null) {
            return;
        }
        this.uidCode.setText(identity.getUidCode());
        this.nickname.setText(identity.getAttribute(UserAttributes.$NICK));
    }
}
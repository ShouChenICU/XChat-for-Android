package icu.mmmc.xchat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

import icu.xchat.core.Identity;
import icu.xchat.core.XChatCore;
import icu.xchat.core.constants.UserAttributes;
import icu.xchat.core.utils.IdentityUtils;

public class IdentityManagerActivity extends AppCompatActivity {
    private TextView nickname;
    private TextView uidCode;
    private TextView menuBtn;
    private TextView genderText;
    private TextView birthText;
    private TextView emailText;
    private TextView phoneText;

    private void loadViews() {
        this.nickname = findViewById(R.id.nickname);
        this.uidCode = findViewById(R.id.uidCode);
        this.menuBtn = findViewById(R.id.menuBtn);
        this.genderText = findViewById(R.id.gender_text);
        this.birthText = findViewById(R.id.birth_text);
        this.emailText = findViewById(R.id.email_text);
        this.phoneText = findViewById(R.id.phone_text);
        this.uidCode.setOnLongClickListener(view -> {
            ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("", uidCode.getText()));
            Toast.makeText(this, "识别码已复制", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_manager);
        findViewById(R.id.backBtn).setOnClickListener(e -> finish());
        loadViews();
        menuBtn.setOnClickListener(e -> openContextMenu(menuBtn));
        registerForContextMenu(menuBtn);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.identity_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (Objects.equals(item.getItemId(), R.id.load_identity)) {
            if (XChatCore.getIdentity() != null) {
                Toast.makeText(this, "请先注销当前身份", Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        } else if (Objects.equals(item.getItemId(), R.id.logout)) {
            XChatCore.logout();
            refresh();
            Toast.makeText(this, "注销成功", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(item.getItemId(), R.id.create_identity)) {
            Intent intent = new Intent(this, CreateIdentityActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null) {
                Uri uri = data.getData();
                EditText password = new EditText(this);
                password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请输入密码")
                        .setNegativeButton("取消", (dialogInterface, i) -> Toast.makeText(IdentityManagerActivity.this, "取消加载", Toast.LENGTH_SHORT).show())
                        .setPositiveButton("确定", (d, i) -> {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            byte[] buf = new byte[512];
                            int len;
                            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                                while ((len = inputStream.read(buf)) != -1) {
                                    outputStream.write(buf, 0, len);
                                }
                                Identity identity = IdentityUtils.parseIdentity(outputStream.toByteArray(), password.getText().toString());
                                XChatCore.loadIdentity(identity);
                                refresh();
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.setView(password);
                builder.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        unregisterForContextMenu(menuBtn);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        Identity identity = XChatCore.getIdentity();
        if (identity == null) {
            this.uidCode.setText("");
            this.nickname.setText("请加载身份");
            this.genderText.setText("");
            this.birthText.setText("");
            this.emailText.setText("");
            this.phoneText.setText("");
            return;
        }
        this.uidCode.setText(identity.getUidCode());
        this.nickname.setText(identity.getAttribute(UserAttributes.$NICK));
        this.genderText.setText(identity.getAttribute(UserAttributes.$GENDER));
        this.birthText.setText(identity.getAttribute(UserAttributes.$BIRTHDAY));
        this.emailText.setText(identity.getAttribute(UserAttributes.$EMAIL));
        this.phoneText.setText(identity.getAttribute(UserAttributes.$PHONE));
    }
}
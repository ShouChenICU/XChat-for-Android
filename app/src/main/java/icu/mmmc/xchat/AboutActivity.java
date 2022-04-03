package icu.mmmc.xchat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import icu.xchat.core.GlobalVariables;

public class AboutActivity extends AppCompatActivity {
    private void loadViews() {
        findViewById(R.id.backBtn).setOnClickListener(e -> finish());
        TextView version = findViewById(R.id.version);
        version.setText("0.2 alpha");
        TextView xchatCoreVersion = findViewById(R.id.xchat_core_version);
        xchatCoreVersion.setText(GlobalVariables.VERSION_STRING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        loadViews();
    }
}
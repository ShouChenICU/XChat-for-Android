package icu.mmmc.xchat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import icu.mmmc.xchat.adapters.RoomItemAdapter;
import icu.xchat.core.Configuration;
import icu.xchat.core.Identity;
import icu.xchat.core.XChatCore;
import icu.xchat.core.constants.UserAttributes;
import icu.xchat.core.entities.ChatRoom;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private TextView topNick;
    private TextView sideNickname;
    private ListView roomList;
    private RoomItemAdapter roomItemAdapter;

    private void loadViews() {
        this.drawerLayout = findViewById(R.id.main_activity);
        this.sideNickname = findViewById(R.id.nick);
        this.topNick = findViewById(R.id.topNick);
        this.roomList = findViewById(R.id.room_list);
        findViewById(R.id.topAvatar).setOnClickListener(e -> drawerLayout.openDrawer(GravityCompat.START));
        this.roomItemAdapter = new RoomItemAdapter(this, roomList);
        roomList.setAdapter(roomItemAdapter);
        findViewById(R.id.refresh).setOnClickListener(v -> roomItemAdapter.refresh());
        findViewById(R.id.userInfo).setOnClickListener(v -> {
            Intent intent = new Intent(this, IdentityManagerActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.server_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, ServerManagerActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.about_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btn_room).setOnClickListener(v -> showRoomList());
        findViewById(R.id.btn_server).setOnClickListener(v -> showServerList());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XChatCore.init(new Configuration());
        setContentView(R.layout.activity_main);
        loadViews();
        showRoomList();
        roomList.setOnItemClickListener((adapterView, view, i, l) -> {
            ChatRoom chatRoom = (ChatRoom) roomItemAdapter.getItem(i);
            ChatRoomActivity.setChatRoom(chatRoom);
            chatRoom.setUnprocessedMsgCount(0);
            roomItemAdapter.notifyDataSetChanged();
            Intent intent = new Intent(this, ChatRoomActivity.class);
            this.startActivity(intent);
        });
        verifyStoragePermissions(this);
    }

    private synchronized void showRoomList() {
        roomList.setVisibility(View.VISIBLE);
    }

    private synchronized void showServerList() {
        loadServerList();
    }

    private void loadServerList() {
        Intent intent = new Intent(this, ServerManagerActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Identity identity = XChatCore.getIdentity();
        if (identity != null) {
            topNick.setText(identity.getAttribute(UserAttributes.$NICK));
            sideNickname.setText(identity.getAttribute(UserAttributes.$NICK));
        } else {
            topNick.setText("请加载身份");
            sideNickname.setText("请加载身份");
        }
        roomItemAdapter.notifyDataSetChanged();
    }

    private final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int REQUEST_EXTERNAL_STORAGE = 1;
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
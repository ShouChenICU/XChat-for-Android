package icu.mmmc.xchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
        findViewById(R.id.userInfo).setOnClickListener(e -> {
            Intent intent = new Intent(this, IdentityManagerActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.server_btn).setOnClickListener(e -> {
            Intent intent = new Intent(this, ServerManagerActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.about_btn).setOnClickListener(e -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btn_room).setOnClickListener(e -> showRoomList());
        findViewById(R.id.btn_server).setOnClickListener(e -> showServerList());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.identity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
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
    }
}
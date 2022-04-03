package icu.mmmc.xchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import icu.xchat.core.constants.RoomAttributes;
import icu.xchat.core.entities.ChatRoom;

public class RoomDetailsActivity extends AppCompatActivity {
    private static ChatRoom chatRoom;
    private TextView roomName;

    private void loadViews() {
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.member_list_btn).setOnClickListener(v -> {
            MemberListActivity.setChatRoom(chatRoom);
            Intent intent = new Intent(this, MemberListActivity.class);
            startActivity(intent);
        });
        roomName = findViewById(R.id.room_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);
        loadViews();
        refresh();
    }

    private void refresh() {
        this.roomName.setText(chatRoom.getRoomInfo().getAttribute(RoomAttributes.$TITLE));
    }

    public static void setChatRoom(ChatRoom chatRoom) {
        RoomDetailsActivity.chatRoom = chatRoom;
    }
}
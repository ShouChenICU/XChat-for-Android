package icu.mmmc.xchat;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import icu.mmmc.xchat.adapters.MemberItemAdapter;
import icu.xchat.core.entities.ChatRoom;

public class MemberListActivity extends AppCompatActivity {
    private static ChatRoom chatRoom;
    private ListView memberList;
    private MemberItemAdapter memberItemAdapter;

    private void loadViews() {
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        memberList = findViewById(R.id.member_list);
        memberItemAdapter = new MemberItemAdapter(this, chatRoom);
        memberList.setAdapter(memberItemAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        loadViews();
    }

    public static void setChatRoom(ChatRoom chatRoom) {
        MemberListActivity.chatRoom = chatRoom;
    }
}
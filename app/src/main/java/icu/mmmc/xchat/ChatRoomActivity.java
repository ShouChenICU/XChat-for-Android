package icu.mmmc.xchat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import icu.mmmc.xchat.adapters.MessageItemAdapter;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.adapters.ProgressAdapter;
import icu.xchat.core.callbacks.interfaces.UpdateMessageCallBack;
import icu.xchat.core.constants.RoomAttributes;
import icu.xchat.core.entities.ChatRoom;

public class ChatRoomActivity extends AppCompatActivity {
    private static ChatRoom chatRoom;
    private UpdateMessageCallBack callBack;
    private MessageItemAdapter messageItemAdapter;
    private TextView roomName;
    private ListView msgList;
    private EditText input;
    private int dH;

    private void loadViews() {
        this.roomName = findViewById(R.id.roomName);
        this.msgList = findViewById(R.id.msg_list);
        this.input = findViewById(R.id.input);
        findViewById(R.id.send_btn).setOnClickListener(e -> {
            if (input.getText().toString().isEmpty()) {
                return;
            }
            XChatCore.pushMessage(input.getText().toString(), chatRoom.getServerCode(), chatRoom.getRid(), new ProgressAdapter() {
                @Override
                public void completeProgress() {
                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "发送成功", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void terminate(String errMsg) {
                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, errMsg, Toast.LENGTH_SHORT).show());
                }
            });
        });
        findViewById(R.id.backBtn).setOnClickListener(e -> finish());
        messageItemAdapter = new MessageItemAdapter(this, msgList, chatRoom);
        msgList.setAdapter(messageItemAdapter);
        msgList.setSelection(messageItemAdapter.getCount());
        View rootView = findViewById(R.id.root_view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int dh = rootView.getRootView().getHeight() - rootView.getHeight();
            if (dh > 200 && dh != dH) {
                msgList.setSelection(messageItemAdapter.getCount());
                dH = dh;
            } else if (dh <= 200) {
                dH = 0;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        this.callBack = chatRoom.getUpdateMessageCallBack();
        loadViews();
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatRoom.setUnprocessedMsgCount(0);
        chatRoom.setUpdateMessageCallBack(callBack);
        callBack.updateMessage(chatRoom.getLastMessage());
    }

    public static void setChatRoom(ChatRoom chatRoom) {
        ChatRoomActivity.chatRoom = chatRoom;
    }

    public void refresh() {
        roomName.setText(chatRoom.getRoomInfo().getAttribute(RoomAttributes.$TITLE));
    }
}
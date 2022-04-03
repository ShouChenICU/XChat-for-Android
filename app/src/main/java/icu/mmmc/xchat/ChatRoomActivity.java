package icu.mmmc.xchat;

import android.content.Intent;
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
    private TextView sendBtn;
    private int dH;

    private void loadViews() {
        this.roomName = findViewById(R.id.roomName);
        this.msgList = findViewById(R.id.msg_list);
        this.input = findViewById(R.id.input);
        this.sendBtn = findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(e -> {
            if (input.getText().toString().isEmpty()) {
                return;
            }
            sendBtn.setEnabled(false);
            XChatCore.pushMessage(input.getText().toString(), chatRoom.getServerCode(), chatRoom.getRid(), new ProgressAdapter() {
                @Override
                public void completeProgress() {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatRoomActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                        input.setText("");
                        sendBtn.setEnabled(true);
                    });
                }

                @Override
                public void terminate(String errMsg) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatRoomActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                        sendBtn.setEnabled(true);
                    });
                }
            });
        });
        findViewById(R.id.backBtn).setOnClickListener(e -> finish());
        findViewById(R.id.details_btn).setOnClickListener(v -> {
            RoomDetailsActivity.setChatRoom(chatRoom);
            Intent intent = new Intent(this, RoomDetailsActivity.class);
            startActivity(intent);
        });
        messageItemAdapter = new MessageItemAdapter(this, msgList, chatRoom);
        msgList.setAdapter(messageItemAdapter);
        msgList.setSelection(messageItemAdapter.getCount());
        findViewById(R.id.clear).setOnClickListener(v -> {
            chatRoom.clearMessage();
            messageItemAdapter.notifyDataSetChanged();
        });
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
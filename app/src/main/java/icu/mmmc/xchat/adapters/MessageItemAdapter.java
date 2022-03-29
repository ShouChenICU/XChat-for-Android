package icu.mmmc.xchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import icu.mmmc.xchat.ChatRoomActivity;
import icu.mmmc.xchat.R;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.adapters.ProgressAdapter;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.MessageInfo;

public class MessageItemAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final ListView listView;
    private final View topSyncMsgView;
    private final ChatRoom chatRoom;
    private final List<MessageInfo> messageInfoList;

    public MessageItemAdapter(Context context, ListView listView, ChatRoom chatRoom) {
        this.mContext = context;
        this.listView = listView;
        this.mInflater = LayoutInflater.from(mContext);
        this.chatRoom = chatRoom;
        this.messageInfoList = chatRoom.getMessageList();
        this.topSyncMsgView = mInflater.inflate(R.layout.message_list_item_top, null);
        TextView textView = topSyncMsgView.findViewById(R.id.sync_msg);
        textView.setOnClickListener(e -> {
            long time;
            if (messageInfoList.isEmpty()) {
                time = System.currentTimeMillis();
            } else {
                time = messageInfoList.get(0).getTimeStamp();
            }
            XChatCore.Tasks.syncMessage(chatRoom.getServerCode(), chatRoom.getRid(), time, 10, new ProgressAdapter() {
                @Override
                public void completeProgress() {
                    ((ChatRoomActivity) mContext).runOnUiThread(() -> Toast.makeText(mContext, "消息同步完成！", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void terminate(String errMsg) {
                    ((ChatRoomActivity) mContext).runOnUiThread(() -> Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT).show());
                }
            });
        });
        chatRoom.setUpdateMessageCallBack(msg -> {
            int i = listView.getLastVisiblePosition();
            if (i >= messageInfoList.size() - 2) {
                listView.post(() -> {
                    notifyDataSetChanged();
                    listView.smoothScrollToPosition(messageInfoList.size() + 1);
                });
            } else {
                listView.post(this::notifyDataSetChanged);
            }
        });
    }

    @Override
    public int getCount() {
        return messageInfoList.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return messageInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RelativeLayout layout;
        if (Objects.equals(i, 0)) {
            return topSyncMsgView;
        }
        i--;
        ItemTag itemTag;
        MessageInfo messageInfo = messageInfoList.get(i);
        itemTag = new ItemTag();
        if (XChatCore.getIdentity() != null && Objects.equals(XChatCore.getIdentity().getUidCode(), messageInfo.getSender())) {
            layout = (RelativeLayout) mInflater.inflate(R.layout.message_list_item_right, null);
            itemTag.isMe = true;
        } else {
            layout = (RelativeLayout) mInflater.inflate(R.layout.message_list_item_left, null);
            itemTag.isMe = false;
        }
        itemTag.avatar = layout.findViewById(R.id.avatar);
        itemTag.nickname = layout.findViewById(R.id.nickname);
        itemTag.content = layout.findViewById(R.id.content);
        layout.setTag(itemTag);
        itemTag.content.setText(messageInfo.getContent());
        itemTag.nickname.setText(String.valueOf(itemTag.isMe));
        return layout;
    }

    private static class ItemTag {
        public boolean isMe;
        public ImageView avatar;
        public TextView nickname;
        public TextView content;
    }
}

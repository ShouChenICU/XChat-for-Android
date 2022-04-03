package icu.mmmc.xchat.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import icu.mmmc.xchat.R;
import icu.xchat.core.UserInfoManager;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.adapters.ProgressAdapter;
import icu.xchat.core.constants.UserAttributes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.MessageInfo;
import icu.xchat.core.entities.UserInfo;

public class MessageItemAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Activity activity;
    private final ListView listView;
    private final ChatRoom chatRoom;
    private final List<MessageInfo> messageInfoList;

    public MessageItemAdapter(Activity activity, ListView listView, ChatRoom chatRoom) {
        this.activity = activity;
        this.listView = listView;
        this.mInflater = LayoutInflater.from(activity);
        this.chatRoom = chatRoom;
        this.messageInfoList = chatRoom.getMessageList();

        chatRoom.setUpdateMessageCallBack(msg -> listView.post(() -> {
            int i = listView.getLastVisiblePosition();
            if (i >= messageInfoList.size() - 2) {
                notifyDataSetChanged();
                listView.smoothScrollToPosition(messageInfoList.size() + 1);
            } else {
                notifyDataSetChanged();
            }
        }));
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
        // TODO: 2022/3/29 待优化性能
        RelativeLayout layout;
        if (Objects.equals(i, 0)) {
            View topSyncMsgView = mInflater.inflate(R.layout.message_list_item_top, null);
            TextView syncBtn = topSyncMsgView.findViewById(R.id.sync_msg);
            syncBtn.setOnClickListener(e -> {
                syncBtn.setEnabled(false);
                syncBtn.setTextColor(activity.getResources().getColor(R.color.secondary, activity.getTheme()));
                long time;
                if (messageInfoList.isEmpty()) {
                    time = System.currentTimeMillis();
                } else {
                    time = messageInfoList.get(0).getTimeStamp();
                }
                XChatCore.Tasks.syncMessage(chatRoom.getServerCode(), chatRoom.getRid(), time, 10, new ProgressAdapter() {
                    @Override
                    public void completeProgress() {
                        listView.post(() -> {
                            Toast.makeText(activity, "消息同步完成！", Toast.LENGTH_SHORT).show();
                            syncBtn.setEnabled(true);
                            syncBtn.setTextColor(activity.getResources().getColor(R.color.success, activity.getTheme()));
                        });
                    }

                    @Override
                    public void terminate(String errMsg) {
                        listView.post(() -> {
                            Toast.makeText(activity, errMsg, Toast.LENGTH_SHORT).show();
                            syncBtn.setEnabled(true);
                            syncBtn.setTextColor(activity.getResources().getColor(R.color.success, activity.getTheme()));
                        });
                    }
                });
            });
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
        itemTag.time = layout.findViewById(R.id.time);
        itemTag.content = layout.findViewById(R.id.content);
        layout.setTag(itemTag);
        itemTag.content.setText(messageInfo.getContent());
        UserInfo userInfo = UserInfoManager.getUserInfo(messageInfo.getSender());
        if (userInfo != null) {
            String nick = userInfo.getAttribute(UserAttributes.$NICK);
            itemTag.nickname.setText(nick == null ? messageInfo.getSender() : nick);
        } else {
            itemTag.nickname.setText(messageInfo.getSender());
        }
        itemTag.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(messageInfo.getTimeStamp())));
        return layout;
    }

    private static class ItemTag {
        public boolean isMe;
        public ImageView avatar;
        public TextView nickname;
        public TextView time;
        public TextView content;
    }
}

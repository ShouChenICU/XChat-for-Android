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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import icu.mmmc.xchat.R;
import icu.xchat.core.XChatCore;
import icu.xchat.core.constants.RoomAttributes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.MessageInfo;

public class RoomItemAdapter extends BaseAdapter {
    private final Context mContext;
    private final ListView listView;
    private final LayoutInflater mInflater;
    private final List<ChatRoom> items;

    public RoomItemAdapter(Context context, ListView listView) {
        super();
        this.mContext = context;
        this.listView = listView;
        this.mInflater = LayoutInflater.from(mContext);
        this.items = new ArrayList<>();
        refresh();
    }

    public void refresh() {
        XChatCore.CallBack.updateRoomInfoCallBack = (roomInfo, serverCode) -> {
            ChatRoom chatRoom = XChatCore.Servers.getServer(serverCode).getChatRoom(roomInfo.getRid());
            synchronized (items) {
                items.removeIf(room -> Objects.equals(room.getServerCode(), serverCode) && Objects.equals(room.getRid(), roomInfo.getRid()));
                items.add(chatRoom);
                listView.post(this::notifyDataSetChanged);
            }
        };
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RelativeLayout layout;
        ItemTag itemTag;
        if (view == null) {
            layout = (RelativeLayout) mInflater.inflate(R.layout.room_list_item, null);
            itemTag = new ItemTag();
            itemTag.roomAvatar = layout.findViewById(R.id.room_avatar);
            itemTag.roomName = layout.findViewById(R.id.room_name);
            itemTag.roomMsg = layout.findViewById(R.id.room_msg);
            itemTag.unprocessedMsgCount = layout.findViewById(R.id.room_unprocessed);
            layout.setTag(itemTag);
        } else {
            layout = (RelativeLayout) view;
            itemTag = (ItemTag) view.getTag();
        }
        ChatRoom chatRoom = items.get(i);
        if (chatRoom != null) {
            itemTag.roomName.setText(chatRoom.getRoomInfo().getAttribute(RoomAttributes.$TITLE));
            MessageInfo messageInfo = chatRoom.getLastMessage();
            itemTag.roomMsg.setText(messageInfo == null ? "" : messageInfo.getContent());
            itemTag.unprocessedMsgCount.setText(String.valueOf(chatRoom.getUnprocessedMsgCount()));
            if (chatRoom.getUnprocessedMsgCount() <= 0) {
                itemTag.unprocessedMsgCount.setVisibility(View.INVISIBLE);
            } else {
                itemTag.unprocessedMsgCount.setVisibility(View.VISIBLE);
            }
            if (itemTag.chatRoom != null) {
                itemTag.chatRoom.setUpdateMessageCallBack(null);
            }
            itemTag.chatRoom = chatRoom;
            chatRoom.setUpdateMessageCallBack(((msg) -> listView.post(() -> {
                if (msg == null) {
                    return;
                }
                itemTag.roomMsg.setText(msg.getContent());
                itemTag.unprocessedMsgCount.setText(String.valueOf(chatRoom.getUnprocessedMsgCount()));
                if (chatRoom.getUnprocessedMsgCount() <= 0) {
                    itemTag.unprocessedMsgCount.setVisibility(View.INVISIBLE);
                } else {
                    itemTag.unprocessedMsgCount.setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            })));
        } else {
            itemTag.roomName.setText("");
        }
        return layout;
    }

    private static class ItemTag {
        public ImageView roomAvatar;
        public TextView roomName;
        public TextView roomMsg;
        public TextView unprocessedMsgCount;
        public ChatRoom chatRoom;
    }
}

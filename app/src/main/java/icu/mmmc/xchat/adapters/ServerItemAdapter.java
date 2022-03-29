package icu.mmmc.xchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import icu.mmmc.xchat.R;
import icu.xchat.core.XChatCore;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.net.Server;

public class ServerItemAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<ServerEntity> items;

    public ServerItemAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
    }

    public void addItem(ServerEntity serverEntity) {
        items.add(serverEntity);
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
        ServerEntity serverEntity = items.get(i);
        if (view == null) {
            layout = (RelativeLayout) mInflater.inflate(R.layout.server_list_item, null);
            itemTag = new ItemTag();
            itemTag.serverTitle = layout.findViewById(R.id.server_title);
            itemTag.status = layout.findViewById(R.id.status);
            layout.setTag(itemTag);
        } else {
            layout = (RelativeLayout) view;
            itemTag = (ItemTag) layout.getTag();
        }
        itemTag.serverTitle.setText(serverEntity.title);
        itemTag.serverEntity = serverEntity;
        ServerInfo serverInfo = serverEntity.serverInfo;
        Server server = XChatCore.Servers.getServer(serverInfo.getServerCode());
        if (server != null && Objects.equals(server.getServerInfo().getHost(), serverInfo.getHost()) && Objects.equals(server.getServerInfo().getPort(), serverInfo.getPort())) {
            itemTag.status.setText("在线");
            itemTag.status.setTextColor(mContext.getResources().getColor(R.color.success, mContext.getTheme()));
        } else {
            itemTag.status.setText("离线");
            itemTag.status.setTextColor(mContext.getResources().getColor(R.color.secondary, mContext.getTheme()));
        }
        return layout;
    }

    private static class ItemTag {
        public TextView serverTitle;
        public TextView status;
        public ServerEntity serverEntity;
    }

    public static class ServerEntity {
        public String title;
        public ServerInfo serverInfo;

        public ServerEntity(String title, ServerInfo serverInfo) {
            this.title = title;
            this.serverInfo = serverInfo;
        }
    }
}

package icu.mmmc.xchat.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import icu.mmmc.xchat.R;
import icu.xchat.core.UserInfoManager;
import icu.xchat.core.constants.UserAttributes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.MemberInfo;

public class MemberItemAdapter extends BaseAdapter {
    private Activity activity;
    private ChatRoom chatRoom;
    private List<MemberInfo> memberInfoList;

    public MemberItemAdapter(Activity activity, ChatRoom chatRoom) {
        this.activity = activity;
        this.chatRoom = chatRoom;
        memberInfoList = new ArrayList<>(chatRoom.getRoomInfo().getMemberInfoMap().values());
    }

    @Override
    public int getCount() {
        return memberInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return memberInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LinearLayout layout;
        ItemTag itemTag;
        if (view == null) {
            layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.member_list_item, null);
            itemTag = new ItemTag();
            itemTag.avatar = layout.findViewById(R.id.avatar);
            itemTag.role = layout.findViewById(R.id.role);
            itemTag.nickname = layout.findViewById(R.id.nickname);
            layout.setTag(itemTag);
        } else {
            layout = (LinearLayout) view;
            itemTag = (ItemTag) view.getTag();
        }
        MemberInfo memberInfo = memberInfoList.get(i);
        if (memberInfo.isOwner()) {
            itemTag.role.setVisibility(View.VISIBLE);
            itemTag.role.setTextColor(activity.getResources().getColor(R.color.success, activity.getTheme()));
        } else {
            itemTag.role.setVisibility(View.INVISIBLE);
        }
        itemTag.nickname.setText(UserInfoManager.getUserInfo(memberInfo.getUidCode()).getAttribute(UserAttributes.$NICK));
        return layout;
    }

    private static class ItemTag {
        public ImageView avatar;
        public TextView role;
        public TextView nickname;
    }
}

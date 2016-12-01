package cn.ucai.superwechat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMChatRoomChangeListener;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.domain.LiveRoom;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.widget.GridMarginDecoration;

/**
 * Created by clawpo on 2016/11/30.
 */

public class LiveListActivity extends BaseActivity {
    @BindView(R.id.recycleview)
    RecyclerView mRecycleview;
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.txt_title)
    TextView mTxtTitle;

    private boolean isLoading;
    private boolean isFirstLoading = true;
    private boolean hasMoreData = true;
    private String cursor;
    private final int pagesize = 10;
    private List<EMChatRoom> rooms;
    PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_live_list);
        ButterKnife.bind(this);
        rooms = new ArrayList<EMChatRoom>();
        loadAndShowData();
        initView();
        setListener();
    }

    private void setListener() {
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(new EMChatRoomChangeListener() {
            @Override
            public void onChatRoomDestroyed(String roomId, String roomName) {
                if (adapter != null) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                loadAndShowData();
                            }
                        }

                    });
                }
            }

            @Override
            public void onMemberJoined(String roomId, String participant) {
            }

            @Override
            public void onMemberExited(String roomId, String roomName,
                                       String participant) {

            }

            @Override
            public void onMemberKicked(String roomId, String roomName,
                                       String participant) {
            }

        });
    }

    private void initView() {
        mImgBack.setVisibility(View.VISIBLE);
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(getString(R.string.chat_room));
        mRecycleview.setHasFixedSize(true);
        mRecycleview.addItemDecoration(new GridMarginDecoration(6));
        mRecycleview.setAdapter(adapter);
    }

    private void loadAndShowData() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    isLoading = true;
                    final EMCursorResult<EMChatRoom> result = EMClient.getInstance().chatroomManager().fetchPublicChatRoomsFromServer(pagesize, cursor);
                    //get chat room list
                    final List<EMChatRoom> chatRooms = result.getData();
                    runOnUiThread(new Runnable() {

                        public void run() {
                            if (chatRooms.size() != 0) {
                                cursor = result.getCursor();
                            }
                            if (isFirstLoading) {
                                isFirstLoading = false;
//                                adapter = new PublicChatRoomsActivity.ChatRoomAdapter(PublicChatRoomsActivity.this, 1, chatRoomList);
                                adapter = new PhotoAdapter(LiveListActivity.this, getLiveRoomList(chatRooms));
                                mRecycleview.setAdapter(adapter);
                                rooms.addAll(chatRooms);
                            } else {
                                if (chatRooms.size() < pagesize) {
                                    hasMoreData = false;
                                }
                                adapter.notifyDataSetChanged();
                            }
                            isLoading = false;
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isLoading = false;
                            Toast.makeText(LiveListActivity.this, getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private List<LiveRoom> getLiveRoomList(List<EMChatRoom> chatRooms) {
        if (chatRooms != null && chatRooms.size() > 0) {

            List<LiveRoom> roomList = new ArrayList<>();
            for (EMChatRoom room : chatRooms) {
                LiveRoom liveRoom = new LiveRoom();
                liveRoom.setName(room.getName());
                liveRoom.setAudienceNum(room.getAffiliationsCount());
                liveRoom.setId(room.getId());
                liveRoom.setChatroomId(room.getId());
                liveRoom.setCover(R.drawable.default_hd_avatar);
                liveRoom.setAnchorId(room.getOwner());
                roomList.add(liveRoom);
            }

            return roomList;
        }
        return null;
    }

    @OnClick(R.id.txt_title)
    public void onBackClick() {
        MFGT.finish(this);
    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        private final List<LiveRoom> liveRoomList;
        private final Context context;

        public PhotoAdapter(Context context, List<LiveRoom> liveRoomList) {
            this.liveRoomList = liveRoomList;
            this.context = context;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final PhotoViewHolder holder = new PhotoViewHolder(LayoutInflater.from(context).
                    inflate(R.layout.layout_livelist_item, parent, false));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    LiveRoom r =  liveRoomList.get(position);
                    L.e("livelist","u="+EMClient.getInstance().getCurrentUser());
                    L.e("livelist","r="+r);
                    if(r.getAnchorId().equals(EMClient.getInstance().getCurrentUser())){
                        context.startActivity(new Intent(context, StartLiveActivity.class)
                                .putExtra("liveroom", liveRoomList.get(position))
                        .putExtra("userId",r.getChatroomId()));
                    }else {
                        context.startActivity(new Intent(context, LiveDetailsActivity.class)
                                .putExtra("liveroom", liveRoomList.get(position)));
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(PhotoViewHolder holder, int position) {
            LiveRoom liveRoom = liveRoomList.get(position);
            holder.anchor.setText(liveRoom.getName());
            holder.audienceNum.setText(liveRoom.getAudienceNum() + "人");
            Glide.with(context)
                    .load(liveRoomList.get(position).getCover())
                    .placeholder(R.color.placeholder)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return liveRoomList != null ? liveRoomList.size() : 0;
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo)
        ImageView imageView;
        @BindView(R.id.author)
        TextView anchor;
        @BindView(R.id.audience_num)
        TextView audienceNum;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

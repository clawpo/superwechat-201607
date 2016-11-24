package cn.ucai.superwechat.openlive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.openlive.model.ConstantApp;


/**
 * 管理直播页面的Activity
 */
public class LiveRoomActivity extends BaseActivity  {

    LiveRoomFragment mLiveRoomFragment;
    ChatFragment mChatFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {
        mLiveRoomFragment=new LiveRoomFragment();
        mChatFragment=new ChatFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layout_live_room, mLiveRoomFragment);
        ft.replace(R.id.layout_chat_room, mChatFragment);
        ft.commit();

        Intent i = getIntent();
        int cRole = i.getIntExtra(ConstantApp.ACTION_KEY_CROLE, 0);

        if (cRole == 0) {
            throw new RuntimeException("Should not reach here");
        }

        String roomName = i.getStringExtra(ConstantApp.ACTION_KEY_ROOM_NAME);

        Bundle bundle = new Bundle();
        bundle.putInt(ConstantApp.ACTION_KEY_CROLE, cRole);
        bundle.putString(ConstantApp.ACTION_KEY_ROOM_NAME,roomName);
        mLiveRoomFragment.setArguments(bundle);

    }

    @Override
    protected void deInitUIandEvent() {
    }
}

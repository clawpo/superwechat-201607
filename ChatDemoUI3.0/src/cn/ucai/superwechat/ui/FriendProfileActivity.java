package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;


public class FriendProfileActivity extends BaseActivity {
    private static final String TAG = FriendProfileActivity.class.getSimpleName();

    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.txt_title)
    TextView mTxtTitle;
    @BindView(R.id.profile_image)
    ImageView mProfileImage;
    @BindView(R.id.tv_userinfo_nick)
    TextView mTvUserinfoNick;
    @BindView(R.id.tv_userinfo_name)
    TextView mTvUserinfoName;
    String username = null;
    User user = null;
    @BindView(R.id.btn_add_contact)
    Button mBtnAddContact;
    @BindView(R.id.btn_send_msg)
    Button mBtnSendMsg;
    @BindView(R.id.btn_send_video)
    Button mBtnSendVideo;
    boolean isFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);
        username = getIntent().getStringExtra(I.User.USER_NAME);
        if (username == null) {
            MFGT.finish(this);
            return;
        }
        initView();
        user = SuperWeChatHelper.getInstance().getAppContactList().get(username);
        if(user==null){
            isFriend = false;
        }else{
            setUserInfo();
            isFriend = true;
        }
        isFriend(isFriend);
        syncUserInfo();
    }

    private void syncFail(){
        if(!isFriend) {
            MFGT.finish(this);
            return;
        }
    }

    private void syncUserInfo() {
        NetDao.syncUserInfo(this, username, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s!=null){
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if(result!=null && result.isRetMsg()){
                        User u  = (User) result.getRetData();
                        if(u!=null){
                            L.e(TAG,"u="+u.getAvatar());
                            if(isFriend){
                                SuperWeChatHelper.getInstance().saveAppContact(u);
                            }
                            user = u;
                            setUserInfo();
                        }else{
                            syncFail();
                        }
                    }else{
                        syncFail();
                    }
                }else{
                    syncFail();
                }
            }

            @Override
            public void onError(String error) {
                syncFail();
            }
        });
    }

    private void initView() {
        mImgBack.setVisibility(View.VISIBLE);
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(getString(R.string.userinfo_txt_profile));
    }

    private void isFriend(boolean isFriend) {
        if (isFriend) {
            mBtnSendMsg.setVisibility(View.VISIBLE);
            mBtnSendVideo.setVisibility(View.VISIBLE);
        } else {
            mBtnAddContact.setVisibility(View.VISIBLE);
        }
    }

    private void setUserInfo() {
        L.e(TAG,"setUserInfo,user="+user.getAvatar());
        EaseUserUtils.setAppUserAvatar(this, user.getMUserName(), mProfileImage);
        EaseUserUtils.setAppUserNick(user.getMUserNick(), mTvUserinfoNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserName(), mTvUserinfoName);
    }

    @OnClick({R.id.img_back, R.id.btn_add_contact, R.id.btn_send_msg, R.id.btn_send_video})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(this);
                break;
            case R.id.btn_add_contact:
                MFGT.gotoAddFirendMsg(this,user.getMUserName());
                break;
            case R.id.btn_send_msg:
                MFGT.gotoChat(this,user.getMUserName());
                break;
            case R.id.btn_send_video:
                if (!EMClient.getInstance().isConnected())
                    Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
                else {
                    startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", user.getMUserName())
                            .putExtra("isComingCall", false));
                    // videoCallBtn.setEnabled(false);
                }
                break;
        }
    }
}

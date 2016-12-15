package cn.ucai.superwechat.live.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.live.data.model.Gift;

/**
 * Created by wei on 2016/6/7.
 */
@RemoteViews.RemoteView
public class LiveLeftGiftView extends RelativeLayout {
    @BindView(R.id.avatar)
    EaseImageView avatar;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.gift_image)
    ImageView giftImage;
    @BindView(R.id.gift_name)
    TextView mGiftName;

    public LiveLeftGiftView(Context context) {
        super(context);
        init(context, null);
    }

    public LiveLeftGiftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public LiveLeftGiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.widget_left_gift, this);
        ButterKnife.bind(this);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setAvatar(String avatar) {
        EaseUserUtils.setAppUserAvatar(getContext(), avatar, this.avatar);
//        Glide.with(getContext()).load(avatar).into(this.avatar);
    }

    public ImageView getGiftImageView() {
        return giftImage;
    }

    public void setGift(int gid){
        Gift gift = SuperWeChatHelper.getInstance().getAppGiftList().get(gid);
        if(gid!=0 && gift!=null){
            mGiftName.setText("送了一个"+gift.getGname());
            giftImage.setImageResource(getGiftImageRes(gid));
        }
    }
    private int getGiftImageRes(int id) {
        Context context = SuperWeChatApplication.getInstance().getApplicationContext();
        String name = "hani_gift_"+id;
        int resId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());
        return resId;
    }
}

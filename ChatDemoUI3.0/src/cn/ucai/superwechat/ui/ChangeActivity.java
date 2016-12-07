package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.domain.Wallet;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Created by clawpo on 2016/12/7.
 */

public class ChangeActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.txt_title)
    TextView mTxtTitle;
    @BindView(R.id.tv_change_balance)
    TextView mTvChangeBalance;

    @BindView(R.id.target_layout)
    LinearLayout contentContainer;

    private View loadingView;
    int change;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_change);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        //add loading view
        loadingView = LayoutInflater.from(this).inflate(R.layout.rp_loading, contentContainer, false);
        contentContainer.addView(loadingView);
        change = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentUserChange();
        setChangeText(change);

        NetDao.getBalance(this, EMClient.getInstance().getCurrentUser(), new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s!=null){
                    Result result = ResultUtils.getResultFromJson(s, Wallet.class);
                    if(result!=null && result.isRetMsg()){
                        Wallet wallet = (Wallet) result.getRetData();
                        if(wallet!=null) {
                            setChangeText(wallet.getBalance());
                            SuperWeChatHelper.getInstance().getUserProfileManager().setCurrentUserChange(wallet.getBalance());
                        }else{
                            setChangeText(0);
                        }
                    }
                }
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    private void setChangeText(int balance) {
        mTvChangeBalance.setText("¥ "+ Float.parseFloat(String.valueOf(balance)));
    }

    private void initView() {
        mImgBack.setVisibility(View.VISIBLE);
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(R.string.title_small_change);
    }

    @OnClick(R.id.img_back)
    public void onBackClick() {
        MFGT.finish(this);
    }
}

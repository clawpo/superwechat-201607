package cn.ucai.superwechat.live.ui.activity;

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
import cn.ucai.superwechat.live.data.model.Wallet;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Created by clawpo on 2016/12/15.
 */

public class ChangeActivity extends cn.ucai.superwechat.ui.BaseActivity {
    @BindView(R.id.left_image)
    ImageView mLeftImage;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.subtitle)
    TextView mSubtitle;
    @BindView(R.id.tv_change_balance)
    TextView mTvChangeBalance;

    @BindView(R.id.target_layout)
    LinearLayout contentContainer;

    private View loadingView;
    int change;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.fragment_change);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        //add loading view
        loadingView = LayoutInflater.from(this).inflate(R.layout.rp_loading, contentContainer, false);
        contentContainer.addView(loadingView);
        change = Integer.parseInt(SuperWeChatHelper.getInstance().getCurrentUsernChange());
        setChangeText(change);

        NetDao.loadChange(this, EMClient.getInstance().getCurrentUser(), new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, Wallet.class);
                    if (result != null && result.isRetMsg()) {
                        Wallet wallet = (Wallet) result.getRetData();
                        if (wallet != null) {
                            setChangeText(wallet.getBalance());
                            SuperWeChatHelper.getInstance().setCurrentUserChange(wallet.getBalance().toString());
                        } else {
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

    private void initView() {
        mLeftImage.setImageResource(R.drawable.rp_back_arrow_yellow);
        mTitle.setText(R.string.change);
        mSubtitle.setText("云账号零钱服务");
        mTvChangeBalance.setText("¥ 0.00");
    }

    private void setChangeText(int balance) {
        mTvChangeBalance.setText("¥ " + Float.parseFloat(String.valueOf(balance)));
    }

    @OnClick({R.id.left_layout, R.id.tv_change_recharge, R.id.tv_change_withdraw})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.left_layout:
                MFGT.finish(this);
                break;
            case R.id.tv_change_recharge:
                break;
            case R.id.tv_change_withdraw:
                break;
        }
    }
}

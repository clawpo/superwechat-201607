package cn.ucai.superwechat.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.domain.Gift;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Created by clawpo on 2016/12/2.
 */

public class RoomGiftListDialog extends DialogFragment {
    private static final String TAG = RoomGiftListDialog.class.getSimpleName();
    Unbinder unbinder;
    @BindView(R.id.rv_gift)
    RecyclerView mRvGift;
    @BindView(R.id.tv_my_bill)
    TextView mTvMyBill;
    @BindView(R.id.tv_recharge)
    TextView mTvRecharge;

    private String username;
    List<Gift> mGiftList;
    GiftAdapter mAdapter;
    GridLayoutManager glm;

    public static RoomGiftListDialog newInstance(String username) {
        RoomGiftListDialog dialog = new RoomGiftListDialog();
//        Bundle args = new Bundle();
//        args.putString("username", username);
//        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gift_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glm = new GridLayoutManager(getContext(), 4);
        mRvGift.setLayoutManager(glm);
        mRvGift.setHasFixedSize(true);
        mGiftList = new ArrayList<>();
        mAdapter = new GiftAdapter(getActivity(),mGiftList);
        mRvGift.setAdapter(mAdapter);
        NetDao.getAllGift(getContext(), new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s!=null){
                    Result result = ResultUtils.getListResultFromJson(s, Gift.class);
                    if(result!=null){
                        if(result.isRetMsg()){
                            List<Gift> list = (List<Gift>) result.getRetData();
                            L.e(TAG,"list="+list);
                            mAdapter.initData(list);
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {

            }
        });
//        if (username != null) {
//            usernameView.setText(username);
//            EaseUserUtils.setAppUserNick(username, usernameView);
//        }
//        mentionBtn.setText("@TA");
//        EaseUserUtils.setAppUserAvatar(getActivity(), username, mIvUseravatar);
    }

//    @OnClick(R.id.btn_message) void onMessageBtnClick(){
//        ChatFragment fragment = ChatFragment.newInstance(username, false);
//        dismiss();
//        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.message_container, fragment).commit();
//    }

//    @OnClick(R.id.btn_mentions)
//    void onMentionBtnClick() {
//        if (dialogListener != null) {
//            dialogListener.onMentionClick(username);
//        }
//    }
//
//    @OnClick(R.id.btn_follow)
//    void onFollowBtnClick() {
//    }

    private UserDetailsDialogListener dialogListener;

    public void setUserDetailsDialogListener(UserDetailsDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    interface UserDetailsDialogListener {
        void onMentionClick(String username);
    }

    private View.OnClickListener mListener;

    public void setGiftOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 使用不带theme的构造器，获得的dialog边框距离屏幕仍有几毫米的缝隙。
        // Dialog dialog = new Dialog(getActivity());
        Dialog dialog = new Dialog(getActivity(), R.style.room_user_details_dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // must be called before set content
        dialog.setContentView(R.layout.fragment_room_user_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class GiftAdapter extends Adapter {
        Context mContext;
        List<Gift> mList;
        boolean isMore;

        public GiftAdapter(Context context, List<Gift> list) {
            mContext = context;
            mList = new ArrayList<>();
            mList.addAll(list);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = null;
            holder = new GiftViewHolder(View.inflate(mContext, R.layout.item_gift, null));
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Gift gift = mList.get(position);
            GiftViewHolder gh = (GiftViewHolder)holder;
            gh.mTvGiftName.setText(gift.getGname());
            gh.mTvGiftPrice.setText(String.valueOf(gift.getGprice()));
            EaseUserUtils.setAppUserPathAvatar(getActivity(),gift.getGurl(),gh.mIvGiftThumb);
            gh.mLayoutGoods.setTag(gift.getId());
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        public void initData(List<Gift> list) {
            if (mList != null) {
                mList.clear();
            }
            mList.addAll(list);
            notifyDataSetChanged();
        }

        public void addData(List<Gift> list) {
            mList.addAll(list);
            notifyDataSetChanged();
        }

        class GiftViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.ivGiftThumb)
            ImageView mIvGiftThumb;
            @BindView(R.id.tvGiftName)
            TextView mTvGiftName;
            @BindView(R.id.tvGiftPrice)
            TextView mTvGiftPrice;
            @BindView(R.id.layout_gift)
            LinearLayout mLayoutGoods;

            GiftViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mLayoutGoods.setOnClickListener(mListener);
            }
        }
    }
}

package cn.ucai.superwechat.live.ui.activity;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.live.data.model.Gift;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Created by wei on 2016/7/25.
 */
public class GiftListDialog extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.rv_gift)
    RecyclerView mRvGift;
    @BindView(R.id.tv_my_bill)
    TextView mTvMyBill;
    @BindView(R.id.tv_recharge)
    TextView mTvRecharge;

    GridLayoutManager gm;
    List<Gift> mGiftList = new ArrayList<>();
    GiftAdapter mAdapter;


    public static GiftListDialog newInstance() {
        GiftListDialog dialog = new GiftListDialog();
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gift_list_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        loadGiftList();
    }

    private void initView() {
        gm = new GridLayoutManager(getContext(),4);
        mRvGift.setLayoutManager(gm);
        mAdapter = new GiftAdapter(getContext(),mGiftList);
        mRvGift.setAdapter(mAdapter);
    }

    private void loadGiftList() {
        Map<Integer, Gift> appGiftList = SuperWeChatHelper.getInstance().getAppGiftList();
        if(appGiftList!=null && !appGiftList.isEmpty()){
            L.e("main","map="+appGiftList.size());
            final List<Gift> allGiftList = new ArrayList<Gift>();
            for (Gift gift : appGiftList.values()) {
                allGiftList.add(gift);
            }
            mAdapter.initData(allGiftList);
        }else {
            NetDao.loadGiftList(getContext(), new OkHttpUtils.OnCompleteListener<String>() {
                @Override
                public void onSuccess(String s) {
                    Result result = ResultUtils.getListResultFromJson(s, Gift.class);
                    if (result != null && result.isRetMsg()) {
                        List<Gift> list = (List<Gift>) result.getRetData();
                        if (list != null && list.size() > 0) {
                            mAdapter.initData(list);
                            SuperWeChatHelper.getInstance().updateAppGiftList(list);
                        }
                    }
                }

                @Override
                public void onError(String error) {

                }
            });
        }
    }



    View.OnClickListener mListener;
    public void setGiftDialogListener(View.OnClickListener dialogListener) {
        this.mListener = dialogListener;
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
            gh.mIvGiftThumb.setImageResource(getGiftImageRes(gift.getId()));
//            EaseUserUtils.setAppUserPathAvatar(getActivity(),gift.getGurl(),gh.mIvGiftThumb);
            gh.mLayoutGoods.setTag(gift.getId());
            gh.itemView.setOnClickListener(mListener);
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        public void initData(List<Gift> list) {
            if (mList != null) {
                mList.clear();
            }
            Collections.sort(list, new Comparator<Gift>() {
                @Override
                public int compare(Gift lhs, Gift rhs) {
                    return lhs.getGprice()-rhs.getGprice();
                }
            });
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
//                mLayoutGoods.setOnClickListener(mListener);
            }
        }
    }
    private int getGiftImageRes(int id) {
        Context context = SuperWeChatApplication.getInstance().getApplicationContext();
        String name = "hani_gift_"+id;
        int resId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());
        return resId;
    }
}

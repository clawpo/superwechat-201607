package cn.ucai.superwechat.openlive.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import cn.ucai.superwechat.openlive.model.VideoStatusData;


/**
 * 管理直播屏幕的RecyclerView
 */
public class GridVideoViewContainer extends RecyclerView {
    public GridVideoViewContainer(Context context) {
        super(context);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 适配器类
     */
    private GridVideoViewContainerAdapter mGridVideoViewContainerAdapter;

    private VideoViewEventListener mEventListener;

    public void setItemEventHandler(VideoViewEventListener listener) {
        this.mEventListener = listener;
    }

    /**
     * 创建适配器并返回是否是新建的适配器
     * @param localUid
     * @param uids
     * @return
     */
    private boolean initAdapter(int localUid, HashMap<Integer, SoftReference<SurfaceView>> uids) {
        if (mGridVideoViewContainerAdapter == null) {
            mGridVideoViewContainerAdapter = new GridVideoViewContainerAdapter(getContext(), localUid, uids, mEventListener);
            mGridVideoViewContainerAdapter.setHasStableIds(true);
            return true;
        }
        return false;
    }

    public void initViewContainer(Context context, int localUid, HashMap<Integer, SoftReference<SurfaceView>> uids) {
        boolean newCreated = initAdapter(localUid, uids);

        if (!newCreated) {// 若不是新建的适配器
            mGridVideoViewContainerAdapter.setLocalUid(localUid);//设置本地窗口的uid
            //初始化一组SurfaceView，添加新赠的窗口，删除新删除的窗口
            mGridVideoViewContainerAdapter.init(uids, localUid, true);
        }

        this.setAdapter(mGridVideoViewContainerAdapter);

        int count = uids.size();
        if (count <= 2) { // only local full view or or with one peer
            this.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        } else if (count > 2 && count <= 4) {
            this.setLayoutManager(new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false));
        }

        mGridVideoViewContainerAdapter.notifyDataSetChanged();
    }

    public SurfaceView getSurfaceView(int index) {
        return mGridVideoViewContainerAdapter.getItem(index).mView.get();
    }

    public VideoStatusData getItem(int position) {
        return mGridVideoViewContainerAdapter.getItem(position);
    }
}

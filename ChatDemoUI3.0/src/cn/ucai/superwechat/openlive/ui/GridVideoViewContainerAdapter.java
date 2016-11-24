package cn.ucai.superwechat.openlive.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.openlive.model.ConstantApp;
import cn.ucai.superwechat.openlive.model.VideoStatusData;


public class GridVideoViewContainerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static Logger log = LoggerFactory.getLogger(GridVideoViewContainerAdapter.class);

    protected final LayoutInflater mInflater;
    protected final Context mContext;

    protected final VideoViewEventListener mListener;

    /**
     * 列表项集合，每个列表项是一个显示用户视频的SurfaceView
     */
    private ArrayList<VideoStatusData> mUsers;

    /**
     * 显示直播的RecyclerView的适配器类构造方法
     * @param context
     * @param localUid：
     * @param uids：列表项集合，元素类型是SurfaceView
     * @param listener：列表项双击事件监听器
     */
    public GridVideoViewContainerAdapter(Context context, int localUid,
                 HashMap<Integer, SoftReference<SurfaceView>> uids, VideoViewEventListener listener) {
        mContext = context;
        mInflater = ((Activity) context).getLayoutInflater();

        mListener = listener;

        mUsers = new ArrayList<>();

        init(uids, localUid, false);
    }

    protected int mItemWidth;
    protected int mItemHeight;

    /** 本机视频的id是SurfaceView的key*/
    private int mLocalUid;

    public void setLocalUid(int uid) {
        mLocalUid = uid;
    }

    public int getLocalUid() {
        return mLocalUid;
    }

    /**
     * 初始化一组SurfaceView，添加新赠的窗口，删除新删除的窗口
     * @param uids：屏幕上所有显示的视频SurfaceView
     * @param localUid：显示本机SurfaceView的key
     * @param force
     */
    public void init(HashMap<Integer, SoftReference<SurfaceView>> uids, int localUid, boolean force) {
        for (HashMap.Entry<Integer, SoftReference<SurfaceView>> entry : uids.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == mLocalUid) { //若是第一个窗口或是本机的窗口
                boolean found = false;
                for (VideoStatusData status : mUsers) {
                    if ((status.mUid == entry.getKey() && status.mUid == 0) || status.mUid == mLocalUid) { // first time
                        status.mUid = mLocalUid;
                        found = true;
                        break;
                    }
                }
                if (!found) {//若没找到，则添加新的窗口
                    mUsers.add(0, new VideoStatusData(mLocalUid, entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
                }
            } else { // 若key>0
                boolean found = false;
                for (VideoStatusData status : mUsers) {
                    if (status.mUid == entry.getKey()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {//若之前没有该窗口，则添加
                    mUsers.add(new VideoStatusData(entry.getKey(), entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
                }
            }
        }

        //遍历之前的所有窗口，删除新去掉的窗口
        //之前的窗口保存在mUsers集合中，新的窗口保存在uids集合中
        Iterator<VideoStatusData> it = mUsers.iterator();
        while (it.hasNext()) {
            VideoStatusData status = it.next();

            if (uids.get(status.mUid) == null) {
                log.warn("after_changed remove not exited members " + (status.mUid & 0xFFFFFFFFL) + " " + status.mView);
                it.remove();
            }
        }

        /**
         * 显示多个画面
         */
        if (force || mItemWidth == 0 || mItemHeight == 0) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);

            int count = uids.size();
            int DividerX = 1;
            int DividerY = 1;
            if (count == 2) {//若是两个用户视频
                DividerY = 2;
            } else if (count >= 3) {//若超过3个用户视频
                DividerX = 2;
                DividerY = 2;
            }
            //设置每个用户顺逆的宽高
            mItemWidth = outMetrics.widthPixels / DividerX;
            mItemHeight = outMetrics.heightPixels / DividerY;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.video_view_container, parent, false);
        v.getLayoutParams().width = mItemWidth;
        v.getLayoutParams().height = mItemHeight;
        return new VideoUserStatusHolder(v);
    }

    /**
     * 将指定用户的窗口删除
     * @param view
     */
    protected final void stripSurfaceView(SurfaceView view) {
        ViewParent parent = view.getParent();
        if (parent != null) {//若果有父容器，则先中父容器中删除SurfaceView
            Log.e("main", "parent).removeView(view)");
            ((FrameLayout) parent).removeView(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoUserStatusHolder myHolder = ((VideoUserStatusHolder) holder);

        final VideoStatusData user = mUsers.get(position);

        log.debug("onBindViewHolder " + position + " " + user + " " + myHolder + " " + myHolder.itemView);

        FrameLayout holderView = (FrameLayout) myHolder.itemView;

        //设置列表项双击事件监听
        holderView.setOnTouchListener(new OnDoubleTapListener(mContext) {
            @Override
            public void onDoubleTap(View view, MotionEvent e) {
                if (mListener != null) {//全屏和小屏切换
                    mListener.onItemDoubleClick(view, user);
                }
            }

            @Override
            public void onSingleTapUp() {
            }
        });

        if (holderView.getChildCount() == 0) {
            SurfaceView target = user.mView.get();
            stripSurfaceView(target);//若target有父容器，则先中父容器中删除target
            holderView.addView(target, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public int getItemCount() {
        int sizeLimit = mUsers.size();
        if (sizeLimit >= ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        return sizeLimit;
    }

    public VideoStatusData getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        VideoStatusData user = mUsers.get(position);

        SurfaceView view = user.mView.get();
        if (view == null) {
            throw new NullPointerException("SurfaceView destroyed for user " + user.mUid + " " + user.mStatus + " " + user.mVolume);
        }

        return (String.valueOf(user.mUid) + System.identityHashCode(view)).hashCode();
    }
}

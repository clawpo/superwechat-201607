package cn.ucai.superwechat.openlive.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import cn.ucai.superwechat.openlive.model.VideoStatusData;
import cn.ucai.superwechat.utils.L;


public class SmallVideoViewAdapter extends VideoViewAdapter {

    public SmallVideoViewAdapter(Context context, int exceptedUid, HashMap<Integer, SoftReference<SurfaceView>> uids, VideoViewEventListener listener) {
        super(context, exceptedUid, uids, listener);
    }

    @Override
    protected void customizedInit(HashMap<Integer, SoftReference<SurfaceView>> uids, boolean force) {
        for (HashMap.Entry<Integer, SoftReference<SurfaceView>> entry : uids.entrySet()) {
            if (entry.getKey() != exceptedUid) {
                entry.getValue().get().setZOrderOnTop(true);
                entry.getValue().get().setZOrderMediaOverlay(true);
                mUsers.add(new VideoStatusData(entry.getKey(), entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
            }
        }

        if (force || mItemWidth == 0 || mItemHeight == 0) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            mItemWidth = outMetrics.widthPixels / 4;
            mItemHeight = outMetrics.heightPixels / 4;
        }
    }

    @Override
    public void notifyUiChanged(HashMap<Integer, SoftReference<SurfaceView>> uids, int uidExcluded, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume) {
        mUsers.clear();

        for (HashMap.Entry<Integer, SoftReference<SurfaceView>> entry : uids.entrySet()) {
            L.e("notifyUiChanged " + entry.getKey() + " " + uidExcluded);

            if (entry.getKey() != uidExcluded) {
                entry.getValue().get().setZOrderOnTop(true);
                entry.getValue().get().setZOrderMediaOverlay(true);
                mUsers.add(new VideoStatusData(entry.getKey(), entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
            }
        }

        notifyDataSetChanged();
    }

    public int getExceptedUid() {
        return exceptedUid;
    }
}

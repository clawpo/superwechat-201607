package cn.ucai.superwechat.openlive.ui;


import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.openlive.model.AGEventHandler;
import cn.ucai.superwechat.openlive.model.ConstantApp;
import cn.ucai.superwechat.openlive.model.EngineConfig;
import cn.ucai.superwechat.openlive.model.MyEngineEventHandler;
import cn.ucai.superwechat.openlive.model.VideoStatusData;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * 管理直播窗口
 */
public class LiveRoomFragment extends Fragment implements AGEventHandler {
    /**
     * 显示直播视频的RecyclerView
     */
    private GridVideoViewContainer mGridVideoViewContainer;

    private RelativeLayout mSmallVideoViewDock;

    // uid = 0 || uid == EngineConfig.mUid
    private final HashMap<Integer, SoftReference<SurfaceView>> mUidsList = new HashMap<>();

    /**
     * 直播视频窗口类型
     */
    public int mViewType = VIEW_TYPE_DEFAULT;
    /**
     * 缺省的窗口类型
     */
    public static final int VIEW_TYPE_DEFAULT = 0;
    /**
     * 小窗口类型
     */
    public static final int VIEW_TYPE_SMALL = 1;

    /**
     * 小窗口的适配器
     */
    private SmallVideoViewAdapter mSmallVideoViewAdapter;

    /**
     * Fragment管理的布局
     */
    View mLayout;

    ImageView btnSwitchBroadcast,btnSwitchCamera,btnMute;

    public LiveRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_live_room, container, false);
        initView(mLayout);
        setListener(mLayout);
        return mLayout;
    }

    private void setListener(View layout) {
        setOnCloseAtivityLisener(layout);
        setShowOrHideClickListener(layout);
    }

    /**
     * 设置显示隐藏一组按钮的按钮单击事件监听
     * @param layout
     */
    private void setShowOrHideClickListener(View layout) {
        layout.findViewById(R.id.ivShowHide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean toHide = true;
                if (view.getTag() != null && (boolean) view.getTag()) {
                    toHide = false;//设置为显示
                }
                view.setTag(toHide);
                doShowButtons(toHide);
            }
        });
    }

    /**
     * 设置关闭直播窗口的事件监听
     * @param layout
     */
    private void setOnCloseAtivityLisener(View layout) {
        layout.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    /**
     * 初始化view
     * @param layout
     */
    private void initView(View layout) {
        //添加针连接进当前频道钉钉用户的各种事件监听
        event().addEventHandler(this);

        //获取Activity传递的主播/观众类型和房间号
        Bundle bundle = getArguments();
        int cRole = bundle.getInt(ConstantApp.ACTION_KEY_CROLE, 0);
        if (cRole == 0) {
            throw new RuntimeException("Should not reach here");
        }

        String roomName = bundle.getString(ConstantApp.ACTION_KEY_ROOM_NAME);
        //进行主播/观众和视频画面的设置
        doConfigEngine(cRole);

        //创建显示视频的RecyclerView
        mGridVideoViewContainer = (GridVideoViewContainer) layout.findViewById(R.id.grid_video_view_container);
        //设置针对RecyclerView列表项的双击事件监听,小屏和全屏的切换
        mGridVideoViewContainer.setItemEventHandler(new VideoViewEventListener() {
            @Override
            public void onItemDoubleClick(View v, Object item) {
                Log.i("onItemDoubleClick ", v + " " + item);
                if (mViewType == VIEW_TYPE_DEFAULT)//若是缺省的全屏，则切换至小屏
                    switchToSmallVideoView(((VideoStatusData) item).mUid);
                else
                    switchToDefaultVideoView();
            }
        });

        //主播和观众切换按钮
        btnSwitchBroadcast = (ImageView) mLayout.findViewById(R.id.btnSwitchBroadcast);
        //自拍和拍照切换按钮
        btnSwitchCamera = (ImageView) mLayout.findViewById(R.id.btnSwitchCamera);
        //静音切换按钮
        btnMute = (ImageView) mLayout.findViewById(R.id.btnMute);

        if (isBroadcaster(cRole)) {//若是主播
            //创建SurfaceView实例
            SurfaceView surfaceV = RtcEngine.CreateRendererView(getActivity().getApplicationContext());
            //设置本地视频显示信息:视频显示的view和显示模式
            rtcEngine().setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0));
            surfaceV.setZOrderOnTop(true);
            surfaceV.setZOrderMediaOverlay(true);

            mUidsList.put(0, new SoftReference<>(surfaceV)); // get first surface view
            // first is now full view
            mGridVideoViewContainer.initViewContainer(getActivity().getApplicationContext(), 0, mUidsList);
            //预览
            worker().preview(true, surfaceV, 0);
            //设置按钮在主播和观众之间切换
            broadcasterUI();
        } else {
            //设置语音的主播和观众之间的切换
            audienceUI();
        }
        //加入频道
        worker().joinChannel(roomName, config().mUid);

        TextView textRoomName = (TextView) layout.findViewById(R.id.room_name);
        textRoomName.setText(roomName);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        doRenderRemoteUi(uid);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity().isFinishing()) {
                    return;
                }

                if (mUidsList.containsKey(uid)) {
                    Log.d("main", (uid & 0xFFFFFFFFL) + " " + mUidsList.get(uid));
                    return;
                }

                final boolean isBroadcaster = isBroadcaster();
                Log.d("main", channel + " " + uid + " " + elapsed + " " + isBroadcaster);

                worker().getEngineConfig().mUid = uid;

                //从列表中取出本地用户的SurfaceView对象(视频)
                SoftReference<SurfaceView> surfaceV = mUidsList.remove(0);
                if (surfaceV != null) {//若存在，则加入到列表中
                    mUidsList.put(uid, surfaceV);
                }

                if (isBroadcaster) {
                    //若是主播，则取消静音
                    rtcEngine().muteLocalAudioStream(false);
                } else {//设置静音
                    rtcEngine().muteLocalAudioStream(true);
                }

                worker().getRtcEngine().setEnableSpeakerphone(true);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d("main", (uid & 0xFFFFFFFFL) + " " + reason);
        doRemoveRemoteUi(uid);
    }

    WorkerThread worker() {
        return ((AGApplication) getActivity().getApplication()).getWorkerThread();
    }

    MyEngineEventHandler event() {
        return ((AGApplication) getActivity().getApplication()).getWorkerThread().eventHandler();
    }

    //进行主播、视频画面的配置
    private void doConfigEngine(int cRole) {
        //获取之前设置的屏幕分辨率在分辨率数组中的索引
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int prefIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX, ConstantApp.DEFAULT_PROFILE_IDX);
        if (prefIndex > ConstantApp.VIDEO_PROFILES.length - 1) {
            prefIndex = ConstantApp.DEFAULT_PROFILE_IDX;
        }
        //获取之前设置的屏幕分辨率
        int vProfile = ConstantApp.VIDEO_PROFILES[prefIndex];

        //在工作线程中进行斌到模式(主播/观众)、视频模式、单/双流、日志等
        worker().configEngine(cRole, vProfile);
    }

    private void switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null)
            mSmallVideoViewDock.setVisibility(View.GONE);
        mGridVideoViewContainer.initViewContainer(getActivity().getApplicationContext(), config().mUid, mUidsList);

        mViewType = VIEW_TYPE_DEFAULT;

        int sizeLimit = mUidsList.size();
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        for (int i = 0; i < sizeLimit; i++) {
            int uid = mGridVideoViewContainer.getItem(i).mUid;
            if (config().mUid != uid) {
                rtcEngine().setRemoteVideoStreamType(uid, Constants.VIDEO_STREAM_HIGH);
                Log.d("main", mUidsList.size() + " " + (uid & 0xFFFFFFFFL));
            }
        }
    }

    private void switchToSmallVideoView(int uid) {
        HashMap<Integer, SoftReference<SurfaceView>> slice = new HashMap<>(1);
        slice.put(uid, mUidsList.get(uid));
        mGridVideoViewContainer.initViewContainer(getActivity().getApplicationContext(), uid, slice);

        bindToSmallVideoView(uid);

        mViewType = VIEW_TYPE_SMALL;

        requestRemoteStreamType(mUidsList.size());
    }

    private boolean isBroadcaster(int cRole) {
        return cRole == Constants.CLIENT_ROLE_BROADCASTER;
    }

    private boolean isBroadcaster() {
        return isBroadcaster(config().mClientRole);
    }

    protected final EngineConfig config() {
        return ((AGApplication) getActivity().getApplication()).getWorkerThread().getEngineConfig();
    }

    protected RtcEngine rtcEngine() {
        return ((AGApplication) getActivity().getApplication()).getWorkerThread().getRtcEngine();
    }

    /**
     * 按主播设置UI
     *
     */
    private void broadcasterUI() {
        btnSwitchBroadcast.setTag(true);
        btnSwitchBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && (boolean) tag) {
                    doSwitchToBroadcaster(false);
                } else {
                    doSwitchToBroadcaster(true);
                }
            }
        });
        btnSwitchBroadcast.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                worker().getRtcEngine().switchCamera();
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                boolean flag = true;
                if (tag != null && (boolean) tag) {
                    flag = false;
                }
                worker().getRtcEngine().muteLocalAudioStream(flag);
                ImageView button = (ImageView) v;
                button.setTag(flag);
                if (flag) {
                    button.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
                } else {
                    button.clearColorFilter();
                }
            }
        });
    }

    private void audienceUI() {
        btnSwitchBroadcast.setTag(null);
        btnSwitchBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && (boolean) tag) {
                    doSwitchToBroadcaster(false);
                } else {
                    doSwitchToBroadcaster(true);
                }
            }
        });
        btnSwitchBroadcast.clearColorFilter();
        btnSwitchCamera.setVisibility(View.GONE);
        btnMute.setTag(null);
        btnMute.setVisibility(View.GONE);
        btnMute.clearColorFilter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doLeaveChannel();//离开频道
        //取消事件监听
        event().removeEventHandler(this);
        //清除所有的画面
        mUidsList.clear();
    }

    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
        if (isBroadcaster()) {
            worker().preview(false, null, 0);
        }
    }

    private void bindToSmallVideoView(int exceptUid) {
        if (mSmallVideoViewDock == null) {
            ViewStub stub = (ViewStub) mLayout.findViewById(R.id.small_video_view_dock);
            mSmallVideoViewDock = (RelativeLayout) stub.inflate();
        }

        RecyclerView recycler = (RecyclerView) mLayout.findViewById(R.id.small_video_view_container);

        boolean create = false;

        if (mSmallVideoViewAdapter == null) {
            create = true;
            mSmallVideoViewAdapter = new SmallVideoViewAdapter(getActivity(), exceptUid, mUidsList, new VideoViewEventListener() {
                @Override
                public void onItemDoubleClick(View v, Object item) {
                    switchToDefaultVideoView();
                }
            });
            mSmallVideoViewAdapter.setHasStableIds(true);
        }
        recycler.setHasFixedSize(true);

        recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false));
        recycler.setAdapter(mSmallVideoViewAdapter);

        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        if (!create) {
            mSmallVideoViewAdapter.notifyUiChanged(mUidsList, exceptUid, null, null);
        }
        recycler.setVisibility(View.VISIBLE);
        mSmallVideoViewDock.setVisibility(View.VISIBLE);
    }

    private void requestRemoteStreamType(final int currentHostCount) {
        Log.d("main", "" + currentHostCount);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap.Entry<Integer, SoftReference<SurfaceView>> highest = null;
                for (HashMap.Entry<Integer, SoftReference<SurfaceView>> pair : mUidsList.entrySet()) {
                    Log.d("main", currentHostCount + " local " + (config().mUid & 0xFFFFFFFFL) + " " + (pair.getKey() & 0xFFFFFFFFL) + " " + pair.getValue().get().getHeight() + " " + pair.getValue().get().getWidth());
                    if (pair.getKey() != config().mUid && (highest == null || highest.getValue().get().getHeight() < pair.getValue().get().getHeight())) {
                        if (highest != null) {
                            //指定接收远端用户的视频流大小。使用该方法可以根据视频窗口的大小动态调整对应视频流的大小，以节约带宽和计算资源
                            rtcEngine().setRemoteVideoStreamType(highest.getKey(), Constants.VIDEO_STREAM_LOW);
                            Log.d("main", currentHostCount + currentHostCount + " " + (highest.getKey() & 0xFFFFFFFFL) + " " + highest.getValue().get().getWidth() + " " + highest.getValue().get().getHeight());
                        }
                        highest = pair;
                    } else if (pair.getKey() != config().mUid && (highest != null && highest.getValue().get().getHeight() >= pair.getValue().get().getHeight())) {
                        rtcEngine().setRemoteVideoStreamType(pair.getKey(), Constants.VIDEO_STREAM_LOW);
                        Log.d("main", currentHostCount + currentHostCount + " " + (pair.getKey() & 0xFFFFFFFFL) + " " + pair.getValue().get().getWidth() + " " + pair.getValue().get().getHeight());
                    }
                }
                if (highest != null && highest.getKey() != 0) {
                    rtcEngine().setRemoteVideoStreamType(highest.getKey(), Constants.VIDEO_STREAM_HIGH);
                    Log.d("main", currentHostCount + currentHostCount + " " + (highest.getKey() & 0xFFFFFFFFL) + " " + highest.getValue().get().getWidth() + " " + highest.getValue().get().getHeight());
                }
            }
        }, 500);
    }

    private void doSwitchToBroadcaster(boolean broadcaster) {
        final int currentHostCount = mUidsList.size();
        final int uid = config().mUid;
        Log.d("main", currentHostCount + " " + (uid & 0XFFFFFFFFL) + " " + broadcaster);

        if (broadcaster) {
            doConfigEngine(Constants.CLIENT_ROLE_BROADCASTER);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doRenderRemoteUi(uid);

                    broadcasterUI();

                    doShowButtons(false);
                }
            }, 1000); // wait for reconfig engine

            rtcEngine().muteLocalVideoStream(false);
            rtcEngine().muteLocalAudioStream(false);
        } else {
            stopInteraction(currentHostCount, uid);
        }
    }

    private void doShowButtons(boolean hide) {
        //根据hide的值显示或隐藏直播窗口上部区域(包括关闭按钮)
        View topArea = mLayout.findViewById(R.id.top_area);
        topArea.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);

        //主播和观众的切换按钮
        btnSwitchBroadcast.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        if (isBroadcaster()) {
            btnSwitchCamera.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            btnMute.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        } else {// 若是观众，则隐藏相机和静音两个按钮
            btnSwitchCamera.setVisibility(View.INVISIBLE);
            btnMute.setVisibility(View.INVISIBLE);
        }
    }

    private void stopInteraction(final int currentHostCount, final int uid) {
        doConfigEngine(Constants.CLIENT_ROLE_AUDIENCE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doRemoveRemoteUi(uid);
                audienceUI();

                doShowButtons(false);
            }
        }, 1000); // wait for reconfig engine

        rtcEngine().muteLocalAudioStream(true);
    }

    public void doRenderRemoteUi(final int uid) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity().isFinishing()) {
                    return;
                }

                SurfaceView surfaceV = RtcEngine.CreateRendererView(getActivity().getApplicationContext());
                surfaceV.setZOrderOnTop(true);
                surfaceV.setZOrderMediaOverlay(true);
                mUidsList.put(uid, new SoftReference<>(surfaceV));
                if (config().mUid == uid) {//设置本地视频属性
                    rtcEngine().setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                } else {
                    //设置远端视频显示属性
                    rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                }

                if (mViewType == VIEW_TYPE_DEFAULT) {
                    Log.d("main", " " + (uid & 0xFFFFFFFFL));
                    switchToDefaultVideoView();
                } else {
                    int bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                    Log.d("main", " " + (uid & 0xFFFFFFFFL) + " " + (bigBgUid & 0xFFFFFFFFL));
                    switchToSmallVideoView(bigBgUid);
                }
            }
        });
    }

    /**
     * 移除远程用户的视频
     *
     * @param uid
     */
    private void doRemoveRemoteUi(final int uid) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity().isFinishing()) {
                    return;
                }

                mUidsList.remove(uid);

                int bigBgUid = -1;
                if (mSmallVideoViewAdapter != null) {
                    bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                }

                Log.d("main", (uid & 0xFFFFFFFFL) + " " + (bigBgUid & 0xFFFFFFFFL));

                if (mViewType == VIEW_TYPE_DEFAULT || uid == bigBgUid) {
                    switchToDefaultVideoView();
                } else {
                    switchToSmallVideoView(bigBgUid);
                }
            }
        });
    }
}

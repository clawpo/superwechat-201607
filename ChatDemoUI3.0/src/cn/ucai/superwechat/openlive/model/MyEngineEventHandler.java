package cn.ucai.superwechat.openlive.model;

import android.content.Context;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import cn.ucai.superwechat.utils.L;
import io.agora.rtc.IRtcEngineEventHandler;

public class MyEngineEventHandler {
    public MyEngineEventHandler(Context ctx, EngineConfig config) {
        this.mContext = ctx;
        this.mConfig = config;
    }

    private final EngineConfig mConfig;

    private final Context mContext;

    private final ConcurrentHashMap<AGEventHandler, Integer> mEventHandlerList = new ConcurrentHashMap<>();

    public void addEventHandler(AGEventHandler handler) {
        this.mEventHandlerList.put(handler, 0);
    }

    public void removeEventHandler(AGEventHandler handler) {
        this.mEventHandlerList.remove(handler);
    }

    final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                AGEventHandler handler = it.next();
                handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
            L.e("onFirstLocalVideoFrame " + " " + width + " " + height + " " + elapsed);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            // FIXME this callback may return times
            Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                AGEventHandler handler = it.next();
                handler.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
        }

        @Override
        public void onRtcStats(RtcStats stats) {
        }


        @Override
        public void onLeaveChannel(RtcStats stats) {

        }

        @Override
        public void onLastmileQuality(int quality) {
            L.e("onLastmileQuality " + quality);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            L.e("onError " + err);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            L.e("onJoinChannelSuccess " + channel + " " + uid + " " + (uid & 0xFFFFFFFFL) + " " + elapsed);

            Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                AGEventHandler handler = it.next();
                handler.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            L.e("onRejoinChannelSuccess " + channel + " " + uid + " " + elapsed);
        }

        public void onWarning(int warn) {
            L.e("onWarning " + warn);
        }
    };

}

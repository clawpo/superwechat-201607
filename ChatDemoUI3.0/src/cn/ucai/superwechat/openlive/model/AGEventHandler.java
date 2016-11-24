package cn.ucai.superwechat.openlive.model;

/**
 * 首次远程视频、加入频道、用户下线事件处理接口
 */
public interface AGEventHandler {
    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);
}

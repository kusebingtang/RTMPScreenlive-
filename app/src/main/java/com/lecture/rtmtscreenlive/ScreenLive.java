package com.lecture.rtmtscreenlive;

import android.media.projection.MediaProjection;

import java.util.concurrent.LinkedBlockingQueue;

public class ScreenLive extends Thread {

    private static final String TAG = "------>dddd<--------";
    private boolean isLiving;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private String url;
    private MediaProjection mediaProjection;


    public void startLive(String url, MediaProjection mediaProjection) {
        this.url = url;
        this.mediaProjection = mediaProjection;
        LiveTaskManager.getInstance().execute(this);

    }


    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        queue.add(rtmpPackage);
    }

    @Override
    public void run() {

        VideoCodec videoCodec = new VideoCodec(this);
        videoCodec.startLive(mediaProjection);
    }
}

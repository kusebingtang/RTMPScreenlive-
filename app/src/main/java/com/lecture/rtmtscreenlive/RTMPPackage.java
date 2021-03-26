package com.lecture.rtmtscreenlive;

public class RTMPPackage {

    private byte[] buffer;
    private long tms;
    //    视频包 音频包
    private int type;
    public static final int RTMP_PACKET_TYPE_AUDIO_DATA = 2;
    public static final int RTMP_PACKET_TYPE_AUDIO_HEAD = 1;
    public static final int RTMP_PACKET_TYPE_VIDEO = 0;

    public RTMPPackage(byte[] buffer, long tms) {
        this.buffer = buffer;
        this.tms = tms;
    }

    public RTMPPackage() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public long getTms() {
        return tms;
    }

    public void setTms(long tms) {
        this.tms = tms;
    }
}
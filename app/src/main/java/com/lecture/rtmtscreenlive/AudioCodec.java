package com.lecture.rtmtscreenlive;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioCodec extends Thread {

    private final ScreenLive screenLive;
    MediaCodec mediaCodec;
    private AudioRecord audioRecord;
    boolean isRecording;
    private int minBufferSize;
    long startTime;

    public AudioCodec(ScreenLive screenLive) {
        this.screenLive = screenLive;
    }

    public void startLive() {
        /**
         * 1、准备编码器
         */
        try {
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    44100, 1);
            //编码规格，可以看成质量
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64_000);
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建AudioRecord 录音
        //最小缓冲区大小
        minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        audioRecord.startRecording();
        start();
    }

    @Override
    public void run() {
        isRecording = true;

        mediaCodec.start();

        //在获取播放的音频数据之前，先发送 audio Special config
        RTMPPackage rtmpPackage = new RTMPPackage();
        byte[] audioSpec = {0x12, 0x08};
        rtmpPackage.setBuffer(audioSpec);
        rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD);
        rtmpPackage.setTms(0);
        screenLive.addPackage(rtmpPackage);


        byte[] buffer = new byte[minBufferSize];
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isRecording) {
            //得到采集的声音数据
            int len = audioRecord.read(buffer, 0, buffer.length);
            if (len <= 0) {
                continue;
            }

            // 交给编码器编码

            //获取输入队列中能够使用的容器的下表
            int index = mediaCodec.dequeueInputBuffer(0);
            if (index >= 0) {
                ByteBuffer byteBuffer = mediaCodec.getInputBuffer(index);
                byteBuffer.clear();
                //把输入塞入容器
                byteBuffer.put(buffer, 0, len);

                //通知容器我们使用完了，你可以拿去编码了
                // 时间戳： 微秒， nano纳秒/1000
                mediaCodec.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0);
            }


            // 获取编码之后的数据
            index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            // 每次从编码器取完，再往编码器塞数据
            while (index >= 0 && isRecording) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
                byte[] data = new byte[bufferInfo.size];
                outputBuffer.get(data);

                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }


                //todo 送去推流
                rtmpPackage = new RTMPPackage();
                rtmpPackage.setBuffer(data);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA);
                //相对时间
                rtmpPackage.setTms(bufferInfo.presentationTimeUs/1000 - startTime);
                screenLive.addPackage(rtmpPackage);
                // 释放输出队列，让其能存放新数据
                mediaCodec.releaseOutputBuffer(index, false);

                index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }

        }
        isRecording = false;

    }
}
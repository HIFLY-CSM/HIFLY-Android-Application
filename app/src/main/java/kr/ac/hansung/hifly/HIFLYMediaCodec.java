package kr.ac.hansung.hifly;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by CYSN on 2017-08-03.
 */


public class HIFLYMediaCodec {

    private MediaFormat format;
    private MediaCodec codec;
    private final int timeoutUs = 10000;
    private final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/temp.mp4";
    private boolean finished;
    private ByteBuffer outputBuffer;
    private MediaCodec.BufferInfo bufferInfo;
    private int videoTrackIndex;
    public static final String VIDEO_ENCODING_FORMAT = "video/avc";
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private Surface surface;

    public HIFLYMediaCodec(Surface surface) {
        this.surface=surface;
        setupMediaCodec();
    }

    public void endCodec() {
        codec.stop();
        codec.release();
        codec = null;
    }

    public void InputYUVData(byte[] bytes, int endPosition){
        int inputBufferIndex=-2;
        try {
            inputBufferIndex = codec.dequeueInputBuffer(10000);
           // Log.i("kkk","in index: "+inputBufferIndex);
        } catch (Exception e) {
            Log.i("kkk", "decodeFrame: dequeue input: " + e);
            codec.stop();
            codec.reset();
            setupMediaCodec();
            e.printStackTrace();
        }
        if (inputBufferIndex >= 0) {
            ByteBuffer buffer=codec.getInputBuffer(inputBufferIndex);
            buffer.put(bytes,0,endPosition);
            codec.queueInputBuffer(inputBufferIndex,0,endPosition,timeoutUs, 0);
            buffer.clear();

            int outputBufferIndex = -2;

            outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo,timeoutUs);

            if (outputBufferIndex >= 0) { // 0 이상일 경우에 인코딩/디코딩 데이터가 출력됩니다.
                outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                codec.releaseOutputBuffer(outputBufferIndex, true);
                //Log.i("kkk", "ok data");

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.i("kkk","videoTrackIndex change: "+String.valueOf(videoTrackIndex));
            }
        }
    }

    public boolean outputData() {

        int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);

        if (outputBufferIndex >= 0) { // 0 이상일 경우에 인코딩/디코딩 데이터가 출력됩니다.

            outputBuffer = codec.getOutputBuffer(outputBufferIndex);
            codec.releaseOutputBuffer(outputBufferIndex, true);
            Log.i("kkk", "muxer start " + String.valueOf(outputBufferIndex));
            Log.i("kkk", "ok data");
            return true;

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            Log.i("kkk","videoTrackIndex change: "+String.valueOf(videoTrackIndex));
        }
        Log.i("kkk", "no data");
        return false;
    }

    private void setupMediaCodec() {

        format = MediaFormat.createVideoFormat(VIDEO_ENCODING_FORMAT, 1280, 720);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1280*720);

        try {
            if(Build.MODEL.toString().contains("SAMSUNG") || Build.MODEL.toString().contains("SM")){
                codec= MediaCodec.createByCodecName("OMX.google.h264.decoder");
            }
            else{
                codec = MediaCodec.createDecoderByType("video/avc");
            }
            codec.configure(format, this.surface, null,0);
            bufferInfo = new MediaCodec.BufferInfo();
            codec.start();
            Log.i("kkk","codec start");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
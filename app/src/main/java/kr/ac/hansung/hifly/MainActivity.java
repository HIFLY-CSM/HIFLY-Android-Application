package kr.ac.hansung.hifly;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    protected TextureView mVideoTexture = null;
    protected SurfaceView mVideoSurfaceView = null;
    protected SurfaceHolder mVideoSurfaceHolder = null;
    protected Surface mCodecSurface = null;
    private Button mBtnStreaming = null;

    //for live streaming
    private boolean isLiveStreaming = false;
    public BlockingQueue<byte[]> blockingQueue = new ArrayBlockingQueue<byte[]>(5);
    private HIFLYMediaCodec codec = null;
    /* private HIFLYStreamingSocket csmSocket;*/
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private MessageSocketThread messageSocketThread=null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int stopCheck = bundle.getInt("stop");
            switch (stopCheck) {
                case 1: {
                    mBtnStreaming.callOnClick();
                    break;
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                byteArrayOutputStream.write(videoBuffer, 0, size);
                if (size == 6) {
                    byte[] vd = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.reset();
                    codec.InputYUVData(vd, vd.length);
                    if (isLiveStreaming) {
                        try {
                            blockingQueue.add(vd);
                        } catch (Exception e) {
                            Log.i("kkkkk", "Queue is Full" + blockingQueue.size());
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();

        if(mVideoTexture == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        //userConnectionSocket.Disconnect();
        uninitPreviewer();
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void initUI() {
        mBtnStreaming = (Button)findViewById(R.id.btn_streaming);
        mBtnStreaming.setOnClickListener(this);
        // init mVideoTexture
        mVideoTexture = (TextureView)findViewById(R.id.video_texture);
        mVideoTexture.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mVideoSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
                Log.i("drone","surfaceView : " + mVideoSurfaceView.getWidth() + " * " + mVideoSurfaceView.getHeight() + " " + mVideoSurfaceView.toString());
                mVideoSurfaceHolder = mVideoSurfaceView.getHolder();
                Log.i("drone","surfaceHolder : " + mVideoSurfaceHolder.toString());
                mCodecSurface = mVideoSurfaceHolder.getSurface();
                Log.i("drone","surface : " + mCodecSurface.toString());
                codec = new HIFLYMediaCodec(mCodecSurface);
                Log.i("drone","codec : " + codec.toString());
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
    }

    private void initPreviewer() {

        BaseProduct product = ConnectionApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast("Disconnected");
        } else {
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = ConnectionApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_streaming: {
                if(!isLiveStreaming) {
                    Log.e("jina","messagesocket thread creating.....");
                    messageSocketThread = new MessageSocketThread(blockingQueue,handler);
                    Log.e("jina","messagesocket thread created");
                    Log.i("kkk","check");
                    messageSocketThread.start();
                    Log.e("jina","messagesocket thread started");
                    isLiveStreaming = true;
                    Toast.makeText(getApplicationContext(), "Streaming Started", Toast.LENGTH_LONG).show();
                    mBtnStreaming.setText("Stop Streaming");
                }
                else{
                    messageSocketThread.sendData("FinishStreaming");
                    isLiveStreaming = false;
                     mBtnStreaming.setText("Start Streaming");
                }
                break;
            }
            default: break;
        }
    }

}

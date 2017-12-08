package com.example.democm;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;

/**
 * Created by 阳旭东 on 2017/12/7.
 */

public class Pusher {
    private static final String TAG = "Pusher";
    private Context mContext;
    AlivcLivePusher mAlivcLivePusher;
    private SurfaceView mSurfaceView;
    private String mUrl;

    Pusher(Context context, SurfaceView surfaceView) {
        mContext = context;
        mSurfaceView = surfaceView;
    }

    public void prepareAndPush(String url){
        Log.d(TAG, "prepareAndPush:" + url);
        mUrl = url;
        initPusher();
        initSurface();
    }

    private void initSurface(){
        Log.d(TAG, "initSurface");
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                holder.setKeepScreenOn(true);
                if(mAlivcLivePusher != null) {
                    try {
                        Log.d(TAG, "....startpreview");
                        mAlivcLivePusher.startPreview(mSurfaceView);
                        Log.d(TAG, "....startPush:" + mUrl);
                        mAlivcLivePusher.startPush(mUrl);
                    } catch (IllegalArgumentException e) {
                        e.toString();
                    } catch (IllegalStateException e) {
                        e.toString();
                    }
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceDestroyed");
            }
        });
    }
    private void initPusher(){
        Log.d(TAG, "initPusher");
        mAlivcLivePusher = new AlivcLivePusher();
        AlivcLivePushConfig mAlivcLivePushConfig = new AlivcLivePushConfig();
        mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_480P);//分辨率540P
        mAlivcLivePushConfig.setInitialVideoBitrate(800); //初始码率800Kbps
        mAlivcLivePushConfig.setTargetVideoBitrate(800); //目标码率800Kbps
        mAlivcLivePushConfig.setMinVideoBitrate(400); //最小码率400Kbps
        mAlivcLivePushConfig.setBeautyOn(false); //关闭美颜
        mAlivcLivePushConfig.setPreviewOrientation(AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT);//竖屏推流
        mAlivcLivePushConfig.setFps(AlivcFpsEnum.FPS_20); //帧率20
        /**
         * 添加水印，起点位置坐标x[0,width) y[0,height)，宽度(0,width]，最多添加3个
         *
         * @param path 水印图片
         * @param x      水印左上角x轴位置 相对位置 0～1
         * @param y      水印右上角y轴位置 相对位置 0～1
         * @param width  水印显示宽度 相对位置 0～1
         */
        // mAlivcLivePushConfig.addWaterMark(waterPath, 0.2, 0.2, 0.3);


        mAlivcLivePusher.init(mContext, mAlivcLivePushConfig);

        mAlivcLivePusher.setLivePushErrorListener(new AlivcLivePushErrorListener() {
            @Override
            public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
                if(error != null) {
                    //添加UI提示或者用户自定义的错误处理
                }
            }
            @Override
            public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
                if(error != null) {
                    //添加UI提示或者用户自定义的错误处理
                }
            }
        });

        mAlivcLivePusher.setLivePushInfoListener(new AlivcLivePushInfoListener() {
            /**
             * 预览开始事件
             */
            @Override
            public void onPreviewStarted(AlivcLivePusher pusher) {
            }
            /**
             * 预览结束事件
             */
            @Override
            public void onPreviewStoped(AlivcLivePusher pusher) {
            }
            /**
             * 推流开始通知
             */
            @Override
            public void onPushStarted(AlivcLivePusher pusher) {
            }
            /**
             * 推流暂停通知
             */
            @Override
            public void onPushPauesed(AlivcLivePusher pusher) {
            }
            /**
             * 推流恢复通知
             */
            @Override
            public void onPushResumed(AlivcLivePusher pusher) {
            }
            /**
             * 推流停止通知
             */
            @Override
            public void onPushStoped(AlivcLivePusher pusher) {
            }
            /**
             * 推流重启通知
             */
            @Override
            public void onPushRestarted(AlivcLivePusher pusher) {
            }
            /**
             * 首帧渲染通知
             */
            @Override
            public void onFirstFramePreviewed(AlivcLivePusher pusher) {
            }
            /**
             *丢帧通知
             */
            @Override
            public void onDropFrame(AlivcLivePusher pusher, int countBef, int countAft) {
            }
            /**
             *调整码率
             */
            @Override
            public void onAdjustBitRate(AlivcLivePusher pusher, int curBr, int targetBr) {
            }
            /**
             *调整帧率
             */
            @Override
            public void onAdjustFps(AlivcLivePusher pusher, int curFps, int targetFps) {
            }
        });

        mAlivcLivePusher.setLivePushNetworkListener(new AlivcLivePushNetworkListener() {
            /**
             * 网络差通知
             */
            @Override
            public void onNetworkPoor(AlivcLivePusher pusher) {
            }
            /**
             * 网络恢复通知
             */
            @Override
            public void onNetworkRecovery(AlivcLivePusher pusher) {
            }
            /**
             * 重连开始
             */
            @Override
            public void onReconnectStart(AlivcLivePusher pusher) {
            }
            /**
             * 重连失败
             */
            @Override
            public void onReconnectFail(AlivcLivePusher pusher) {
            }
            /**
             * 重连成功
             */
            @Override
            public void onReconnectSucceed(AlivcLivePusher pusher) {
            }
            /**
             * 发送数据超时
             */
            @Override
            public void onSendDataTimeout(AlivcLivePusher pusher) {
            }
            /**
             * 连接失败
             */
            @Override
            public void onConnectFail(AlivcLivePusher pusher) {
            }
        });
    }

    public void startPush(String url){
        //"rtmp://video-center.alivecdn.com/AppName/StreamName?vhost=push.yangxudong.com"
        mAlivcLivePusher.startPreview(mSurfaceView);;
        mAlivcLivePusher.startPush(url);
    }

    public void stopPush(){
        mAlivcLivePusher.stopPreview();
        mAlivcLivePusher.stopPush();
    }

    public void destroy(){
        stopPush();
        mAlivcLivePusher.destroy();
    }
}

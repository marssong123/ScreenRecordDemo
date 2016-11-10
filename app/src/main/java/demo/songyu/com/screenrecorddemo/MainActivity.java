package demo.songyu.com.screenrecorddemo;

/*
 * Copyright (c) 2014 Yrom Wang <http://www.yrom.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;

//    CMD 命令设置Options:
//            --size WIDTHxHEIGHT
//    Set the video size, e.g. "1280x720".  Default is the device's main
//    display resolution (if supported), 1280x720 if not.  For best results,
//    use a size supported by the AVC encoder.
//            --bit-rate RATE
//    Set the video bit rate, in megabits per second.  Default 4Mbps.
//    --time-limit TIME
//    Set the maximum recording time, in seconds.  Default / maximum is 180.
//            --rotate
//    Rotate the output 90 degrees.
//    --verbose
//    Display interesting information on stdout.
//    --help
//    Show this message


public class MainActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private Button mButton;
    private int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    private Button stopButton;
    private Thread thread;
    public static boolean isrun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);
        stopButton = (Button) findViewById(R.id.stopButton);
        mButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        if (currentapiVersion > 20) {
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentapiVersion > 20) {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }
            // video size
            final int width = 2560;
            final int height = 1440;
            File file = new File(Environment.getExternalStorageDirectory(),
                    "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
            String path=new File(Environment.getExternalStorageDirectory(), "abcabc.mp4").getPath();
            Log.i("MainActivity path",path);
            final int bitrate = 6000000;
            mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection,path);
            mRecorder.start();
            Toast.makeText(this, "Screen recorder is running...", Toast.LENGTH_SHORT).show();
            moveTaskToBack(true);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {


        if (v.getId() == R.id.button) {
            try {

                if (currentapiVersion > 20) {

                    Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, REQUEST_CODE);

                } else {

                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String cmd = "screenrecord " + new File(Environment.getExternalStorageDirectory(), "33333.mp4").getPath();
                            execShellCom(cmd);
                        }
                    });
                    thread.start();

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(v.getContext(), "开始录屏", Toast.LENGTH_LONG).show();
        }



        else if (v.getId() == R.id.stopButton) {

            if (mRecorder != null) {
                mRecorder.quit();
                mRecorder = null;
            }else {
                stop();
            }
            Toast.makeText(v.getContext(), "结束录屏", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            mRecorder.quit();
            mRecorder = null;
        }
    }

    private void execShellCom(String cmd) {
        try {
            //权限设置
            Process p = Runtime.getRuntime().exec("su");
            //获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            //提交命令
            dataOutputStream.flush();
            //关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }


    public synchronized void stop() {
        if (thread == null) {
            return;
        }
        Thread moribund = thread;
        thread = null;
        moribund.interrupt();
    }


}

package com.eveyen.RecordBao;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.eveyen.RecordBao.File.File_Function;
import com.eveyen.RecordBao.Record.Record_implement;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 *  作者：EveYen
 *  最後修改日期：10/12
 *  完成功能：漂浮按鈕
 **/
public class FloatWindows extends Service {

    private static final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

    private LayoutInflater inflater;
    private Display mDisplay;
    private View layoutView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private View.OnTouchListener touchListener;
    private View.OnClickListener clickListener;

    private boolean isRecording=false;
    Record_implement audioRecord;
    public String voicePath;
    private SQL_implement item;
    private ArrayList<SQL_Item> lists;
    String getText = "null";
    String Title = null;

    public FloatWindows() {}

    public void initData(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date date = new Date();
        String name = "note_" + sdf.format(date);
        voicePath = File_Function.getRootPath() + name + ".wav";
        Title = name;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                LayoutParamFlags, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT; // 圖片按鈕的初始位置
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        inflater = LayoutInflater.from(this);
        layoutView = inflater.inflate(R.layout.floatwindows, null); // 取得layout
        windowManager.addView(layoutView, params);

        final ImageButton button = (ImageButton) layoutView
                .findViewById(R.id.float_imgb); // 取得圖片按鈕
        button.setBackgroundResource(R.drawable.microphone);
        // 圖片按鈕-點擊監聽事件
        clickListener = new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    Log.i("Service", "stop!");
                    if(!isRecording) {
                        button.setBackgroundResource(R.drawable.pause);
                        startRecord();
                    }else{
                        button.setBackgroundResource(R.drawable.microphone);
                        stopRecord();
                        startWebRecognizer(readFile(voicePath));
                    }

                } catch (Exception ex) {
                }
            }
        };
        touchListener = new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long downTime;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 按下圖片按鈕尚未放開時
                        Log.i("downTime", downTime + "");
                        downTime = SystemClock.elapsedRealtime();
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP: // 放開圖片按鈕時
                        long currentTime = SystemClock.elapsedRealtime();
                        Log.i("currentTime - downTime", currentTime - downTime + "");
                        if (currentTime - downTime < 200) { // 當按下圖片按鈕時
                            v.performClick(); // 自動點擊事件
                        } else {
                             updateViewLocation(); //黏住邊框功能
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE: // 按住移動時
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(layoutView, params);
                        return true;
                }
                return false;
            }

            // 黏住邊框功能
            private void updateViewLocation() {
                DisplayMetrics metrics = calculateDisplayMetrics();
                int width = metrics.widthPixels / 2;
                if (params.x >= width)
                    params.x = (width * 2) - 10;
                else if (params.x <= width)
                    params.x = 10;
                windowManager.updateViewLayout(layoutView, params);
            }
        };

        button.setOnClickListener(clickListener); // 圖片按鈕-點擊監聽事件
        layoutView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                return false;
            }
        });

        button.setOnTouchListener(touchListener);// 圖片按鈕-移動監聽事件

    }

    private DisplayMetrics calculateDisplayMetrics() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getMetrics(mDisplayMetrics);
        return mDisplayMetrics;
    }

    private void startRecord() {
        initData();
        isRecording = true;
        audioRecord = new Record_implement(voicePath);
        Log.e("app",voicePath);
        audioRecord.start();
    }

    private void stopRecord(){
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }

    public void Alert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Do you want to save this record?");
        dialog.setMessage(Title);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startWebRecognizer(readFile(voicePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File f = new File(voicePath);
                if (f.exists()) f.delete();
            }
        });
        dialog.setNeutralButton("Preview", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(voicePath)), "audio/MP3");
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private HttpURLConnection getConnection(){

        HttpURLConnection connection = null;
        try{
            URL httpUrl = new URL("http://www.google.com/speech-api/v2/recognize?xjerr=1&client=chromium&maxresults=1&lang=zh-TW&key=AIzaSyBPohttFCsLdkFGqyuAL8qcWuYkMv9VJJo");
            connection = (HttpURLConnection)httpUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "audio/l16; rate=8000");
        }catch (MalformedURLException ex){
            Log.e("TAG","getConnection();Invalid url format",ex);
        }catch (ProtocolException ex){
            Log.e("TAG", "getConnection();Un support protocol",ex);
        }catch (IOException ex){
            Log.e("TAG","getConnection();IO error while open connection",ex);
        }
        return connection;
    }

    public void startWebRecognizer(final byte[] wavData){
        final HttpURLConnection connection = getConnection();
        getText="";
        if (connection == null){
            Log.e("TAG","未連上oogle speech");
        }else {
            new Thread(){
                @Override
                public void run(){
                    try {
                        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                        dos.write(wavData);
                        dos.flush();
                        dos.close();

                        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),
                                Charset.forName("utf-8"));
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String decodedString = "";
                        String StrTemp;
                        while ((StrTemp = bufferedReader.readLine()) != null){
                            decodedString += StrTemp;
                        }
                        getText = getTextString(decodedString);
                        updateProHandler.sendEmptyMessage(500);
                        Log.e("TAG",getText);
                    }catch (IOException ex){
                        Log.e("TAG","傳檔失敗");
                    }
                }
            }.start();
        }
    }

    public String getTextString(String textString)
    {
        String returnStr="你沒講話啊";
        if(textString.split("transcript\":\"").length>1) {
            returnStr = textString.split("transcript\":\"")[1].split("\"")[0];
        }
        return returnStr;
    }

    public static byte[] readFile(String filename) throws IOException {
        // Open file
        File f = new File(filename);
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        try {
            // Get and check length
            long longlength = raf.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            raf.readFully(data);
            return data;
        } finally {
            raf.close();
        }
    }

    Handler updateProHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 500) {
                Log.e("FloatView",getText);
                saveData();
            }
        }
    };

    public void saveData(){
        int[] colorset={Color.argb(255,255,201,181),Color.argb(255,192,185,221),Color.argb(255,197,255,216),Color.argb(255,244,241,139),Color.argb(255,169,211,255)};
        int r = (int)(Math.random()* 5);
        item = new SQL_implement(this);
        SQL_Item temp = new SQL_Item(0, new Date().getTime(), colorset[r], Title, getText, voicePath, 0);
        item.insert(temp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(layoutView);
    }
}

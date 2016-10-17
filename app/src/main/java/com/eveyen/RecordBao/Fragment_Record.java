package com.eveyen.RecordBao;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.eveyen.RecordBao.CKIP.Text_mining;
import com.eveyen.RecordBao.File.File_Function;
import com.eveyen.RecordBao.Record.Record_implement;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
 *  最後修改日期：10/13
 *  完成功能：可以錄音，播放
 **/

public class Fragment_Record extends Fragment implements View.OnClickListener {
    private View v;

    private Chronometer com_voice_time;
    private ImageView iv_voice_img;
    private Button bt_record;
    private TextView tv_record_trans;

    Record_implement audioRecord;
    public String voicePath;
    private boolean isRecording=false;
    long timeWhenPaused = 0;

    private SQL_implement item;
    private ArrayList<SQL_Item> lists;

    Text_mining text_mining;
    String getText = "null";
    String Title = "";
    String Location = "";
    ArrayList<String> inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
    ArrayList<String> TagList = new ArrayList<String>();   //宣告動態陣列 存切詞的詞性
    ArrayList<String> DateList = new ArrayList<String>();

    public Fragment_Record() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_record, container, false);
        initElement();
        initListener();
        return v;
    }

    public void initElement(){
        com_voice_time = (Chronometer) v.findViewById(R.id.com_voice_time);
        iv_voice_img = (ImageView) v.findViewById(R.id.iv_voice_img);
        bt_record = (Button) v.findViewById(R.id.bt_record);
        tv_record_trans = (TextView)v.findViewById(R.id.tv_record_trans);

        bt_record.setText("Record");

        timeWhenPaused = 0;
        isRecording = false;

        bt_record.setEnabled(true);
    }

    public void initData(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date date = new Date();
        String name = "note_" + sdf.format(date);
        voicePath = File_Function.getRootPath() + name + ".wav";
        Title = name;
    }

    public void initListener() {
        bt_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_record:
                if (isRecording) {
                    stopRecord();
                    Alert();
                } else {
                    startRecord();
                }
                break;
        }
    }

    private void startRecord() {
        initData();
        isRecording = true;
        com_voice_time.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        com_voice_time.start();

        iv_voice_img.setImageResource(R.drawable.mic_selected);
        bt_record.setText("Stop");
        bt_record.setEnabled(true);
        tv_record_trans.setText("");
        // start
        audioRecord = new Record_implement(voicePath);
        Log.e("app",voicePath);
        audioRecord.start();
    }

    private void stopRecord(){
        isRecording = false;
        com_voice_time.stop();
        timeWhenPaused = 0;

        iv_voice_img.setImageResource(R.drawable.mic_default);
        bt_record.setText("Record");
        bt_record.setEnabled(true);

        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }

    public void Alert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Do you want to save this record?");
        dialog.setMessage(Title);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                com_voice_time.setBase(SystemClock.elapsedRealtime());
                try {
                    startWebRecognizer(File_Function.readFile(voicePath));
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

                        text_mining = new Text_mining(getText);
                        inputList = text_mining.getInputList();
                        TagList = text_mining.getTagList();
                        DateList = text_mining.getDate();
                        Location = text_mining.getLocation();

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
        String returnStr="-----";
        if(textString.split("transcript\":\"").length>1) {
            returnStr = textString.split("transcript\":\"")[1].split("\"")[0];
        }
        return returnStr;
    }


    Handler updateProHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 500) {
                tv_record_trans.append("時間：");
                for(int i=0;i<DateList.size();i++){
                    tv_record_trans.append(DateList.get(i));
                }
                tv_record_trans.append("\n");
                tv_record_trans.append("地點：");
                tv_record_trans.append(Location);
                saveData();
            }
        }
    };

    public void saveData(){
        int[] colorset={Color.argb(255,255,201,181),Color.argb(255,192,185,221),Color.argb(255,197,255,216),Color.argb(255,244,241,139),Color.argb(255,169,211,255)};
        int r = (int)(Math.random()* 5);
        item = new SQL_implement(getContext());
        SQL_Item temp = new SQL_Item(0, new Date().getTime(), colorset[r], Title, getText, voicePath, 0);
        item.insert(temp);
    }

    public void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }
}
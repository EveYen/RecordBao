package com.eveyen.RecordBao;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.eveyen.RecordBao.Record.Record_implement;
import com.eveyen.RecordBao.Tools.Data_Function;
import com.eveyen.RecordBao.Tools.GoogleSpeech;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
/**
 *  作者：EveYen
 *  最後修改日期：10/17
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

    Text_mining text_mining;
    String getText = "null";
    String Title = "";
    String Sloca = "";
    String Sdate = "";
    String Sche = "";
    String Person = "";
    ArrayList<String> inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
    ArrayList<String> TagList = new ArrayList<String>();   //宣告動態陣列 存切詞的詞性
    ArrayList<String> DateList = new ArrayList<String>();

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
        voicePath = Data_Function.getFilepath();
        Title = voicePath.split("/WAV/")[1];
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

    /**
     * 跳出是否儲存的提示
     */
    public void Alert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Do you want to save this record?");
        dialog.setMessage(Title);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                com_voice_time.setBase(SystemClock.elapsedRealtime());
                try {
                    startWebRecognizer(Data_Function.readFile(voicePath));
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

    /**
     * 語音辨識
     * @param wavData
     */
    public void startWebRecognizer(final byte[] wavData){
        final HttpURLConnection connection = GoogleSpeech.getConnection();
        getText="";
        if (connection == null){
            Log.e("TAG","未連上oogle speech");
        }else {
            new Thread(){
                @Override
                public void run(){
                    try {
                        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());//上傳需要辨識的資料
                        dos.write(wavData);
                        dos.flush();
                        dos.close();

                        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),
                                Charset.forName("utf-8"));//取回辨識完成的資料
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String decodedString = "";
                        String StrTemp;
                        while ((StrTemp = bufferedReader.readLine()) != null){
                            decodedString += StrTemp;
                        }
                        getText = GoogleSpeech.getTextString(decodedString);//解析資料

                        text_mining = new Text_mining(getContext(),getText); //傳上去CKIP
                        inputList = text_mining.getInputList();
                        TagList = text_mining.getTagList();
                        Sdate = text_mining.getDate();
                        try {
                            Sloca = text_mining.getLocation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Person = text_mining.getPerson();

                        updateProHandler.sendEmptyMessage(500);//這個thread完成才送出訊息給Handler
                        Log.e("TAG",getText);
                    }catch (IOException ex){
                        Log.e("TAG","傳檔失敗");
                    }
                }
            }.start();
        }
    }

    /**
     * 接收到相關訊息並顯示
     */
    Handler updateProHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 500) {
                Data_Function.saveData(getContext(), Title, getText, voicePath, Sdate, Sloca, Sche, Person);
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }
}
package com.eveyen.RecordBao;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.eveyen.RecordBao.CKIP.Text_mining;
import com.eveyen.RecordBao.Record.Record_implement;
import com.eveyen.RecordBao.SQL.SQL_implement;
import com.eveyen.RecordBao.Tools.Data_Function;
import com.eveyen.RecordBao.Tools.GoogleSpeech;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *  作者：EveYen
 *  最後修改日期：10/12
 *  完成功能：漂浮按鈕的錄音與儲存
 **/
public class FloatWindows extends Service {

    private static final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL   //把window以外的事件傳到下面的window
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE     //window不能獲得焦點
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD  //在安全鎖之外的鍵盤會被駁回
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; //鎖屏時也有window

    private Display mDisplay;
    private View layoutView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private boolean isRecording=false;
    Record_implement audioRecord;
    public String voicePath;
    private SQL_implement item;
    String getText = "null";
    String Title = null;

    Text_mining text_mining;
    String Sloca = "";
    String Sdate = "";
    String Sche = "";
    String Person = "";
    ArrayList<String> inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
    ArrayList<String> TagList = new ArrayList<String>();   //宣告動態陣列 存切詞的詞性

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //電話狀態的Listener
        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();
        //取得TelephonyManager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //將電話狀態的Listener加到取得TelephonyManager
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        createFloatView();
    }

    private void createFloatView(){
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                LayoutParamFlags, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.LEFT; // 圖片按鈕的初始位置
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        LayoutInflater inflater = LayoutInflater.from(this);
        layoutView = inflater.inflate(R.layout.floatwindows, null); // 取得layout
        windowManager.addView(layoutView, params);


        final ImageButton button = (ImageButton) layoutView.findViewById(R.id.float_imgb); // 取得圖片按鈕
        button.setBackgroundResource(R.drawable.microphone);
        View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    Log.i("Service", "stop!");
                    if (!isRecording) {
                        button.setBackgroundResource(R.drawable.pause);
                        startRecord();
                    } else {
                        button.setBackgroundResource(R.drawable.microphone);
                        stopRecord();
                        startWebRecognizer(Data_Function.readFile(voicePath));
                    }

                } catch (Exception ex) {

                }
            }
        };
        //圖片按鈕-觸碰移動事件
        View.OnTouchListener touchListener = new View.OnTouchListener() {
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
        button.setOnTouchListener(touchListener);// 圖片按鈕-移動監聽事件
        layoutView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent arg1) {
                return false;
            }
        });
    }

    private DisplayMetrics calculateDisplayMetrics() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getMetrics(mDisplayMetrics);
        return mDisplayMetrics;
    }

    private void startRecord() {
        voicePath = Data_Function.getFilepath();
        Title = voicePath.split("/WAV/")[1];
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
                        getText = GoogleSpeech.getTextString(decodedString);

                        text_mining = new Text_mining(getBaseContext(), getText); //傳上去CKIP
                        inputList = text_mining.getInputList();
                        TagList = text_mining.getTagList();
                        Sdate = text_mining.getDate();
                        try {
                            Sloca = text_mining.getLocation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Person = text_mining.getPerson();
                        updateProHandler.sendEmptyMessage(500);
                        Log.e("TAG",getText);
                    }catch (IOException ex){
                        Log.e("TAG","傳檔失敗");
                    }
                }
            }.start();
        }
    }

    Handler updateProHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 500) {
                Data_Function.saveData(getBaseContext(), Title, getText, voicePath, Sdate, Sloca, Sche, Person);
                Person = "";
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(layoutView);
    }

    public class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state) {
                //電話狀態是閒置的
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                //電話狀態是接起的
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    ArrayList<String[]> contact = getContactsName();
                    for(int j=0;j < contact.size();j++){
                        if(phoneNumber.equals(contact.get(j)[1])){
                            Person = contact.get(j)[0];
                        }
                    }
                    //Toast.makeText(getBaseContext(), Person, Toast.LENGTH_SHORT).show();
                    break;
                //電話狀態是響起的
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, phoneNumber);
        }

    }

    public ArrayList<String[]> getContactsName() {
        ArrayList<String[]> contactinfo = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();  //取得內容解析器
        //取得所有聯絡人
        Cursor contact_NAME = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (contact_NAME.moveToNext()) {
            String[] contacts = new String[2];
            long id = contact_NAME.getLong(contact_NAME.getColumnIndex(ContactsContract.Contacts._ID));//取的名字ID
            Cursor contact_NUM = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + Long.toString(id), null, null); //利用ID搜尋號碼
            while (contact_NUM.moveToNext()) {
                contacts[1] = contact_NUM.getString(contact_NUM.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ","");
            }
            contact_NUM.close();
            contacts[0] = contact_NAME.getString(contact_NAME.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contactinfo.add(contacts);
        }
        return contactinfo;
    }
}

package com.eveyen.RecordBao.CKIP;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import tw.cheyingwu.ckip.CKIP;
import tw.cheyingwu.ckip.Term;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：CKIP
 */
public class Text_mining {

    private Context mcontext;
    private String TAG = "text_mining";
    static ArrayList<String> inputList; //宣告動態陣列 存切詞的name
    static ArrayList<String> TagList;   //宣告動態陣列 存切詞的詞性
    static ArrayList<Boolean> DoneList;   //宣告動態陣列 確認是否已經分析過
    Calendar calendar, now, temp;
    SimpleDateFormat time_in_min = new SimpleDateFormat("yyyy-MM-dd HH:mm EEEE");
    SimpleDateFormat time_in_hour = new SimpleDateFormat("yyyy-MM-dd HH:00 EEEE");
    SimpleDateFormat time_in_day = new SimpleDateFormat("yyyy-MM-dd EEEE");

    private String URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private String Key = "AIzaSyBHkShKCuIqzU604t7VGsGylpToOecQaCo";
    String contactName = "";
    JSONObject j;

    int jump=0;

    public Text_mining(Context context, String textString) {
        mcontext = context;
        inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
        TagList = new ArrayList<String>();
        DoneList = new ArrayList<Boolean>();
        String CKIP_IP = "140.109.19.104";
        int CKIP_PORT = 1501;
        String CKIP_USERNAME = "boannyen84";
        String CKIP_PASSWORD = "nckucsieiir106";
        CKIP connect = new CKIP(CKIP_IP, CKIP_PORT, CKIP_USERNAME,
                CKIP_PASSWORD);
        connect.setRawText(textString);
        connect.send();

        for (Term t : connect.getTerm()) {
            inputList.add(t.getTerm()); // t.getTerm()會讀到斷詞的String，將其存到inputList陣列
            TagList.add(t.getTag());    // t.getTag() 會讀到斷詞的詞性，將其存到TagList陣列
            DoneList.add(false);
        }
    }

    public ArrayList<String> getInputList() {
        return inputList;
    }

    public ArrayList<String> getTagList() {
        return TagList;
    }

    public ArrayList<Boolean> getDoneList() {
        return DoneList;
    }

    /**
     * 找名詞內含有(中英)數字＋年月日
     *
     */
    public String getDate() {
        //初始化每個時間基準都為現在，每週第一天是星期一
        calendar = Calendar.getInstance();
        //calendar.setFirstDayOfWeek(Calendar.MONDAY);
        now = Calendar.getInstance();
        //now.setFirstDayOfWeek(Calendar.MONDAY);
        temp = Calendar.getInstance();
        //temp.setFirstDayOfWeek(Calendar.MONDAY);

        String datestr = time_in_min.format(now.getTime());
        int AM_PM = -1;//凌晨＝1;早上＝1，上午＝1，中午＝2，下午＝2，傍晚＝2，晚上＝2
        for (int i = 0; i < TagList.size(); i++) {
            String token = inputList.get(i);
            String tag = TagList.get(i);
            if (tag.equals("N")) {
                if (token.equals("早上") || token.equals("凌晨") || token.equals("上午") || token.equals("清早")) {
                    AM_PM = 1;
                    DoneList.set(i, true);
                }
                if (token.equals("中午") || token.equals("下午") || token.equals("傍晚") || token.equals("晚上")) {
                    AM_PM = 2;
                    DoneList.set(i, true);
                }
                if (token.equals("今天")) {
                    datestr = testDateFormat("1天", calendar, now, AM_PM);
                    DoneList.set(i, true);
                }
                if (token.equals("明天")) {
                    datestr = testDateFormat("2天", calendar, now, AM_PM);
                    DoneList.set(i, true);
                }
                if (token.equals("後天")) {
                    datestr = testDateFormat("3天", calendar, now, AM_PM);
                    DoneList.set(i, true);
                }

                if(token.contains("星期")||token.contains("禮拜")||token.contains("週")){
                    int index = 0 , two = 0;
                    if(jump==0){
                        temp.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
                        temp.add(Calendar.DATE,-7);
                    }
                    if(token.equals("禮拜")||token.equals("星期")){
                        datestr = testDateFormat("7天", calendar, now, AM_PM);
                        DoneList.set(i, true);
                        DoneList.set(i + 1, true);
                    }
                    if(i < TagList.size() - 1 && token.contains("禮拜")){
                        index = ChiToNumber(inputList.get(i + 1));
                        if(inputList.get(i + 1).equals("天")) index=8;
                        two = 1;
                        Log.e("index" , String.valueOf(index));
                    }
                    if(token.equals("禮拜天")){
                        index=8;
                        Log.e("index" , String.valueOf(index));
                    }
                    if(token.length() > 2 && token.contains("星期")){
                        index = ChiToNumber(token.split("星期")[1]);
                        if(token.split("星期")[1].equals("天")) index=8;
                        Log.e("index" , String.valueOf(index));
                    }
                    if(token.length() > 1 && token.contains("週")){
                        index = ChiToNumber(token.split("週")[1]);
                        if(token.split("週")[1].equals("天")) index=8;
                        Log.e("index" , String.valueOf(index));
                    }

                    switch (index) {
                        case 1:
                            datestr = testDateFormat("8天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 2:
                            datestr = testDateFormat("9天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 3:
                            datestr = testDateFormat("10天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 4:
                            datestr = testDateFormat("11天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 5:
                            datestr = testDateFormat("12天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 6:
                            datestr = testDateFormat("13天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 7:
                            datestr = testDateFormat("14天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                        case 8:
                            datestr = testDateFormat("14天", temp, now, AM_PM);
                            DoneList.set(i, true);
                            if (jump >= 1) DoneList.set(i - 1, true);
                            if (jump == 2) DoneList.set(i - 2, true);
                            if (two != 0) DoneList.set(i + 1, true);
                            break;
                    }
                }

            }
            if (tag.equals("POST")) {
                if (token.equals("半")) {
                    datestr = testDateFormat("30分", calendar, now, AM_PM);
                    DoneList.set(i, true);
                }
            }
            if (tag.equals("N") && testDateFormat(token, calendar, now, AM_PM) != null) { //CKIP解析成名詞
                datestr = testDateFormat(token, calendar, now, AM_PM);
                DoneList.set(i, true);
            }
            if (tag.equals("M")){
                if(token.equals("個")){
                    jump+=1;
                    continue;
                }
            }
            if (tag.equals("DET") && i < TagList.size() - 1) {//CKIP解析成名詞
                if (inputList.get(i + 1).equals("號") || inputList.get(i + 1).equals("分") || inputList.get(i + 1).equals("點")) {
                    if (token.indexOf('點') > 0) {
                        datestr = testDateFormat(token.split("點")[0] + "點", calendar, now, AM_PM);
                        datestr = testDateFormat(token.split("點")[1] + inputList.get(i + 1), calendar, now, AM_PM);
                        DoneList.set(i, true);
                        DoneList.set(i + 1, true);
                    } else {
                        token += inputList.get(i + 1);
                        datestr = testDateFormat(token, calendar, now, AM_PM);
                        DoneList.set(i, true);
                        DoneList.set(i + 1, true);
                    }
                }
                if(token.equals("下")||token.equals("這")){
                    temp.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
                    if(token.equals("這")){
                        temp.add(Calendar.DATE,-7);
                    }
                    Log.e("本週日",time_in_day.format(temp.getTime()));
                    jump+=1;
                    continue;
                }
            }
        }
        return datestr;
    }

    /**
     * 找是否為日期格式
     * @param str
     * @return 日期
     */
    public String testDateFormat(String str, Calendar calendar, Calendar now, int ampm) {
        int lastindex = str.length() - 1;
        int num = getNumber(str.substring(0, lastindex));
        char comp = str.charAt(lastindex);
        if (num > 0 && (comp == '年' || comp == '月' || comp == '日' || comp == '時' || comp == '號' || comp == '點' || comp == '分' || comp == '天')) {
            switch (comp) {
                case '年':
                    calendar.set(Calendar.YEAR, num);
                    break;
                case '月':
                    calendar.set(Calendar.MONTH, num - 1);
                    break;
                case '日':
                    calendar.set(Calendar.DAY_OF_MONTH, num);
                    break;
                case '號':
                    calendar.set(Calendar.DAY_OF_MONTH, num);
                    break;
                case '時':
                    if (ampm > 0) {
                        switch (ampm) {
                            case 1:
                                num = num % 12;
                                calendar.set(Calendar.AM_PM, Calendar.AM);
                                calendar.set(Calendar.HOUR, num);
                                break;
                            case 2:
                                num = num % 12;
                                calendar.set(Calendar.AM_PM, Calendar.PM);
                                calendar.set(Calendar.HOUR, num);
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (num < 12) {
                            calendar.set(Calendar.AM_PM, Calendar.AM);
                            calendar.set(Calendar.HOUR, num);
                        }
                        if (num >= 12) {
                            num = num % 12;
                            calendar.set(Calendar.AM_PM, Calendar.PM);
                            calendar.set(Calendar.HOUR, num);
                        }
                    }
                    break;
                case '點':
                    if (ampm > 0) {
                        switch (ampm) {
                            case 1:
                                num = num % 12;
                                calendar.set(Calendar.AM_PM, Calendar.AM);
                                calendar.set(Calendar.HOUR, num);
                                break;
                            case 2:
                                num = num % 12;
                                calendar.set(Calendar.AM_PM, Calendar.PM);
                                calendar.set(Calendar.HOUR, num);
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (num < 12) {
                            calendar.set(Calendar.AM_PM, Calendar.AM);
                            calendar.set(Calendar.HOUR, num);
                        }
                        if (num >= 12) {
                            num = num % 12;
                            calendar.set(Calendar.AM_PM, Calendar.PM);
                            calendar.set(Calendar.HOUR, num);
                        }
                    }
                    break;
                case '分':
                    calendar.set(Calendar.MINUTE, num);
                    break;
                case '天':
                    if (num == 2) calendar.add(Calendar.DATE, +1);
                    if (num == 3) calendar.add(Calendar.DATE, +2);
                    if (num == 7) calendar.add(Calendar.DATE, +7);
                    //下週
                    if (num == 8) calendar.add(Calendar.DATE, +2);
                    if (num == 9) calendar.add(Calendar.DATE, +3);
                    if (num == 10) calendar.add(Calendar.DATE, +4);
                    if (num == 11) calendar.add(Calendar.DATE, +5);
                    if (num == 12) calendar.add(Calendar.DATE, +6);
                    if (num == 13) calendar.add(Calendar.DATE, +7);
                    if (num == 14) calendar.add(Calendar.DATE, +8);

                    break;
            }
            if(calendar.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY))
                return time_in_day.format(calendar.getTime());
            else if(calendar.get(Calendar.MINUTE) == now.get(Calendar.MINUTE)){
                return time_in_hour.format(calendar.getTime());
            }else{
            return time_in_min.format(calendar.getTime());
            }
        }
        return null;
    }

    /**
     * 找字串中有沒有數字
     * @param str
     * @return int
     */
    private int getNumber(String str) {
        String temp = "";
        Pattern pattern = Pattern.compile("[一二三四五六七八九十零百]*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            if (matcher.group().length() > 0) {
                return ChiToNumber(matcher.group());
            }
        }
        if (temp.equals("")) {
            for (int i = 0; i < str.length(); i++) {
                if (Character.isDigit(str.charAt(i))) {
                    temp += str.charAt(i);
                } else {
                    break;
                }
            }
            if (!temp.equals("")) {
                return Integer.parseInt(temp);
            }
        }
        return -1;
    }

    /**
     * 把中文數字轉成阿拉伯數字(到百位數)
     * @param s
     * @return 阿拉伯數字
     */
    private int ChiToNumber(String s) {
        int ithousand = 0, ihundred = 0, itens = 0, itemp = 0;
        for (int i = 0; i < s.length(); i++) {
            if (ChiToDigit(s.charAt(i)) > 0) {
                itemp = ChiToDigit(s.charAt(i));
            } else {
                switch (s.charAt(i)) {
                    case '千':
                        if (itemp == 0) itemp = 1;
                        ithousand = 1000 * itemp;
                        itemp = 0;
                    case '百':
                        if (itemp == 0) itemp = 1;
                        ihundred = 100 * itemp;
                        itemp = 0;
                        break;
                    case '十':
                        if (itemp == 0) itemp = 1;
                        itens = 10 * itemp;
                        itemp = 0;
                        break;
                    default:
                        break;
                }
            }
        }
        if ((ithousand + ihundred + itens + itemp) == 0) return -1;
        return ithousand + ihundred + itens + itemp;
    }

    /**
     * 把中文字轉數字
     * @param c
     * @return 數字0~9
     */
    private int ChiToDigit(char c) {
        switch (c) {
            case '零':
                return 0;
            case '一':
                return 1;
            case '二':
                return 2;
            case '三':
                return 3;
            case '四':
                return 4;
            case '五':
                return 5;
            case '六':
                return 6;
            case '七':
                return 7;
            case '八':
                return 8;
            case '九':
                return 9;
            default:
                return -1;
        }
    }

    public String getLocation() throws IOException, JSONException {
        int size = TagList.size();
        Boolean[] temp = new Boolean[size];
        for (int i = 0; i < size; i++) {
            temp[i]=false;
        }
        String request = "";
        String result;
        Location location = locateUser();

        for (int i = 0; i < size; i++) {
            String token = inputList.get(i);
            if(!DoneList.get(i) && (TagList.get(i).equals("N")||TagList.get(i).equals("Vi")||TagList.get(i).equals("N"))){
                if(!(inputList.get(i).contains("你")||inputList.get(i).contains("我")||inputList.get(i).contains("他")||inputList.get(i).contains("在")||inputList.get(i).contains("去")||inputList.get(i).contains("吃"))) {
                    request += token + ",";
                    temp[i] = true;
                }
            }
        }
        result = JsonToPlaceName(connectToPlaceApi(location,5000,request));
        request = "";
        if(result!=null){
            for (int i = 0; i < size; i++) {
                if(temp[i]){
                    if(result.contains(inputList.get(i))) {
                        DoneList.set(i, true);
                        request +=  inputList.get(i) + ",";
                    }
                }
            }
        }
        result = JsonToPlaceName(connectToPlaceApi(location,5000,request));
        return result;
    }

    public String getPerson() {
        ArrayList<String[]> contact = getContactsName();
        for (int i = 0; i < TagList.size(); i++) {
            if (TagList.get(i).equals("N") && DoneList.get(i).equals(false)) {
                String token = inputList.get(i);
                for (int j = 0; j < contact.size(); j++) {
                    if (token.equals(contact.get(j)[0])) {
                        return token;
                    }
                }
            }
        }
        return contactName;
    }

    public ArrayList<String[]> getContactsName() {
        ArrayList<String[]> contactinfo = new ArrayList<>();
        ContentResolver contentResolver = mcontext.getContentResolver();  //取得內容解析器
        //取得所有聯絡人
        Cursor contact_NAME = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (contact_NAME.moveToNext()) {
            String[] contacts = new String[2];
            long id = contact_NAME.getLong(contact_NAME.getColumnIndex(ContactsContract.Contacts._ID));//取的名字ID
            Cursor contact_NUM = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + Long.toString(id), null, null); //利用ID搜尋號碼
            while (contact_NUM.moveToNext()) {
                contacts[1] = contact_NUM.getString(contact_NUM.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");
            }
            contact_NUM.close();
            contacts[0] = contact_NAME.getString(contact_NAME.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contactinfo.add(contacts);
        }
        return contactinfo;
    }

    private Location locateUser() {
        LocationManager locationManager = (LocationManager) mcontext.getSystemService(mcontext.LOCATION_SERVICE); // 取得裝置的定位服務
        String bestProvider = LocationManager.GPS_PROVIDER; // 指定最佳定位是用 GPS 定位
        Criteria criteria = new Criteria(); // Criteria 會依照裝置的定位設定依狀況幫你取得裝置位置，見備註1
        bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider); // 取得最後定位到的位置
        if(location != null) {
            return location;
        }
        return null;
    }

    private String connectToPlaceApi(Location location, int radius ,String query) throws IOException {
        String result = "";
        if(location!=null){
            URL = URL + "&location=" + location.getLatitude() + "," + location.getLongitude();
            URL = URL + "&radius=" + Integer.toString(radius);
        }
        URL = URL + "&language=zh-TW";
        URL = URL + "&query=" + query;
        URL = URL + "&key=" + Key;

        HttpClient client = null;
        try {
            client = new DefaultHttpClient();

            HttpGet get = new HttpGet(URL);

            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return result;
    }

    private String JsonToPlaceName(String json) throws JSONException {
        try{
            j = new JSONObject(json);
            Object jsonOb = j.getJSONArray("results").getJSONObject(0).get("name");
            return (String) jsonOb;
        }catch(Exception e) {
            Log.e(TAG,"can not convert from json.");
        }
        return null;
    }

    private String JsonToPlaceID(String json) throws JSONException {
        try{
            j = new JSONObject(json);
            Object jsonOb = j.getJSONArray("results").getJSONObject(0).get("place_id");
            return (String) jsonOb;
        }catch(Exception e) {
            Log.e(TAG,"can not convert from json.");
        }
        return null;
    }
}

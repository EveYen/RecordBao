package com.eveyen.RecordBao.CKIP;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.cheyingwu.ckip.CKIP;
import tw.cheyingwu.ckip.Term;

/**
 *  作者：EveYen
 *  最後修改日期：10/13
 *  完成功能：CKIP
 */
public class Text_mining {

    static ArrayList<String> inputList; //宣告動態陣列 存切詞的name
    static ArrayList<String> TagList;   //宣告動態陣列 存切詞的詞性
    static String DateFormat;
    Calendar c;

    public Text_mining(String textString){

        inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
        TagList = new ArrayList<String>();
        String CKIP_IP = "140.109.19.104";
        int CKIP_PORT = 1501;
        String CKIP_USERNAME = "eve223267";
        String CKIP_PASSWORD = "convallaria0824";
        CKIP connect = new CKIP(CKIP_IP, CKIP_PORT, CKIP_USERNAME,
                CKIP_PASSWORD);
        connect.setRawText(textString);
        connect.send();

        for (Term t : connect.getTerm()) {
            inputList.add(t.getTerm()); // t.getTerm()會讀到斷詞的String，將其存到inputList陣列
            TagList.add(t.getTag());    // t.getTag() 會讀到斷詞的詞性，將其存到TagList陣列
        }
    }

    public ArrayList<String> getInputList(){
        return inputList;
    }

    public ArrayList<String> getTagList(){
        return TagList;
    }


    /**
     * 找名詞內含有(中英)數字＋年月日
     *
     */
    public ArrayList<String> getDate(){
        c = Calendar.getInstance();
        int ampm=-1; //凌晨＝1;早上＝1，上午＝1，中午＝2，下午＝2，傍晚＝2，晚上＝2
        ArrayList<String> DateList = new ArrayList<String>(); //宣告動態陣列 存時間
        for(int i = 0;i<TagList.size();i++){
            String token = inputList.get(i);
            String tag = TagList.get(i);
            if(tag.equals("N")){
                if(token.equals("早上")||token.equals("凌晨")||token.equals("上午")||token.equals("清早")) ampm = 1;
                if(token.equals("中午")||token.equals("下午")||token.equals("傍晚")||token.equals("晚上")) ampm = 2;
            }
            if(tag.equals("N") && testDateFormat(token,c,ampm)!=null){ //CKIP解析成名詞
                DateList.add(testDateFormat(token,c,ampm));
            }
            if(tag.equals("DET")&& i<TagList.size()-1) {//CKIP解析成名詞
                if(inputList.get(i + 1).equals("號")||inputList.get(i + 1).equals("分")||inputList.get(i + 1).equals("點")){
                    if(token.indexOf('點')>0){
                        DateList.add(testDateFormat(token.split("點")[0]+"點",c,ampm));
                        DateList.add(testDateFormat(token.split("點")[1]+inputList.get(i+1),c,ampm));
                    }
                    else {
                        token+=inputList.get(i+1);
                        DateList.add(testDateFormat(token,c,ampm));
                    }
                }
            }
        }
        return DateList;
    }

    /**
     * 找是否為日期格式
     * @param str
     * @return 日期
     */
    public String testDateFormat(String str,Calendar calendar,int ampm){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int lastindex = str.length()-1;
        int num = getNumber(str.substring(0,lastindex));
        char comp = str.charAt(lastindex);
        if(num > 0 && (comp=='年'||comp=='月'||comp=='日'||comp=='時'||comp=='點'||comp=='分')){
            switch (comp){
                case '年':
                    calendar.set(calendar.YEAR,num);
                    break;
                case '月':
                    calendar.set(calendar.MONTH,num-1);
                    break;
                case '日':
                    calendar.set(calendar.DAY_OF_MONTH,num);
                    break;
                case '號':
                    calendar.set(calendar.DAY_OF_MONTH,num);
                    break;
                case '時':
                    if(ampm>0){
                        switch (ampm){
                            case 1:
                                num = num%12;
                                calendar.set(calendar.AM_PM,Calendar.AM);
                                calendar.set(calendar.HOUR,num);
                                break;
                            case 2:
                                num = num%12;
                                calendar.set(calendar.AM_PM,Calendar.PM);
                                calendar.set(calendar.HOUR,num);
                                break;
                            default:
                                break;
                        }
                    }
                    else{
                        if(num<12){
                            calendar.set(calendar.AM_PM,Calendar.AM);
                            calendar.set(calendar.HOUR,num);
                        }
                        if(num>=12){
                            num = num%12;
                            calendar.set(calendar.AM_PM,Calendar.PM);
                            calendar.set(calendar.HOUR,num);
                        }
                    }
                    break;
                case '點':
                    if(ampm>0){
                        switch (ampm){
                            case 1:
                                num = num%12;
                                calendar.set(calendar.AM_PM,Calendar.AM);
                                calendar.set(calendar.HOUR,num);
                                break;
                            case 2:
                                num = num%12;
                                calendar.set(calendar.AM_PM,Calendar.PM);
                                calendar.set(calendar.HOUR,num);
                                break;
                            default:
                                break;
                        }
                    }
                    else{
                        if(num<12){
                            calendar.set(calendar.AM_PM,Calendar.AM);
                            calendar.set(calendar.HOUR,num);
                        }
                        if(num>=12){
                            num = num%12;
                            calendar.set(calendar.AM_PM,Calendar.PM);
                            calendar.set(calendar.HOUR,num);
                        }
                    }
                    break;
                case '分':
                    calendar.set(calendar.MINUTE,num);
                    break;
            }
            return df.format(calendar.getTime());
        }
        return null;
    }

    /**
     * 找字串中有沒有數字
     * @param str
     * @return int
     */

    private int getNumber(String str){
        String temp = "";
        Pattern pattern = Pattern.compile("[一二三四五六七八九十零百]*");
        Matcher matcher = pattern.matcher(str);
        if(matcher.matches()){
            if(matcher.group().length()>0) {
                return ChiToNumber(matcher.group());
            }
        }
        if(temp.equals("")){
            for(int i=0;i<str.length();i++){
                if(Character.isDigit(str.charAt(i))){
                    temp += str.charAt(i);
                }
                else{
                    break;
                }
            }
            if(!temp.equals("")){
                Log.e("Number",temp);
                return Integer.parseInt(temp);
            }
        }
        return -1;
    }

    /**
     * 把中文數字轉成阿拉伯數字(到百位數)
     * @param s
     * @return  阿拉伯數字
     */
    private int ChiToNumber(String s){
        int ithousand=0,ihundred=0,itens=0,itemp=0;
        for(int i = 0; i<s.length() ; i++){
            if(ChiToDigit(s.charAt(i))>0){
                itemp=ChiToDigit(s.charAt(i));
            }
            else{
                switch (s.charAt(i)){
                    case '千':
                        if(itemp==0) itemp=1;
                        ithousand = 1000*itemp;
                        itemp = 0;
                    case '百':
                        if(itemp==0) itemp=1;
                        ihundred = 100*itemp;
                        itemp = 0;
                        break;
                    case '十':
                        if(itemp==0) itemp=1;
                        itens = 10*itemp;
                        itemp = 0;
                        break;
                    default:
                        break;
                }
            }
        }
        if((ithousand+ihundred+itens+itemp)==0) return -1;
        return ithousand+ihundred+itens+itemp;
    }

    /**
     * 把中文字轉數字
     * @param c
     * @return 數字0~9
     */
    private int ChiToDigit(char c){
        switch (c){
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

    public String getLocation(){
        for(int i = 0;i<TagList.size()-1;i++){
            if(TagList.get(i).equals("P")){
                return inputList.get(i+1);
            }
        }
        return "";
    }
}

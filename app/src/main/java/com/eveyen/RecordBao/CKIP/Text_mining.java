package com.eveyen.RecordBao.CKIP;

import android.util.Log;

import java.util.ArrayList;
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

        ArrayList<String> DateList = new ArrayList<String>(); //宣告動態陣列 存時間
        for(int i = 0;i<TagList.size();i++){
            String token = inputList.get(i);
            String tag = TagList.get(i);
            if(tag.equals("N") && testDateFormat(token)!=null){ //CKIP解析成名詞
                DateList.add(testDateFormat(token));
            }
            if(tag.equals("DET")&& i<TagList.size()-1){ //CKIP解析成名詞
                if(inputList.get(i+1).equals("號"))
                    DateList.add(token+"日");
            }
            if(tag.equals("DET")&& i<TagList.size()-1){
                if(inputList.get(i+1).equals("分"))
                    DateList.add(token+"分");
            }
        }
        return DateList;
    }

    /**
     * 找是否為日期格式
     * @param str
     * @return 日期
     */
    public String testDateFormat(String str){
        int lastindex = str.length()-1;
        int num = getNumber(str.substring(0,lastindex));
        char comp = str.charAt(lastindex);
        if(num > 0 && (comp=='年'||comp=='月'||comp=='日'||comp=='時'||comp=='點'||comp=='分')) return Integer.toString(num)+comp;
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

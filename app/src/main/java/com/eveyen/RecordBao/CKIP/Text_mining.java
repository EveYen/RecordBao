package com.eveyen.RecordBao.CKIP;

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
            if(TagList.get(i).equals("N")){
                
                int lastindex = inputList.get(i).length()-1;
                switch (inputList.get(i).charAt(lastindex)){
                        case '年':
                        DateList.add(inputList.get(i));
                        break;
                    case '月':
                        DateList.add(inputList.get(i));
                        break;
                    case '日':
                        DateList.add(inputList.get(i));
                        break;
                    case '點':
                        DateList.add(inputList.get(i));
                        break;
                    case '分':
                        DateList.add(inputList.get(i));
                        break;
                    default:
                        break;
                }
            }
            if(TagList.get(i).equals("DET")&& i<TagList.size()-1){
                if(inputList.get(i+1).equals("號")) DateList.add(2,inputList.get(i)+"日");
            }
        }
        return DateList;
    }

    public boolean isDateFormat(String str){
        int lastindex = str.length()-1;
        int num = getNumber(str.substring(0,lastindex-1));
        if(num>0 && str.charAt(lastindex)=='年') return true;
        return false;
    }
    private int getNumber(String str){
        String temp=null;
        Pattern pattern = Pattern.compile("[一二三四五六七八九十]");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            temp = matcher.group();
            for(int j=0;j<temp.length();j++){
                if(temp.charAt(j)=='十'){
                    if(j==0) temp="10";
                }
            }
        }
        if(temp==null){
            for(int i=0;i<str.length();i++){
                if(Character.isDigit(str.charAt(i))){
                    temp += str.charAt(i);
                }
                else{
                    break;
                }
            }
        }
        if(temp!=null){
            return Integer.parseInt(temp);
        }
        return -1;
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

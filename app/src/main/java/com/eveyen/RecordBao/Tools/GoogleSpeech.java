package com.eveyen.RecordBao.Tools;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：與Google speech連線
 */
public class GoogleSpeech {

    public static HttpURLConnection getConnection(){
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

    public static String getTextString(String textString)
    {
        String returnStr="---";
        if(textString.split("transcript\":\"").length>1) {
            returnStr = textString.split("transcript\":\"")[1].split("\"")[0];
        }
        return returnStr;
    }
}

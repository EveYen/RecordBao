package com.eveyen.RecordBao.Tools;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：回傳檔案路徑/讀byte檔/上傳SQLite
 */
public class Data_Function {
    private static SQL_implement item;

    public static String getRootPath() {

        String rootPath = "/WAV/";
        String ROOT = "";// /storage/emulated/0
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
            Log.e("app", "系统方法：" + ROOT);
        }
        rootPath = ROOT + rootPath;

        File lrcFile = new File(rootPath);
        if (!lrcFile.exists()) {
            lrcFile.mkdirs();
        }
        return rootPath;
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
    public static String getFilepath(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date date = new Date();
        String name = "note_" + sdf.format(date);
        String voicePath = Data_Function.getRootPath() + name + ".wav";
        return voicePath;
    }

    public static void saveData(Context c,String Title, String getText, String voicePath, String sdate, String sloca, String sche, String contact){
        int[] colors = {Color.parseColor("#ffa6bc"),//粉紅
                Color.parseColor("#FFA7CDE2"),//天空藍
                Color.parseColor("#FFFFEF9F"),//鵝黃色
                Color.parseColor("#FFB8FFBC"),//草綠色
                Color.parseColor("#FFFFC894"),//橘色
        };
        int r = (int)(Math.random()*5-1);
        item = new SQL_implement(c);
        SQL_Item temp = new SQL_Item(0, new Date().getTime(), colors[r] , Title, getText, voicePath, sdate, sloca, sche, contact,0);
        temp=item.insert(temp);
        Log.e("SAVE","ID="+temp.getId()+" , date="+temp.getDatetime()+" , Title="+temp.getTitle()+" , contant="+temp.getContent()+" , top="+temp.getTop());
    }
}

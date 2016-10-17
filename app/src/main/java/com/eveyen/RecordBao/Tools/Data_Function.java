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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  作者：EveYen
 *  最後修改日期：10/18
 *  完成功能：搜尋檔案/回傳檔案路徑
 */
public class Data_Function {

    private static List<File> list = new ArrayList<File>();
    private static SQL_implement item;

    public static List<File> search(File file, String[] ext) {
        list.clear();
        searchFile(file, ext);
        return list;
    }
    private static void searchFile(File file, String[] ext) {
        if (file != null) {
            if (file.isDirectory()) {//檢查是否是目錄
                File[] listFile = file.listFiles();// listFiles()把目前所有的檔案都列出來
                if (listFile != null) {
                    for (int i = 0; i < listFile.length; i++) {
                        if (file.canRead()) {
                            searchFile(listFile[i], ext);//因是資料夾所以要往下找
                        }
                    }
                }
            } else {
                String filename = file.getAbsolutePath();
                // file.getName();// 加入名稱
                // file.getPath();// 加入路徑
                // file.length(); // 加入文件大小
                for (int i = 0; i < ext.length; i++) {
                    if (filename.endsWith(ext[i])) {
                        list.add(file);
                    }
                }
            }
        }
    }
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

    public static void saveData(Context c,String Title, String getText, String voicePath){
        int[] colorset={Color.argb(255,255,201,181),Color.argb(255,192,185,221),Color.argb(255,197,255,216),Color.argb(255,244,241,139),Color.argb(255,169,211,255)};
        int r = (int)(Math.random()* 5);
        item = new SQL_implement(c);
        SQL_Item temp = new SQL_Item(0, new Date().getTime(), colorset[r], Title, getText, voicePath, 0);
        item.insert(temp);
    }
}

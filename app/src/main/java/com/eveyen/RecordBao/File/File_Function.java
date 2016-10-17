package com.eveyen.RecordBao.File;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 *  作者：EveYen
 *  最後修改日期：9/6
 *  完成功能：搜尋檔案/回傳檔案路徑
 */
public class File_Function {
    private static List<File> list = new ArrayList<File>();

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

}

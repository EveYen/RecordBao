package com.eveyen.RecordBao.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by eveyen on 2016/9/8.
 */
public class SQL_implement {
    // 表格名稱
    public static final String TABLE_NAME = "Note";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String DATETIME_COLUMN = "datetime";
    public static final String COLOR_COLUMN = "color";
    public static final String TITLE_COLUMN = "title";
    public static final String CONTENT_COLUMN = "content";
    public static final String FILENAME_COLUMN = "filename";
    public static final String SDATE_COLUMN = "sdate";
    public static final String SLOCA_COLUMN = "sloca";
    public static final String SCHEDULE_COLUMN = "schedule";
    public static final String CONTACT_COLUMN = "contact";
    public static final String TOP_COLUMN = "top";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATETIME_COLUMN + " INTEGER NOT NULL, " +
                    COLOR_COLUMN + " INTEGER NOT NULL, " +
                    TITLE_COLUMN + " TEXT NOT NULL, " +
                    CONTENT_COLUMN + " TEXT, " +
                    FILENAME_COLUMN + " TEXT, " +
                    SDATE_COLUMN + " TEXT, "+
                    SLOCA_COLUMN + " TEXT, "+
                    SCHEDULE_COLUMN + " TEXT, "+
                    CONTACT_COLUMN + " TEXT, "+
                    TOP_COLUMN + " INTEGER NOT NULL )";

    private static SQLiteDatabase db;

    public SQL_implement(Context context) {
        db = SQL_Initial.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }
    // 新增參數指定的物件
    public SQL_Item insert(SQL_Item item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(DATETIME_COLUMN, item.getDatetime());
        cv.put(COLOR_COLUMN, item.getColor());
        cv.put(TITLE_COLUMN, item.getTitle());
        cv.put(CONTENT_COLUMN, item.getContent());
        cv.put(FILENAME_COLUMN, item.getFileName());
        cv.put(SDATE_COLUMN, item.getScheduleDate());
        cv.put(SLOCA_COLUMN, item.getScheduleLocation());
        cv.put(SCHEDULE_COLUMN, item.getSchedule());
        cv.put(CONTACT_COLUMN, item.getContact());
        cv.put(TOP_COLUMN, item.getTop());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }


    // 修改參數指定的物件
    public boolean update(SQL_Item item,int top) {

        delete(item);
        item.setTop(top);
        item = insert(item);


        Log.e("啊啊啊啊啊啊啊", String.valueOf(item.getTop()));
        /*ContentValues cv = new ContentValues();
        cv.put(TOP_COLUMN, uptop);

        String where = KEY_ID + "=" + upid;
        */
        if(item!=null) return true;
        return false;
    }



    public boolean delete(SQL_Item item){
        String where = KEY_ID + "=" + item.getId() ;
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public static ArrayList<SQL_Item> getAll() {
        ArrayList<SQL_Item> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public SQL_Item get(long id) {
        // 準備回傳結果用的物件
        SQL_Item item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public static SQL_Item getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        SQL_Item result = new SQL_Item();

        result.setId(cursor.getLong(0));
        result.setDatetime(cursor.getLong(1));
        result.setColor(cursor.getInt(2));
        result.setTitle(cursor.getString(3));
        result.setContent(cursor.getString(4));
        result.setFileName(cursor.getString(5));
        result.setScheduleDate(cursor.getString(6));
        result.setScheduleLocation(cursor.getString(7));
        result.setSchedule(cursor.getString(8));
        result.setContact(cursor.getString(9));
        result.setTop(cursor.getInt(10));

        // 回傳結果
        return result;
    }


    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

}

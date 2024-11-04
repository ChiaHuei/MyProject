package com.example.myproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Date;

public class DB {
    private static final String TABLE_NAME = "notes";
    private SQLiteDatabase db;


    class MySQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(_id INTEGER PRIMARY KEY, come TEXT NOT NULL, type TEXT NOT NULL, money INTEGER, note TEXT,  date TEXT);";

        public MySQLiteOpenHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    public DB(Context context) {
        db = new MySQLiteOpenHelper(context).getReadableDatabase();
    }

    public Cursor getItemById(String itemId) {
        String[] projection = {
                "_id", // 假設 id 是主鍵
                "money",
                "come",
                "date",
                "type",
                "note"
        };

        // 使用 where 子句查找特定項目
        String selection = "_id = ?";
        String[] selectionArgs = { itemId };

        return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
    }


    public boolean create(String come, String type, int money, String note, String date){
        ContentValues cv = new ContentValues();
        cv.put("come", come);
        cv.put("type", type);
        cv.put("money", money);
        cv.put("note", note);
        cv.put("date", date);
        return db.insert(TABLE_NAME, null, cv)>0;
    }

    public boolean delete(long id){
        return db.delete(TABLE_NAME, "_id="+id, null)>0;
    }

    public boolean deleteAll() {
        // 執行刪除所有資料的 SQL 語句
        db.execSQL("DELETE FROM " + TABLE_NAME);
        return true; // 返回 true 代表操作成功
    }


    public boolean update(long id, String come, String type, int money, String note, String date){
        ContentValues cv = new ContentValues();
        cv.put("come", come);
        cv.put("type", type);
        cv.put("money", money);
        cv.put("note", note);
        cv.put("date", date);
        return db.update(TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor readAll(){
        return db.query(TABLE_NAME, new String[]{"_id", "come", "type", "money", "note", "date"}, null,null,null,null,null);
    }

    public Cursor read(String yearMonth) {
        String query = "SELECT * FROM notes WHERE date LIKE ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{yearMonth + "%"}); // 使用 LIKE 查詢當月資料
        return cursor;
    }

    public Cursor readMonthlyExpenses(String yearMonth) {
        String query = "SELECT type, SUM(money) as total FROM notes WHERE date LIKE ? AND come != '收入' GROUP BY type";
        return db.rawQuery(query, new String[]{yearMonth + "%"});
    }


}

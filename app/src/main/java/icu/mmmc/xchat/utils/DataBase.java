package icu.mmmc.xchat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import icu.xchat.core.entities.ServerInfo;

public class DataBase extends SQLiteOpenHelper {
    public DataBase(@Nullable Context context) {
        super(context, "xchat.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE \"t_servers\" (\n" +
                "  \"id\" integer NOT NULL,\n" +
                "  \"server_title\" text NOT NULL DEFAULT '',\n" +
                "  \"server_code\" text NOT NULL DEFAULT '',\n" +
                "  \"host\" text NOT NULL DEFAULT '',\n" +
                "  \"port\" integer NOT NULL DEFAULT 41321,\n" +
                "  PRIMARY KEY (\"id\")\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    @SuppressLint("Recycle")
    public void saveServer(String title, ServerInfo serverInfo) throws Exception {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_title FROM t_servers WHERE server_title = ?", new String[]{title});
        if (cursor.moveToFirst()) {
            throw new Exception("存在同名记录");
        }
        getWritableDatabase().execSQL("INSERT INTO t_servers (server_title,server_code,host,port) " +
                "VALUES(?,?,?,?)", new Object[]{title, serverInfo.getServerCode(), serverInfo.getHost(), serverInfo.getPort()});
    }

    public void delServer(int id) {
        getWritableDatabase().execSQL("DELETE FROM t_servers WHERE id = ?", new Object[]{id});
    }
}

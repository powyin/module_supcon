package com.supconit.hcmobile.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.net.DownInfo;
import com.supconit.hcmobile.net.HttpManager;
import com.supconit.hcmobile.net.NetState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DBOperator {
    private volatile static DBOperator mInstance;

    public static DBOperator getInstance() {
        if (mInstance == null) {
            synchronized (DBOperator.class) {
                if (mInstance == null) {
                    mInstance = new DBOperator();
                }
            }
        }
        return mInstance;
    }

    private Db db;

    private DBOperator() {
        db = new Db(HcmobileApp.getApplication(), "sup_con_common_db", null, 10);
    }


    /**
     * 加入表数据 根据jsonString
     */
    public String initDataByJsonArray(String table, JSONArray key_Content, JSONArray jsonArray) {
        if (jsonArray == null || TextUtils.isEmpty(table)) {
            Log.e("powyin", "jsonArray is null and table is null");
            return "jsonArray is null and table is null";
        }
        String error = null;
        SQLiteDatabase sb = db.getWritableDatabase();
        String[] arrayList = new String[key_Content.length()];
        for (int i = 0; i < key_Content.length(); i++) {
            Object o = key_Content.opt(i);
            String siName = o.toString();
            arrayList[i] = (siName);
        }
        sb.beginTransaction();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            try {
                ContentValues contentValues = copyValueToContentValues(arrayList, jsonArray.getJSONObject(i));
                if (contentValues.size() > 0) {
                    sb.insertOrThrow(table, "", contentValues);
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getMessage();
                break;
            }
        }
        sb.setTransactionSuccessful();
        sb.endTransaction();
        sb.close();
        return error;
    }

    /**
     * 执行sql 语句
     */
    public String exec(String sql) {
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        String ret;
        try {
            writableDatabase.execSQL(sql);
            ret = null;
        } catch (Exception e) {
            e.printStackTrace();
            ret = e.getMessage();
        } finally {
            writableDatabase.close();
        }
        return ret;
    }


    /**
     * 清除table表数据 保留表结构
     */
    public String cleanTable(String table) {
        if (TextUtils.isEmpty(table)) {
            return "table is null please checked it";
        }
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        try {
            writableDatabase.execSQL("DELETE FROM  " + table);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public void loadDa(String url) {
        SQLiteDatabase sl = SQLiteDatabase.openDatabase("null", null, 0);
        String uid = String.valueOf(url.hashCode());
        File db = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), uid, "db", false);

        if (!db.exists() || db.length() <= 0) {
            List<DownInfo> downInfo = HttpManager.fileDownLoadAsy(Collections.singletonMap(url, FileUtil.getRandomFilePath(HcmobileApp.getApplication(), null, null, false)));
            DownInfo down = downInfo.get(0);
            if (down.status == NetState.FINISH) {
                FileUtil.copy(down.localFilePath, db);
            }
        }
    }


    /**
     * json 实体转 ContentValues;
     */
    private static ContentValues copyValueToContentValues(String[] key_Content, JSONObject resource) {
        ContentValues target = new ContentValues();
        if (resource == null || key_Content == null) {
            return target;
        }
        for (String key : key_Content) {
            try {
                Object value = resource.opt(key);
                if (value == null) {
                    break;
                }
                if (value instanceof Boolean) {
                    target.put(key, resource.getBoolean(key));
                } else if (value instanceof Long) {
                    target.put(key, resource.getLong(key));
                } else if (value instanceof Integer) {
                    target.put(key, resource.getInt(key));
                } else if (value instanceof Double) {
                    target.put(key, resource.getDouble(key));
                } else if (value instanceof Float) {
                    target.put(key, resource.getDouble(key));
                } else {
                    target.put(key, value.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return target;

    }

    /**
     * 查询sql
     */
    public JSONArray query(String sql) throws Exception {
        SQLiteDatabase readableDatabase = db.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = readableDatabase.rawQuery(sql, null);
            JSONArray array = new JSONArray();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        JSONObject jsonObject = CursorToJsonObj(cursor);
                        if (jsonObject != null) {
                            array.put(jsonObject);
                        }
                    } while (cursor.moveToNext());
                }
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            readableDatabase.close();
        }
    }


    /**
     * 获取db
     */
    public SQLiteDatabase getReadableDatabase() {
        return db.getReadableDatabase();
    }

    public static JSONObject CursorToJsonObj(Cursor cursor) {
        try {
            String[] columnNames = cursor.getColumnNames();
            JSONObject object = new JSONObject();
            for (String name : columnNames) {
                int index = cursor.getColumnIndex(name);
                int type = cursor.getType(index);
                switch (type) {
                    case Cursor.FIELD_TYPE_NULL:
                        object.put(name, JSONObject.NULL);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        object.put(name, cursor.getString(index));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        object.put(name, cursor.getLong(index));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        object.put(name, cursor.getDouble(index));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        object.put(name, cursor.getBlob(index));
                        break;
                    default:
                        break;
                }
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Db extends SQLiteOpenHelper {
        private Db(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}

















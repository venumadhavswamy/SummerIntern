package com.rgukt.summerintern;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String CONTACTS_TABLE = "contactsTable";
    private static final String NUMBER_KEY = "number";
    private static final String NAME_KEY = "name";
    private static final String CAN_SHARE_LOCATION_KEY = "canShareLocation";
    private static final String CAN_SEND_MESSAGE_KEY = "canSendMessage";

    DatabaseHelper(Context context)
    {
        super(context,CONTACTS_TABLE,null,1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE);
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " +CONTACTS_TABLE +
                " (" + NUMBER_KEY+ " TEXT PRIMARY KEY, " +
                NAME_KEY + "TEXT," +
                CAN_SEND_MESSAGE_KEY + " INTEGER DEFAULT 0, " +
                CAN_SHARE_LOCATION_KEY+ " INTEGER DEFAULT 0);";

        db.execSQL(createTable);
    }

    public boolean insertContact(String number, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NUMBER_KEY, number);
        contentValues.put(NAME_KEY, name);

        long success = db.insert(CONTACTS_TABLE, null, contentValues);

        db.close();
        return success == -1;
    }

}

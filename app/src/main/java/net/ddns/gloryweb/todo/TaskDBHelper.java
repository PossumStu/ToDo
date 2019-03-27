package net.ddns.gloryweb.todo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;

public class TaskDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Tasks.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_COLUMN_ID = "_id";
    public static final String TASKS_COLUMN_NAME = "name";
    public static final String TASKS_COLUMN_REPEAT = "repeat";
    public static final String TASKS_COLUMN_WEEKLY = "weekly";
    public static final String TASKS_COLUMN_REP = "rep";
    public static final String TASKS_COLUMN_COUNT = "count";
    public static final String TASKS_COLUMN_M = "Mday";
    public static final String TASKS_COLUMN_T = "T";
    public static final String TASKS_COLUMN_W = "W";
    public static final String TASKS_COLUMN_R = "R";
    public static final String TASKS_COLUMN_F = "F";
    public static final String TASKS_COLUMN_S = "S";
    public static final String TASKS_COLUMN_SuDAY = "Su";
    public static final String TASKS_COLUMN_ACTIVE = "active";
    private Context context;


    public TaskDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    //////////////////////
    //Creates task table//
    //////////////////////
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TASKS_TABLE_NAME + "(" +
                TASKS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                TASKS_COLUMN_NAME + " TEXT, " +
                TASKS_COLUMN_REPEAT + " INTEGER," +
                TASKS_COLUMN_WEEKLY + " INTEGER, " +
                TASKS_COLUMN_REP + " INTEGER, " +
                TASKS_COLUMN_COUNT + " INTEGER, " +
                TASKS_COLUMN_SuDAY + " INTEGER, " +
                TASKS_COLUMN_M + " INTEGER, " +
                TASKS_COLUMN_T + " INTEGER, " +
                TASKS_COLUMN_W + " INTEGER, " +
                TASKS_COLUMN_R + " INTEGER, " +
                TASKS_COLUMN_F + " INTEGER, " +
                TASKS_COLUMN_S + " INTEGER, " +
                TASKS_COLUMN_ACTIVE + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
        onCreate(db);
    }

    //////////////////////
    //Creates a new task//
    //////////////////////
    public boolean insertTask(String name, int repeat, int weekly, int rep, int SuDay, int M, int T, int W, int R, int F, int S, int active) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASKS_COLUMN_NAME, name);
        contentValues.put(TASKS_COLUMN_REPEAT, repeat);
        contentValues.put(TASKS_COLUMN_WEEKLY, weekly);
        contentValues.put(TASKS_COLUMN_REP, rep);
        contentValues.put(TASKS_COLUMN_COUNT, 0);
        contentValues.put(TASKS_COLUMN_M, M);
        contentValues.put(TASKS_COLUMN_T, T);
        contentValues.put(TASKS_COLUMN_W, W);
        contentValues.put(TASKS_COLUMN_R, R);
        contentValues.put(TASKS_COLUMN_F, F);
        contentValues.put(TASKS_COLUMN_S, S);
        contentValues.put(TASKS_COLUMN_SuDAY, SuDay);
        contentValues.put(TASKS_COLUMN_ACTIVE, active);
        db.insert(TASKS_TABLE_NAME, null, contentValues);
        return true;
    }

    //////////////////////////////////////////////////////////////////
    //Returns cursor object that contains the entire task data table//
    //////////////////////////////////////////////////////////////////
    public Cursor getAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + TASKS_COLUMN_NAME + " FROM " + TASKS_TABLE_NAME, null);
        return res;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //Complex logic to determine whether or not a task's active bit should be on or off//
    /////////////////////////////////////////////////////////////////////////////////////
    public void ActivateTasks() {
        Date date = new Date();
        int day = date.getDay();
        System.out.println(this);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        /* dont think i need the shared pref right now
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String date1 = String.valueOf(date.getDate());
        String prevDate = sharedPreferences.getString("prevDate", date1);
        int cDate = Integer.parseInt(date1);
        int pDate = Integer.parseInt(prevDate);
        */


        //Weekly-based activity activator
        Cursor weekCursor = db.rawQuery("SELECT * FROM " + TASKS_TABLE_NAME + " WHERE " + TASKS_COLUMN_WEEKLY + " = 1", null);
        for (int i = 0; i < weekCursor.getCount(); i++) {
            weekCursor.moveToPosition(i);
            if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ACTIVE)) != 1) {
                //deletes non-repeating tasks that have been marked inactive
                if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REPEAT)) == 0) {
                    int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                    db.delete(TASKS_TABLE_NAME, TASKS_COLUMN_ID + " = " + del, null);
                }
                //Days of the week check
                if (day == 0 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_SuDAY)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 1 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_M)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 2 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_T)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 3 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_W)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 4 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_R)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 5 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_F)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                if (day == 6 && weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_S)) == 1) {
                    if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP))) {

                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    } else /*if (weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) != weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_REP)))*/ {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }

                //Count up if count != rep cycle
                //TODO Fix this so it doesn't count up every time task is activated, i.e. update daily


            }

        }


        //Monthly-based activity activator
        Cursor monthCursor = db.rawQuery("SELECT * FROM " + TASKS_TABLE_NAME + " WHERE " + TASKS_COLUMN_WEEKLY + " = 0", null);
        for (int i = 0; i < monthCursor.getCount(); i++) {
            monthCursor.moveToPosition(i);
            if (monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_ACTIVE)) != 1) {
                if (monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_COUNT)) == monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_REP))) {
                    if (monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_SuDAY)) == date.getDate()) {
                        int del = monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_ID));
                        contentValues.put(TASKS_COLUMN_ACTIVE, 1);
                        contentValues.put(TASKS_COLUMN_COUNT, 0);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }
                }
                //TODO Fix count so it is only when a month passes rather than every time the activator is called
                else {
                    //if the date today is the tasks active date, increase count by one
                    if (monthCursor.getInt(monthCursor.getColumnIndex(TASKS_COLUMN_SuDAY)) == date.getDate()) {
                        int del = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_ID));
                        int countup = weekCursor.getInt(weekCursor.getColumnIndex(TASKS_COLUMN_COUNT)) + 1;
                        contentValues.put(TASKS_COLUMN_COUNT, countup);
                        db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_ID + " = " + del, null);
                    }

                }
            }


        }
    }

    /////////////////////////////////////////////////
    //Sets active bit to 0 for task with given name//
    /////////////////////////////////////////////////
    public void DeactivateTask(String taskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASKS_COLUMN_ACTIVE, 0);
            db.update(TASKS_TABLE_NAME, contentValues, TASKS_COLUMN_NAME + " = '" + taskName + "'", null);

    }

    ///////////////////////////////////////////////
    //Returns a string array of active task names//
    ///////////////////////////////////////////////
    public ArrayList<String> getActiveNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> taskNames = new ArrayList<String>();
        Cursor res = db.rawQuery("SELECT " + TASKS_COLUMN_NAME + " FROM " + TASKS_TABLE_NAME + " WHERE " + TASKS_COLUMN_ACTIVE + " = 1", null);
        for (int i = 0; i < res.getCount(); i++) {
            res.moveToPosition(i);
            String string = res.getString(res.getColumnIndex("name"));
            taskNames.add(string);
        }
        return taskNames;
    }

    public Cursor getActiveCursor(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> taskNames = new ArrayList<String>();
        Cursor res = db.rawQuery("SELECT " + TASKS_COLUMN_NAME + " FROM " + TASKS_TABLE_NAME + " WHERE " + TASKS_COLUMN_ACTIVE + " = 1", null);
        return res;
    }

    public Integer deleteTask(String[] names) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TASKS_TABLE_NAME,
                TASKS_COLUMN_NAME + " = ?",
                names);
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
    }

    public void createTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE " + TASKS_TABLE_NAME + "(" +
                TASKS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                TASKS_COLUMN_NAME + " TEXT, " +
                TASKS_COLUMN_REPEAT + " INTEGER," +
                TASKS_COLUMN_WEEKLY + " INTEGER, " +
                TASKS_COLUMN_REP + " INTEGER, " +
                TASKS_COLUMN_COUNT + " INTEGER, " +
                TASKS_COLUMN_SuDAY + " INTEGER, " +
                TASKS_COLUMN_M + " INTEGER, " +
                TASKS_COLUMN_T + " INTEGER, " +
                TASKS_COLUMN_W + " INTEGER, " +
                TASKS_COLUMN_R + " INTEGER, " +
                TASKS_COLUMN_F + " INTEGER, " +
                TASKS_COLUMN_S + " INTEGER, " +
                TASKS_COLUMN_ACTIVE + " INTEGER)"
        );
    }
}

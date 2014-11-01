package org.damcode.workout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by dm on 1/10/2014.
 */
public class WorkoutDBAO implements Closeable {

    private static String GET_SESSIONS_FROM_WORKOUT = "SELECT rowid,* FROM " +
            WorkoutDataHelper.SESSIONS_TABLE_NAME + " WHERE workoutid = ";

    private static String GET_ALL_WORKOUTS = "SELECT rowid,* FROM " + WorkoutDataHelper.WORKOUT_TABLE_NAME;

    private static String GET_WORKOUT_TOTAL_TIME = "SELECT sum(time) FROM " +
            WorkoutDataHelper.SESSIONS_TABLE_NAME + " WHERE workoutid = ";

    private SQLiteDatabase reader;
    private SQLiteDatabase writer;
    private Context context;
    private WorkoutDataHelper helper;

    public WorkoutDBAO(Context context) {
        this.context = context;
    }

    public ArrayList<HashMap> getWorkouts() {
        initDb();

        ArrayList<HashMap> results = null;

        Cursor curs = reader.rawQuery(GET_ALL_WORKOUTS, null);

        if (curs.getCount() > 0) {
            results = new ArrayList<HashMap>();

            curs.moveToFirst();
            int nameCol = curs.getColumnIndex("name");
            int rowIdCol = curs.getColumnIndex("rowid");

            while (!curs.isAfterLast()) {
                HashMap h = new HashMap();
                h.put("name", curs.getString(nameCol));
                h.put("id", curs.getInt(rowIdCol));
                results.add(h);
                curs.moveToNext();
            }
            curs.close();
        }

        try {
            close();
        } finally {
            return results;
        }

    }


    public ArrayDeque<int[]> getBeepQueue(int workoutId){
        initDb();
        ArrayDeque<int[]> results = null;



        Cursor curs = reader.rawQuery(GET_SESSIONS_FROM_WORKOUT + Integer.toString(workoutId), null);

        if (curs.getCount() > 0) {
            results = new ArrayDeque();

            curs.moveToFirst();
            int timeCol = curs.getColumnIndex("time");
            int stepTypeCol = curs.getColumnIndex("type");
            int computedTime = 0;

            while (!curs.isAfterLast()) {
                computedTime += curs.getInt(timeCol);
                results.add(new int[]{computedTime,((int) curs.getInt(stepTypeCol))});
                curs.moveToNext();
            }
            curs.close();
        }
        try {
            close();
        } finally {
            return results;
        }

    }

    public ArrayList<HashMap> getSessions(int workoutId) {
        initDb();
        ArrayList<HashMap> results = null;

        Cursor curs = reader.rawQuery(GET_SESSIONS_FROM_WORKOUT + Integer.toString(workoutId), null);

        if (curs.getCount() > 0) {
            results = new ArrayList<HashMap>();

            curs.moveToFirst();
            int timeCol = curs.getColumnIndex("time");
            int rowIdCol = curs.getColumnIndex("rowid");
            int stepTypeCol = curs.getColumnIndex("type");

            while (!curs.isAfterLast()) {
                HashMap h = new HashMap();
                h.put("time", curs.getInt(timeCol));
                h.put("id", curs.getInt(rowIdCol));
                h.put("type", curs.getInt(stepTypeCol));
                results.add(h);
                curs.moveToNext();
            }
            curs.close();
        }
        try {
            close();
        } finally {
            return results;
        }
    }

    private void initDb() {
        helper = new WorkoutDataHelper(context);
        reader = helper.getReadableDatabase();
        writer = helper.getWritableDatabase();
    }

    public int addSession(int workoutid, int time, int type) {
        initDb();

        ContentValues v = new ContentValues();
        v.put("time", time);
        v.put("workoutid", workoutid);
        v.put("type", type);


        int sessionId = (int) writer.insert(WorkoutDataHelper.SESSIONS_TABLE_NAME, null, v);

        try {
            close();
        } finally {
            return sessionId;
        }
    }

    public int getWorkoutTotalTime(int rowid) {
        initDb();

        Cursor c = reader.rawQuery(GET_WORKOUT_TOTAL_TIME + Integer.toString(rowid), null);

        if (!c.moveToFirst()) {
            return 0;
        }

        int totalTime = c.getInt(0);
        try {
            close();
        } finally {
            return totalTime;
        }
    }

    public void removeSession(int rowId) {
        initDb();

        Log.d("DELETE", Integer.toString(rowId));
        writer.delete(WorkoutDataHelper.SESSIONS_TABLE_NAME, "rowid = " + Integer.toString(rowId), null);

        try {
            close();
        } catch (IOException e) {

        }
    }

    public void removeWorkout(int rowId) {
        initDb();

        Log.d("DELETE", Integer.toString(rowId));
        writer.delete(WorkoutDataHelper.WORKOUT_TABLE_NAME, "rowid = " + Integer.toString(rowId), null);
        writer.delete(WorkoutDataHelper.SESSIONS_TABLE_NAME, "workoutid = " + Integer.toString(rowId), null);

        try {
            close();
        } catch (IOException e) {

        }
    }

    public int addWorkout(String name) {
        initDb();

        ContentValues v = new ContentValues();
        v.put("name", name);
        int workoutId = (int) writer.insert(WorkoutDataHelper.WORKOUT_TABLE_NAME, null, v);
        try {
            close();
        } finally {
            return workoutId;
        }
    }


    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
        helper.close();
    }
}

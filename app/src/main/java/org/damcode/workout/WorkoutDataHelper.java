package org.damcode.workout;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dm on 1/10/2014.
 */
public class WorkoutDataHelper  extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "dmworkouts.db";
    public static final String WORKOUT_TABLE_NAME = "workouts";
    public static final String SESSIONS_TABLE_NAME = "sessions";
    public static final String CREATE_WORKOUT_TABLE = "CREATE TABLE " + WORKOUT_TABLE_NAME +
            " ( name TEXT )";
    public static final String CREATE_SESSIONS_TABLE = "CREATE TABLE " + SESSIONS_TABLE_NAME +
            " ( time NUMERIC, workoutid  INTEGER)";


    public WorkoutDataHelper(Context context) {
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WORKOUT_TABLE);
        db.execSQL(CREATE_SESSIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // do something!
    }
}

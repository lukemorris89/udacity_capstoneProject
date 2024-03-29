package com.example.capstoneproject.data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.capstoneproject.model.Test;

@Database(entities = {Test.class}, version = 2, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class TestDatabase extends RoomDatabase {
    private static final String LOG_TAG = TestDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "testData";
    private static TestDatabase sInstance;

    public abstract TestDao testDao();

    public static TestDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        TestDatabase.class,
                        TestDatabase.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }
}

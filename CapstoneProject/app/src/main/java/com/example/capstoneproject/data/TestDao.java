package com.example.capstoneproject.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.capstoneproject.model.Test;

import java.util.List;


@Dao
public interface TestDao {

    @Query("SELECT * FROM testData ORDER BY mTestID")
    LiveData<List<Test>> loadAllTests();

    //TODO add qualifier for today's tests only
    @Query("SELECT * FROM testData where mTestResult = :testGroup ORDER BY mTestID")
    LiveData<List<Test>> loadTestGroup(String testGroup);

    @Insert
    void insertTest(Test test);

    @Delete
    void deleteTest(Test test);
}


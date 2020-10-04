package com.example.capstoneproject.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.capstoneproject.model.Test;

import java.util.List;

@Dao
public interface TestDao {

    @Query("SELECT * FROM testData ORDER BY mTestID")
    LiveData<List<Test>> loadAllTests();

    @Query("SELECT * FROM testData where mTestResult = :testGroup ORDER BY mTestID")
    LiveData<List<Test>> loadTestGroup(String testGroup);

    @Query("SELECT * FROM testData where mTestID = :testId ORDER BY mTestID")
    Test loadSingleTestByID(int testId);

    @Insert
    void insertTest(Test test);

    @Update
    void updateTest(Test test);

    @Delete
    void deleteTest(Test test);

    @Query("SELECT * FROM testData ORDER BY mTestID")
    List<Test> loadAllTestsWidget();
}


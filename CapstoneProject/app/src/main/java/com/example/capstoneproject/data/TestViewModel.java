package com.example.capstoneproject.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.repository.TestRepository;

import java.util.List;

public class TestViewModel extends AndroidViewModel {
    private TestRepository mTestRepository;
    private LiveData<List<Test>> mAllTests;

    public TestViewModel(@NonNull Application application) {
        super(application);
        mTestRepository = new TestRepository(application);
        mAllTests = mTestRepository.getAllTests();
    }

    public LiveData<List<Test>> getAllTests() {
        return mAllTests;
    }

    public LiveData<List<Test>> getTestGroup(String testGroup) {
        return mTestRepository.getTestGroup(testGroup);
    }

    public Test getSingleTestById(int testId) {
        return mTestRepository.getSingleTestByID(testId);
    }

    public void insertTest(Test test) {
        mTestRepository.insertTest(test);
    }

    public void updateTest(Test test) {
        mTestRepository.updateTest(test);
    }
}

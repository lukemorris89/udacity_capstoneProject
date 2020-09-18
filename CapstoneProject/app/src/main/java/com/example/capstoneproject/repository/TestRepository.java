package com.example.capstoneproject.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.capstoneproject.data.TestDao;
import com.example.capstoneproject.data.TestDatabase;
import com.example.capstoneproject.model.Test;

import java.util.List;

public class TestRepository {
    private TestDao mTestDao;
    private LiveData<List<Test>> mGroupTests;

    public TestRepository(Application application) {
        TestDatabase db = TestDatabase.getInstance(application);
        mTestDao = db.testDao();
    }

    public LiveData<List<Test>> getAllTests() {
        return mTestDao.loadAllTests();
    }

    public LiveData<List<Test>> getTestGroup(String testGroup) {
        return mTestDao.loadTestGroup(testGroup);
    }

    public LiveData<Test> getSingleTestByID(int testId) {
        return mTestDao.loadSingleTestByID(testId);
    }

    public void insertTest(Test test) {
        InsertTestAsyncTask task = new InsertTestAsyncTask(mTestDao);
        task.execute(test);
    }

    public void updateTest(Test test) {
        UpdateTestAsyncTask task = new UpdateTestAsyncTask(mTestDao);
        task.execute(test);
    }

    private static class InsertTestAsyncTask extends AsyncTask<Test, Void, Void> {
        private TestDao asyncTaskDao;

        InsertTestAsyncTask(TestDao testDao) {
            asyncTaskDao = testDao;
        }

        @Override
        protected Void doInBackground(Test... tests) {
            asyncTaskDao.insertTest(tests[0]);
            return null;
        }
    }

    private static class UpdateTestAsyncTask extends AsyncTask<Test, Void, Void> {
        private TestDao asyncTaskDao;

        UpdateTestAsyncTask(TestDao testDao) {
            asyncTaskDao = testDao;
        }

        @Override
        protected Void doInBackground(Test... tests) {
            asyncTaskDao.updateTest(tests[0]);
            return null;
        }
    }
}

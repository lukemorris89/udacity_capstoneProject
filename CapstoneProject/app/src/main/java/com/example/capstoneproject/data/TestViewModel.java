package com.example.capstoneproject.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.capstoneproject.model.Test;

import java.util.List;

public class TestViewModel extends AndroidViewModel {
    private LiveData<List<Test>> tests;
    private LiveData<List<Test>> positiveTests;
    private LiveData<List<Test>> negativeTests;
    private LiveData<List<Test>> inconclusiveTests;

    public TestViewModel(@NonNull Application application) {
        super(application);
        TestDatabase database = TestDatabase.getInstance(this.getApplication());
        this.tests = database.TestDao().loadAllTests();
        this.positiveTests = database.TestDao().loadTestGroup("Positive");
        this.negativeTests = database.TestDao().loadTestGroup("Negative");
        this.inconclusiveTests = database.TestDao().loadTestGroup("Inconclusive");
    }

    public LiveData<List<Test>> getTests() {
        return tests;
    }

    public LiveData<List<Test>> getPositiveTestGroup() {
        return positiveTests;
    }

    public LiveData<List<Test>> getNegativeTestGroup() {
        return negativeTests;
    }

    public LiveData<List<Test>> getInconclusiveTestGroup() {
        return inconclusiveTests;
    }
}

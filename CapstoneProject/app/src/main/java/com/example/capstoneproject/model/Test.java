package com.example.capstoneproject.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@Entity (tableName = "testData")
public class Test {

    @PrimaryKey (autoGenerate = true)
    private int mTestID;
    private String mPatientID;
    private String mTestResult;
    private String mSex;
    private String mAgeGroup;
    private String mEthnicity;
    private ArrayList<String> mComorbidities;
    private String mTestNotes;
    private double mTestLatitude;
    private double mTestLongitude;
    private Date mTestDate;

    @Ignore
    public Test(int testId, String patientID, String testResult, String sex,
                String ageGroup, String ethnicity, ArrayList<String> comorbidities,
                String testNotes, double testLatitude, double testLongitude, Date testDate) {
        mTestID = testId;
        mPatientID = patientID;
        mTestResult = testResult;
        mSex = sex;
        mAgeGroup = ageGroup;
        mEthnicity = ethnicity;
        mComorbidities = comorbidities;
        mTestNotes = testNotes;
        mTestLatitude = testLatitude;
        mTestLongitude = testLongitude;
        mTestDate = testDate;
    }

    public Test(String patientID, String testResult, String sex,
                String ageGroup, String ethnicity, ArrayList<String> comorbidities,
                String testNotes, double testLatitude, double testLongitude, Date testDate) {
        mPatientID = patientID;
        mTestResult = testResult;
        mSex = sex;
        mAgeGroup = ageGroup;
        mEthnicity = ethnicity;
        mComorbidities = comorbidities;
        mTestNotes = testNotes;
        mTestLatitude = testLatitude;
        mTestLongitude = testLongitude;
        mTestDate = testDate;
    }

    public int getTestID() {
        return mTestID;
    }

    public void setTestID(int testID) {
        this.mTestID = testID;
    }

    public String getPatientID() {
        return mPatientID;
    }

    public void setPatientID(String patientID) {
        this.mPatientID = patientID;
    }

    public String getTestResult() {
        return mTestResult;
    }

    public void setTestResult(String testResult) {
        this.mTestResult = testResult;
    }

    public String getSex() {
        return mSex;
    }

    public void setSex(String sex) {
        this.mSex = sex;
    }

    public String getAgeGroup() {
        return mAgeGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.mAgeGroup = ageGroup;
    }

    public String getEthnicity() {
        return mEthnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.mEthnicity = ethnicity;
    }

    public ArrayList<String> getComorbidities() {
        return mComorbidities;
    }

    public void setComorbidities(ArrayList<String> comorbidities) {
        this.mComorbidities = comorbidities;
    }

    public String getTestNotes() {
        return mTestNotes;
    }

    public void setTestNotes(String testNotes) {
        this.mTestNotes = testNotes;
    }

    public double getTestLatitude() {
        return mTestLatitude;
    }

    public void setTestLatitude(double testLatitude) {
        this.mTestLatitude = testLatitude;
    }

    public double getTestLongitude() {
        return mTestLongitude;
    }

    public void setTestLongitude(double testLongitude) {
        this.mTestLongitude = testLongitude;
    }

    public Date getTestDate() {
        return mTestDate;
    }

    public void setTestDate(Date testDate) {
        this.mTestDate = testDate;
    }

    public String getTestDateFormatted() {
        DateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return dateFormat.format(mTestDate);
    }
}

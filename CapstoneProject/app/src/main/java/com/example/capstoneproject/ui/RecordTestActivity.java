package com.example.capstoneproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestDao;
import com.example.capstoneproject.data.TestDatabase;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.utils.SaveTestExecutor;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RecordTestActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {
    public static final String TEST_SAVED_BUNDLE_KEY = "test_saved";
    public static final String TEST_SAVED_STRING_KEY = "test_saved";
    private static final String LOG_TAG = RecordTestActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    private TestDatabase mDb;

    private EditText mPatientIDEditText;
    private Spinner mTestResultSpinner;
    private Spinner mSexSpinner;
    private Spinner mAgeGroupSpinner;
    private Spinner mEthnicitySpinner;
    private CheckBox mSymptom1CheckBox;
    private CheckBox mSymptom2CheckBox;
    private CheckBox mSymptom3CheckBox;
    private CheckBox mSymptom4CheckBox;
    private CheckBox mSymptom5CheckBox;
    private EditText mTestNotesEditText;
    private double mTestLatitude;
    private double mTestLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_test);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.record_new_test_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDb = TestDatabase.getInstance(getApplicationContext());
        TestViewModel viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        mPatientIDEditText = findViewById(R.id.patient_id_edittext_input);
        mTestResultSpinner = findViewById(R.id.test_result_spinner);
        mSexSpinner = findViewById(R.id.sex_spinner);
        mAgeGroupSpinner = findViewById(R.id.age_group_spinner);
        mEthnicitySpinner = findViewById(R.id.ethnicity_spinner);
        setUpSpinners();
        mSymptom1CheckBox = findViewById(R.id.comorbidities_checkbox_symptom1);
        mSymptom2CheckBox = findViewById(R.id.comorbidities_checkbox_symptom2);
        mSymptom3CheckBox = findViewById(R.id.comorbidities_checkbox_symptom3);
        mSymptom4CheckBox = findViewById(R.id.comorbidities_checkbox_symptom4);
        mSymptom5CheckBox = findViewById(R.id.comorbidities_checkbox_symptom5);
        mTestNotesEditText = findViewById(R.id.test_notes_edittext_input);

        Button saveButton = findViewById(R.id.save_test_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTestResult();
            };
        });
    }

    private void setUpSpinners() {
        mTestResultSpinner.setOnItemSelectedListener(this);
        mSexSpinner.setOnItemSelectedListener(this);
        mAgeGroupSpinner.setOnItemSelectedListener(this);
        mEthnicitySpinner.setOnItemSelectedListener(this);

        String[] testResultsArray = getResources().getStringArray(R.array.result_options);
        List<String> testResultsArrayList = Arrays.asList(testResultsArray);
        String[] sexOptionsArray = getResources().getStringArray(R.array.sex_options);
        List<String> sexOptionsArrayList = Arrays.asList(sexOptionsArray);
        String[] ageGroupArray = getResources().getStringArray(R.array.age_groups);
        List<String> ageGroupArrayList = Arrays.asList(ageGroupArray);
        String[] ethnicityArray = getResources().getStringArray(R.array.ethnicity_options);
        List<String> ethnicityArrayList = Arrays.asList(ethnicityArray);

        ArrayAdapter<String> testResultsAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_textview, testResultsArrayList);
        testResultsAdapter.setDropDownViewResource(R.layout.spinner_custom_dialog_textview);
        ArrayAdapter<String> sexOptionsAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_textview, sexOptionsArrayList);
        sexOptionsAdapter.setDropDownViewResource(R.layout.spinner_custom_dialog_textview);
        ArrayAdapter<String> ageGroupAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_textview, ageGroupArrayList);
        ageGroupAdapter.setDropDownViewResource(R.layout.spinner_custom_dialog_textview);
        ArrayAdapter<String> ethnicityAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_textview, ethnicityArrayList);
        ethnicityAdapter.setDropDownViewResource(R.layout.spinner_custom_dialog_textview);

        mTestResultSpinner.setAdapter(testResultsAdapter);
        mSexSpinner.setAdapter(sexOptionsAdapter);
        mAgeGroupSpinner.setAdapter(ageGroupAdapter);
        mEthnicitySpinner.setAdapter(ethnicityAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mTestLatitude = location.getLatitude();
        mTestLongitude = location.getLongitude();

    }

    private void saveTestResult() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String patientId = mPatientIDEditText.getText().toString();
        String testResult = mTestResultSpinner.getSelectedItem().toString();
        String sex = mSexSpinner.getSelectedItem().toString();
        String ageGroup = mAgeGroupSpinner.getSelectedItem().toString();
        String ethnicity = mEthnicitySpinner.getSelectedItem().toString();
        ArrayList<String> comorbidities = new ArrayList<>();
        int countComorbidities = 0;

        if (mSymptom1CheckBox.isChecked()) {
            comorbidities.add(mSymptom1CheckBox.getText().toString());
            countComorbidities++;
        }
        if (mSymptom2CheckBox.isChecked()) {
            comorbidities.add(mSymptom2CheckBox.getText().toString());
            countComorbidities++;
        }
        if (mSymptom3CheckBox.isChecked()) {
            comorbidities.add(mSymptom3CheckBox.getText().toString());
            countComorbidities++;
        }
        if (mSymptom4CheckBox.isChecked()) {
            comorbidities.add(mSymptom4CheckBox.getText().toString());
            countComorbidities++;
        }
        if (mSymptom5CheckBox.isChecked()) {
            comorbidities.add(mSymptom5CheckBox.getText().toString());
            countComorbidities++;
        }
        if (countComorbidities == 0) {
            comorbidities.add(getString(R.string.na));
        }
        String testNotes = mTestNotesEditText.getText().toString();
        Date testDate = new Date(System.currentTimeMillis());

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch(SecurityException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
        double testLatitude = mTestLatitude;
        double testLongitude = mTestLongitude;

        final Test test = new Test(patientId, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, testLatitude, testLongitude, testDate);
        SaveTestExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.testDao().insertTest(test);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(TEST_SAVED_STRING_KEY, "Test Saved");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(TEST_SAVED_BUNDLE_KEY, bundle);
        startActivity(intent);
        finish();
    }
}
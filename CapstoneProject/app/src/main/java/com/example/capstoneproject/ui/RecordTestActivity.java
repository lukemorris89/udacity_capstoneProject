package com.example.capstoneproject.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.utils.LocationHelper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RecordTestActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private final int mPatientIdMinLength = 5;

    private double mTestLatitude;
    private double mTestLongitude;

    private int mTestID;

    private boolean isViewEditTest = false;

    private List<TextInputLayout> mEditTextLayouts;
    private List<TextInputLayout> mDropdownLayouts;
    private List<EditText> mEditTextInputs;
    private List<AutoCompleteTextView> mDropdownInputs;

    private List<String> mDropdownErrorTextList;
    private List<View> mInvalidViews;
    private List<CheckBox> mComorbiditiesCheckboxes;

    private Button mEditTestButton;
    private ScrollView mRecordEditTestScrollView;
    private ScrollView mViewTestScrollView;

    private Test mCurrentTest;

    private TestViewModel mTestViewModel;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTestViewModel = new ViewModelProvider(this).get(TestViewModel.class);
        Intent intent = getIntent();
        if (intent.hasExtra("test_id")) {
            mTestID = intent.getIntExtra("test_id", 0);
            isViewEditTest = true;
        }
        setContentView(R.layout.activity_record_test);
        mEditTestButton = findViewById(R.id.edit_test_button);
        mRecordEditTestScrollView = findViewById(R.id.test_results_scrollview);
        mViewTestScrollView = findViewById(R.id.view_test_scrollview);
        TextView testIDTitleTextView = findViewById(R.id.test_id_title_edittest_textview);
        TextView testIDTextView = findViewById(R.id.test_id_edittest_textview);

        final boolean[] isEditable = {false};
        mEditTestButton.setOnClickListener(view -> {
            if (isEditable[0]) {
                mRecordEditTestScrollView.setVisibility(View.GONE);
                mViewTestScrollView.setVisibility(View.VISIBLE);
                mEditTestButton.setText(R.string.edit_test);
                isEditable[0] = false;
            } else {
                mRecordEditTestScrollView.setVisibility(View.VISIBLE);
                mViewTestScrollView.setVisibility(View.GONE);
                mEditTestButton.setText(R.string.cancel);
                isEditable[0] = true;
            }
        });

        Toolbar toolbar = findViewById(R.id.record_test_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (isViewEditTest) {
                TextView title = findViewById(R.id.title);
                title.setText(R.string.view_test_results);
            }
        }

        setupUI(findViewById(R.id.record_test_rootlayout));

        if (isViewEditTest) {
            mEditTestButton.setVisibility(View.VISIBLE);
            mRecordEditTestScrollView.setVisibility(View.GONE);
            mViewTestScrollView.setVisibility(View.VISIBLE);
            testIDTextView.setVisibility(View.VISIBLE);
            testIDTitleTextView.setVisibility(View.VISIBLE);
            new GetTestAsyncTask().execute(mTestID);
        } else  {
            startLocationUpdates();
        }

        Button saveButton = findViewById(R.id.save_test_button);
        saveButton.setOnClickListener(view -> saveTestResult());
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        if (!(view instanceof EditText) || view instanceof AutoCompleteTextView) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard();
                return false;
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
        mInvalidViews = new ArrayList<>();
        setUpDropdowns();
        setUpEditTextValidation();

        mComorbiditiesCheckboxes = new ArrayList<>();
        mComorbiditiesCheckboxes.add(findViewById(R.id.comorbidities_checkbox_symptom1));
        mComorbiditiesCheckboxes.add(findViewById(R.id.comorbidities_checkbox_symptom2));
        mComorbiditiesCheckboxes.add(findViewById(R.id.comorbidities_checkbox_symptom3));
        mComorbiditiesCheckboxes.add(findViewById(R.id.comorbidities_checkbox_symptom4));
        mComorbiditiesCheckboxes.add(findViewById(R.id.comorbidities_checkbox_symptom5));
    }

    private void setUpDropdowns() {
        mDropdownLayouts = new ArrayList<>();
        mDropdownLayouts.add(findViewById(R.id.test_result_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.sex_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.age_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.ethnicity_dropdown_layout));

        mDropdownInputs = new ArrayList<>();
        mDropdownInputs.add(findViewById(R.id.record_test_test_result_dropdown));
        mDropdownInputs.add(findViewById(R.id.sex_test_result_dropdown));
        mDropdownInputs.add(findViewById(R.id.age_test_result_dropdown));
        mDropdownInputs.add(findViewById(R.id.ethnicity_test_result_dropdown));

        List<List<String>> dropdownInputsLists = new ArrayList<>();
        dropdownInputsLists.add(Arrays.asList(getResources().getStringArray(R.array.result_options)));
        dropdownInputsLists.add(Arrays.asList(getResources().getStringArray(R.array.sex_options)));
        dropdownInputsLists.add(Arrays.asList(getResources().getStringArray(R.array.age_groups)));
        dropdownInputsLists.add(Arrays.asList(getResources().getStringArray(R.array.ethnicity_options)));

        mDropdownLayouts = new ArrayList<>();
        mDropdownLayouts.add(findViewById(R.id.test_result_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.sex_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.age_dropdown_layout));
        mDropdownLayouts.add(findViewById(R.id.ethnicity_dropdown_layout));

        for (int i = 0; i < mDropdownInputs.size(); i++) {
            mDropdownInputs.get(i).setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_custom_textview, dropdownInputsLists.get(i)));
            mDropdownInputs.get(i).setThreshold(200);
        }
        setUpDropdownValidation();
    }

    private void setUpEditTextValidation() {
        mEditTextLayouts = new ArrayList<>();
        mEditTextLayouts.add(findViewById(R.id.patient_id_edittext_layout));
        mEditTextInputs = new ArrayList<>();
        mEditTextInputs.add(findViewById(R.id.patient_id_edittext_input));
        mEditTextInputs.add(findViewById(R.id.test_notes_edittext_input));

        for (int i = 0; i < mEditTextLayouts.size(); i++) {
            TextInputLayout inputLayout = mEditTextLayouts.get(i);
            EditText inputField = mEditTextInputs.get(i);
            TextWatcher editTextTextWatcher = null;
            if (i == 0) {
                editTextTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        String textEntry = charSequence.toString();
                        if (textEntry.length() < mPatientIdMinLength) {
                            inputLayout.setError("Please enter min 5 digits");
                        } else {
                            inputLayout.setError(null);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                };
            }
            inputField.addTextChangedListener(editTextTextWatcher);
        }
    }


    private void setUpDropdownValidation() {
        mDropdownErrorTextList = new ArrayList<>();
        mDropdownErrorTextList.add("Please choose test result");
        mDropdownErrorTextList.add("Please choose patient sex");
        mDropdownErrorTextList.add("Please choose patient age group");
        mDropdownErrorTextList.add("Please choose patient ethnicity");

        for (int i = 0; i < mDropdownInputs.size(); i++) {
            TextInputLayout dropdownLayout = mDropdownLayouts.get(i);
            AutoCompleteTextView dropdownInput = mDropdownInputs.get(i);
            int finalI = i;
            TextWatcher dropdownTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    dropdownLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            };
            dropdownInput.addTextChangedListener(dropdownTextWatcher);
            dropdownInput.setOnDismissListener(() -> {
                if (dropdownInput.getText().toString().isEmpty()) {
                    dropdownLayout.setError(mDropdownErrorTextList.get(finalI));
                    mInvalidViews.add(mDropdownInputs.get(finalI));
                } else {
                    dropdownLayout.setError(null);
                }
            });
        }
    }

    private void saveTestResult() {
        TextInputLayout patientIdEditTextLayout = mEditTextLayouts.get(0);
        String patientID = mEditTextInputs.get(0).getText().toString();
        if (patientID.length() < mPatientIdMinLength) {
            patientIdEditTextLayout.setError("Please enter min 5 digits");
            mInvalidViews.add(patientIdEditTextLayout);
        } else {
            patientIdEditTextLayout.setError(null);
        }

        for (int i = 0; i < mDropdownInputs.size(); i++) {
            TextInputLayout dropdownLayout = mDropdownLayouts.get(i);
            AutoCompleteTextView dropdownInput = mDropdownInputs.get(i);
            if (dropdownInput.getText().toString().isEmpty()) {
                dropdownLayout.setError(mDropdownErrorTextList.get(i));
                mInvalidViews.add(dropdownInput);
            } else {
                dropdownLayout.setError(null);
            }
        }

        if (mInvalidViews.size() > 0) {
            mRecordEditTestScrollView.post(() -> {
                mInvalidViews.get(0).requestFocus();
                mRecordEditTestScrollView.scrollTo(0, mInvalidViews.get(0).getBottom());
                mInvalidViews.clear();
            });
            return;
        }

        String testResult = mDropdownInputs.get(0).getText().toString();
        String sex = mDropdownInputs.get(1).getText().toString();
        String ageGroup = mDropdownInputs.get(2).getText().toString();
        String ethnicity = mDropdownInputs.get(3).getText().toString();

        ArrayList<String> comorbidities = new ArrayList<>();
        int countComorbidities = 0;
        for (int i = 0; i < mComorbiditiesCheckboxes.size(); i++) {
            if (mComorbiditiesCheckboxes.get(i).isChecked()) {
                comorbidities.add(mComorbiditiesCheckboxes.get(i).getText().toString());
                countComorbidities++;
            }
        }
        if (countComorbidities == 0) {
            comorbidities.add(getString(R.string.na));
        }

        Date testDate = new Date(System.currentTimeMillis());

        EditText testNotesEditText = findViewById(R.id.test_notes_edittext_input);
        String testNotes = testNotesEditText.getText().toString();

        if (isViewEditTest) {
            Test test = new Test(mTestID, patientID, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, mCurrentTest.getTestLatitude(),mCurrentTest.getTestLongitude(), mCurrentTest.getTestLocation(), mCurrentTest.getTestDate());
            mTestViewModel.updateTest(test);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            LocationHelper locationHelper = new LocationHelper(this);
            String location = locationHelper.getAddress(mTestLatitude, mTestLongitude);
//
            Test test = new Test(patientID, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, mTestLatitude, mTestLongitude, location, testDate);
            mTestViewModel.insertTest(test);
//
            Intent intent = new Intent(RecordTestActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setUpViewTestSummary(Test test) {
        TextView viewTestID = findViewById(R.id.test_id_textview);
        TextView editTestID = findViewById(R.id.test_id_edittest_textview);
        viewTestID.setText(String.valueOf(test.getTestID()));
        editTestID.setText(String.valueOf(test.getTestID()));

        TextView viewTestPatientID = findViewById(R.id.patient_id_textview);
        viewTestPatientID.setText(test.getPatientID());
        mEditTextInputs.get(0).setText(test.getPatientID());

        TextView viewTestResult = findViewById(R.id.test_result_textview);
        viewTestResult.setText(test.getTestResult());
        mDropdownInputs.get(0).setText(test.getTestResult());

        TextView viewTestSex = findViewById(R.id.sex_textview);
        viewTestSex.setText(test.getSex());
        mDropdownInputs.get(1).setText(test.getSex());

        TextView viewTestAge = findViewById(R.id.agegroup_textview);
        viewTestAge.setText(test.getAgeGroup());
        mDropdownInputs.get(2).setText(test.getAgeGroup());

        TextView viewTestEthnicity = findViewById(R.id.ethnicity_textview);
        viewTestEthnicity.setText(test.getEthnicity());
        mDropdownInputs.get(3).setText(test.getEthnicity());

        TextView viewTestComorbidities = findViewById(R.id.comorbidities_textview);
        List<String> currentTestComorbidities = test.getComorbidities();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < currentTestComorbidities.size() - 1; i++) {
            builder.append(currentTestComorbidities.get(i));
            builder.append(", ");
        }
        builder.append(currentTestComorbidities.get(currentTestComorbidities.size()-1));
        String comorbiditiesString = builder.toString();
        viewTestComorbidities.setText(comorbiditiesString);
        for (int i = 0; i < currentTestComorbidities.size() ; i++) {
            for (int j = 0; j < mComorbiditiesCheckboxes.size(); j++) {
                if (mComorbiditiesCheckboxes.get(j).getText().equals(currentTestComorbidities.get(i))) {
                    mComorbiditiesCheckboxes.get(j).setChecked(true);
                }
            }
        }

        TextView viewTestNotes = findViewById(R.id.test_notes_textview);
        viewTestNotes.setText(test.getTestNotes());
        mEditTextInputs.get(1).setText(test.getTestNotes());

        String currentTestDateFormatted = test.getTestDateFormatted();
        TextView viewTestDate = findViewById(R.id.test_date_textview);
        viewTestDate.setText(currentTestDateFormatted);

        String currentTestLocation = test.getTestLocation();

        TextView locationTextView = findViewById(R.id.test_location_textview);
        locationTextView.setText(currentTestLocation);
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //Adapterview methods
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    //Async Task to get single test by ID
    private class GetTestAsyncTask extends AsyncTask<Integer, Void, Test> {

        @Override
        protected Test doInBackground(Integer... integers) {
            mCurrentTest = mTestViewModel.getSingleTestById(integers[0]);
            return mCurrentTest;
        }

        @Override
        protected void onPostExecute(Test test) {
            setUpViewTestSummary(test);
        }
    }

    //Location Methods
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    public void onLocationChanged(Location location) {
        mTestLatitude = location.getLatitude();
        mTestLongitude = location.getLongitude();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        }
        else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.record_test_rootlayout),
                    R.string.permission_denied_test,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.return_snackbar, view -> {
                snackbar.dismiss();
                Intent intent = new Intent(RecordTestActivity.this, MainActivity.class);
                startActivity(intent);
            });
            snackbar.show();
        }
    }
}

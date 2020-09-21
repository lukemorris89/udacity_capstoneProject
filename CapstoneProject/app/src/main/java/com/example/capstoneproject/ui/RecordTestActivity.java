package com.example.capstoneproject.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.utils.Utils;
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

    private final int mPatientIdMinLength = 5;
    private final int mTestNotesMaxLength = 500;

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
    private TextView mTestIDTextView;

    private Test mCurrentTest;
    private String mCurrentTestPatientID;
    private String mCurrentTestResult;
    private String mCurrentTestSex;
    private String mCurrentTestAgeGroup;
    private String mCurrentTestEthnicity;
    private List<String> mCurrentTestComorbidities;
    private String mCurrentTestNotes;
    private Date mCurrentTestDate;
    private String mCurrentTestDateFormatted;
    private double mCurrentTestLatitude;
    private double mCurrentTestLongitude;

    private TestViewModel mTestViewModel;

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
        mTestIDTextView = findViewById(R.id.test_id_edittest_textview);

        final boolean[] isEditable = {false};
        mEditTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            mTestIDTextView.setVisibility(View.VISIBLE);
            testIDTitleTextView.setVisibility(View.VISIBLE);
            new GetTestAsyncTask().execute(mTestID);
        }

        Button saveButton = findViewById(R.id.save_test_button);
        saveButton.setOnClickListener(view -> saveTestResult());
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText) || view instanceof AutoCompleteTextView) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
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
            dropdownInput.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (dropdownInput.getText().toString().isEmpty()) {
                        dropdownLayout.setError(mDropdownErrorTextList.get(finalI));
                        mInvalidViews.add(mDropdownInputs.get(finalI));
                    } else {
                        dropdownLayout.setError(null);
                    }
                }
            });
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
        TextInputLayout patientIdEditTextLayout = mEditTextLayouts.get(0);
        String patientId = mEditTextInputs.get(0).getText().toString();
        if (patientId.length() < mPatientIdMinLength) {
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
            mRecordEditTestScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mInvalidViews.get(0).requestFocus();
                    mRecordEditTestScrollView.scrollTo(0, mInvalidViews.get(0).getBottom());
                    mInvalidViews.clear();
                }
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

        EditText testNotesEditText = findViewById(R.id.test_notes_edittext_input);
        String testNotes = testNotesEditText.getText().toString();

        Date testDate = new Date(System.currentTimeMillis());

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch(SecurityException e) {
            if (e.getMessage() != null) {
                Log.v(LOG_TAG, e.getMessage());
            }
        }

        double testLatitude = mTestLatitude;
        double testLongitude = mTestLongitude;

        if (isViewEditTest) {
            Test test = new Test(mTestID, patientId, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, testLatitude, testLongitude, testDate);
            mTestViewModel.updateTest(test);
        } else {
            Test test = new Test(patientId, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, testLatitude, testLongitude, testDate);
            mTestViewModel.insertTest(test);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
        mCurrentTestComorbidities = test.getComorbidities();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mCurrentTestComorbidities.size() - 1; i++) {
            builder.append(mCurrentTestComorbidities.get(i));
            builder.append(", ");
        }
        builder.append(mCurrentTestComorbidities.get(mCurrentTestComorbidities.size()-1));
        String comorbiditiesString = builder.toString();
        viewTestComorbidities.setText(comorbiditiesString);
        for (int i = 0; i < mCurrentTestComorbidities.size() ; i++) {
            for (int j = 0; j < mComorbiditiesCheckboxes.size(); j++) {
                if (mComorbiditiesCheckboxes.get(j).getText().equals(mCurrentTestComorbidities.get(i))) {
                    mComorbiditiesCheckboxes.get(j).setChecked(true);
                }
            }
        }

        TextView viewTestNotes = findViewById(R.id.test_notes_textview);
        viewTestNotes.setText(test.getTestNotes());
        mEditTextInputs.get(1).setText(test.getTestNotes());

        mCurrentTestDateFormatted = test.getTestDateFormatted();
        TextView viewTestDate = findViewById(R.id.test_date_textview);
        viewTestDate.setText(mCurrentTestDateFormatted);

        mCurrentTestLatitude = test.getTestLatitude();
        mCurrentTestLongitude = test.getTestLongitude();
        String testLocation = mCurrentTestLatitude + ", " + mCurrentTestLongitude;
        TextView viewTestLocation = findViewById(R.id.test_location_textview);
        viewTestLocation.setText(testLocation);
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

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
}
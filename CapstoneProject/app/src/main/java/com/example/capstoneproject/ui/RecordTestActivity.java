package com.example.capstoneproject.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
    private boolean mTestValid;

    private boolean isViewEditTest = false;

    private List<TextInputLayout> mEditTextLayouts;
    private List<TextInputLayout> mDropdownLayouts;
    private List<EditText> mEditTextInputs;
    private List<AutoCompleteTextView> mDropdownInputs;
    private List<String> mDropdownErrorTextList;
    private List<View> mInvalidViews;

    private Button mEditTestButton;
    private ScrollView mRecordEditTestScrollView;
    private ScrollView mViewTestScrollView;
    private TextView mTestIDTextView;

    private TestViewModel mTestViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra("test_id")) {
            isViewEditTest = true;
        }
        setContentView(R.layout.activity_record_test);
        mEditTestButton = findViewById(R.id.edit_test_button);
        mRecordEditTestScrollView = findViewById(R.id.test_results_scrollview);
        mViewTestScrollView = findViewById(R.id.view_test_scrollview);
        mTestIDTextView = findViewById(R.id.test_id_edittest_textview);
        if (isViewEditTest) {
            mEditTestButton.setVisibility(View.VISIBLE);
            mRecordEditTestScrollView.setVisibility(View.GONE);
            mViewTestScrollView.setVisibility(View.VISIBLE);
            mTestIDTextView.setVisibility(View.VISIBLE);
        }

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

        mTestViewModel = new ViewModelProvider(this).get(TestViewModel.class);

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
                            mTestValid = false;
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
                        mTestValid = false;
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
                mTestValid = false;
                    dropdownLayout.setError(mDropdownErrorTextList.get(i));
                    mInvalidViews.add(dropdownInput);
            } else {
                dropdownLayout.setError(null);
            }
        }

        if (!mTestValid) {
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

        ArrayList<CheckBox> comorbiditiesOptions = new ArrayList<>();
        comorbiditiesOptions.add(findViewById(R.id.comorbidities_checkbox_symptom1));
        comorbiditiesOptions.add(findViewById(R.id.comorbidities_checkbox_symptom2));
        comorbiditiesOptions.add(findViewById(R.id.comorbidities_checkbox_symptom3));
        comorbiditiesOptions.add(findViewById(R.id.comorbidities_checkbox_symptom4));
        comorbiditiesOptions.add(findViewById(R.id.comorbidities_checkbox_symptom5));
        ArrayList<String> comorbidities = new ArrayList<>();
        int countComorbidities = 0;
        for (int i = 0; i < comorbiditiesOptions.size(); i++) {
            if (comorbiditiesOptions.get(i).isChecked()) {
                comorbidities.add(comorbiditiesOptions.get(i).getText().toString());
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

        final Test test = new Test(patientId, testResult, sex, ageGroup, ethnicity, comorbidities, testNotes, testLatitude, testLongitude, testDate);
        if (isViewEditTest) {
        mTestViewModel.updateTest(test);
        } else {
            mTestViewModel.insertTest(test);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
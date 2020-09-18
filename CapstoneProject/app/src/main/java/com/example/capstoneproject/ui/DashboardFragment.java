package com.example.capstoneproject.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.progresviews.ProgressWheel;
import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestDatabase;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    public static final String POSITIVE_KEY = "Positive";
    public static final String NEGATIVE_KEY = "Negative";
    public static final String INCONCLUSIVE_KEY = "Inconclusive";

    private static final String LOG_TAG = DashboardFragment.class.getSimpleName();

    private List<ExpandableListView> mExpandableListViews;
    private List<Test> mPositiveTests = new ArrayList<>();
    private List<Test> mNegativeTests = new ArrayList<>();
    private List<Test> mInconclusiveTests = new ArrayList<>();

    private TestDatabase mDb;

    private DashboardProgressBar mDashboardProgressWheelPositive;
    private DashboardProgressBar mDashboardProgressWheelNegative;
    private DashboardProgressBar mDashboardProgressWheelInconclusive;

    private Context mContext;


    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);


        mDashboardProgressWheelPositive = view.findViewById(R.id.wheel_progress_positive);
        mDashboardProgressWheelNegative = view.findViewById(R.id.wheel_progress_negative);
        mDashboardProgressWheelInconclusive = view.findViewById(R.id.wheel_progress_inconclusive);

        mExpandableListViews = new ArrayList<>();


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RecordTestActivity.class);
                startActivity(intent);
            }
        });

        mDb = TestDatabase.getInstance(getActivity().getApplicationContext());
        TestViewModel viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        return view;
    }

    public void onViewCreated(final View view, Bundle saved) {
        super.onViewCreated(view, saved);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                for (ExpandableListView view : mExpandableListViews) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = mExpandableListViews.get(0).getWidth();
                    int widthPx = Utils.convertDpToPx(getContext(), width);
                    view.setIndicatorBoundsRelative(widthPx - 1750, 0);
                }
            }
        });
    }

    private void setUpExpandableListViews() {
        List<Test> expandableListViewDetailPositive = mPositiveTests;
        List<Test> expandableListViewDetailNegative = mNegativeTests;
        List<Test> expandableListViewDetailInconclusive = mInconclusiveTests;

        ExpandableListViewAdapter expandableListViewPositiveAdapter = new ExpandableListViewAdapter(getContext(), POSITIVE_KEY, expandableListViewDetailPositive);
        ExpandableListViewAdapter expandableListViewNegativeAdapter = new ExpandableListViewAdapter(getContext(), NEGATIVE_KEY, expandableListViewDetailNegative);
        ExpandableListViewAdapter expandableListViewInconclusiveAdapter = new ExpandableListViewAdapter(getContext(), INCONCLUSIVE_KEY, expandableListViewDetailInconclusive);

        List<ExpandableListViewAdapter> expandableListViewAdapters = new ArrayList<>();
        expandableListViewAdapters.add(expandableListViewPositiveAdapter);
        expandableListViewAdapters.add(expandableListViewNegativeAdapter);
        expandableListViewAdapters.add(expandableListViewInconclusiveAdapter);

        for (int i = 0; i < mExpandableListViews.size(); i++) {
            mExpandableListViews.get(i).setAdapter(expandableListViewAdapters.get(i));
        }

    }


    private void updateDashboardSummaryCard(String targetView) {
        int updateLength;
        if (targetView.equals("Positive")) {
            updateLength = mPositiveTests.size();
//            mDashboardProgressWheelPositive.setStepCountText(String.valueOf(updateLength));
        } else if (targetView.equals("Negative")) {
            updateLength = mNegativeTests.size();
//            mDashboardProgressWheelNegative.setStepCountText(String.valueOf(updateLength));
        } else if (targetView.equals("Inconclusive")) {
            updateLength = mInconclusiveTests.size();
//            mDashboardProgressWheelInconclusive.setStepCountText(String.valueOf(updateLength));
        }
    }

    private void setRecyclerViewHeight(ExpandableListView listView, int group) {
//        int
//        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
//        int totalHeight = 0;
//        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY);
//        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
//            View groupItem = listAdapter.getGroupView(i, false, null, listView);
//            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
//            totalHeight += groupItem.getMeasuredHeight();
//
//        }
//
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        int height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
//        if (height < 10) {
//            height = 200;
//        }
//        params.height = height;
//        listView.setLayoutParams(params);
//        listView.requestLayout();
//    }
    }
}
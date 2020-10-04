package com.example.capstoneproject.ui;

import android.animation.ValueAnimator;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.utils.Utils;
import com.example.capstoneproject.widget.TestSummaryWidget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private List<TextView> mDashboardProgressBarTextViewsList;
    private List<RecyclerView> mRecyclerViewsList;
    private List<View> mExpandButtonViewsList;
    private List<ImageButton> mExpandButtonsList;
    private List<TextView> mNoResultsTextViewsList;
    private List<LinearLayout> mRecyclerViewContainersList;
    private List<View> mRecyclerViewTitleList;

    private TestViewModel mTestViewModel;

    private boolean[] isSelected = { false, false, false};
    private String[] mTestResultOptions = { "Positive", "Negative", "Inconclusive" };

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mTestViewModel = new ViewModelProvider(this.getActivity()).get(TestViewModel.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext();
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        List<DashboardProgressBar> dashboardProgressBarsList = new ArrayList<>();
        dashboardProgressBarsList.add(view.findViewById(R.id.wheel_progress_positive));
        dashboardProgressBarsList.add(view.findViewById(R.id.wheel_progress_negative));
        dashboardProgressBarsList.add(view.findViewById(R.id.wheel_progress_inconclusive));

        mDashboardProgressBarTextViewsList = new ArrayList<>();
        mDashboardProgressBarTextViewsList.add(view.findViewById(R.id.wheel_progress_positive_text));
        mDashboardProgressBarTextViewsList.add(view.findViewById(R.id.wheel_progress_negative_text));
        mDashboardProgressBarTextViewsList.add(view.findViewById(R.id.wheel_progress_inconclusive_text));

        mRecyclerViewsList = new ArrayList<>();
        mRecyclerViewsList.add(view.findViewById(R.id.recyclerview_positive));
        mRecyclerViewsList.add(view.findViewById(R.id.recyclerview_negative));
        mRecyclerViewsList.add(view.findViewById(R.id.recyclerview_inconclusive));

        mExpandButtonViewsList = new ArrayList<>();
        mExpandButtonViewsList.add(view.findViewById(R.id.expand_view_button_positive));
        mExpandButtonViewsList.add(view.findViewById(R.id.expand_view_button_negative));
        mExpandButtonViewsList.add(view.findViewById(R.id.expand_view_button_inconclusive));

        mExpandButtonsList = new ArrayList<>();
        mExpandButtonsList.add(view.findViewById(R.id.expand_view_button_image_positive));
        mExpandButtonsList.add(view.findViewById(R.id.expand_view_button_image_negative));
        mExpandButtonsList.add(view.findViewById(R.id.expand_view_button_image_inconclusive));

        mNoResultsTextViewsList = new ArrayList<>();
        mNoResultsTextViewsList.add(view.findViewById(R.id.no_test_results_positive));
        mNoResultsTextViewsList.add(view.findViewById(R.id.no_test_results_negative));
        mNoResultsTextViewsList.add(view.findViewById(R.id.no_test_results_inconclusive));

        mRecyclerViewContainersList = new ArrayList<>();
        mRecyclerViewContainersList.add(view.findViewById(R.id.recyclerview_positive_container));
        mRecyclerViewContainersList.add(view.findViewById(R.id.recyclerview_negative_container));
        mRecyclerViewContainersList.add(view.findViewById(R.id.recyclerview_inconclusive_container));

        mRecyclerViewTitleList = new ArrayList<>();
        mRecyclerViewTitleList.add(view.findViewById(R.id.recyclerview_positive_title));
        mRecyclerViewTitleList.add(view.findViewById(R.id.recyclerview_negative_title));
        mRecyclerViewTitleList.add(view.findViewById(R.id.recyclerview_inconclusive_title));

        for (int i = 0; i < mTestResultOptions.length; i++) {
            RecyclerView currentRecyclerView = mRecyclerViewsList.get(i);
            setUpExpandFunctionality(i);
            currentRecyclerView.setAdapter(new TestAdapter(getActivity()));
            currentRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            int finalI = i;
            mTestViewModel.getTestGroup(mTestResultOptions[i]).observe(getViewLifecycleOwner(), tests -> {
                TestAdapter adapter =(TestAdapter)currentRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setTests(tests);
                    int adapterNewLength = adapter.getItemCount();
                    if (adapterNewLength > Utils.adapterLengths[finalI]) {
                        adapter.notifyItemInserted(adapterNewLength - 1);
                    } else {
                        RecyclerView.ItemAnimator animator = currentRecyclerView.getItemAnimator();
                        if (animator instanceof SimpleItemAnimator) {
                            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                        }
                    }
                    Utils.setAdapterLengths(finalI, adapterNewLength);
                }
                updateDashboardSummaryCard(finalI, tests.size());
                setRecyclerViewHeight(finalI);

                Intent intent = new Intent(context, TestSummaryWidget.class);
                intent.setAction("com.example.capstoneproject.widget.action.REFRESH");
                if (getActivity() != null) {
                    int[] ids = AppWidgetManager.getInstance(getActivity().getApplication()).getAppWidgetIds(new ComponentName(getActivity().getApplication(), TestSummaryWidget.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    getActivity().sendBroadcast(intent);
                }
            });
            setUpSummaryBarObserver(dashboardProgressBarsList.get(i));
        }
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), RecordTestActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setUpExpandFunctionality(int index) {
        mExpandButtonViewsList.get(index).setOnClickListener(view -> {
            ImageButton expandButton = mExpandButtonsList.get(index);
            if (!isSelected[index]) {
                expandButton.setImageResource(R.drawable.ic_baseline_expand_less_24);
                isSelected[index] = true;
            }
            else {
                expandButton.setImageResource(R.drawable.ic_baseline_expand_more_24);
                isSelected[index] = false;
            }
            setRecyclerViewHeight(index);
        });
    }

    private void updateDashboardSummaryCard(int index, int size) {
        mDashboardProgressBarTextViewsList.get(index).setText(String.valueOf(size));
    }

    private void setRecyclerViewHeight(int index) {
        if (mRecyclerViewsList.get(index).getAdapter() != null) {
            int listLength = mRecyclerViewsList.get(index).getAdapter().getItemCount();
            int itemHeight = 180;
            int maxHeight = 400;
            int containerHeight = Math.min(listLength * itemHeight, maxHeight);
            ViewGroup.LayoutParams recyclerViewParams = mRecyclerViewContainersList.get(index).getLayoutParams();
            ViewGroup.LayoutParams noResultsViewParams = mNoResultsTextViewsList.get(index).getLayoutParams();
            if (listLength > 0) {
                noResultsViewParams.height = 0;
                if (isSelected[index]) {
                    mRecyclerViewTitleList.get(index).setVisibility(View.VISIBLE);
                    recyclerViewParams.height = containerHeight;
                } else {
                    mRecyclerViewTitleList.get(index).setVisibility(View.GONE);
                    recyclerViewParams.height = 0;
                }
                mRecyclerViewContainersList.get(index).setLayoutParams(recyclerViewParams);
            } else {
                mRecyclerViewTitleList.get(index).setVisibility(View.GONE);
                if (isSelected[index]) {
                    noResultsViewParams.height = 180;
                } else {
                    noResultsViewParams.height = 0;
                }
            }
            mNoResultsTextViewsList.get(index).setLayoutParams(noResultsViewParams);
        }
    }

    private void setUpSummaryBarObserver(DashboardProgressBar progressBar) {
        progressBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startAnimation(progressBar);
                progressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void startAnimation(DashboardProgressBar progressBar) {
        int max = 100;

        ValueAnimator animator = ValueAnimator.ofInt(0, max);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(0);
        animator.setDuration(300);
        animator.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            progressBar.setProgress(value);
        });
        animator.start();
    }
}
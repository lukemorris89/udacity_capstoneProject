package com.example.capstoneproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestViewModel;
import com.example.capstoneproject.model.Test;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GraphFragment extends Fragment {

    private LineChart mLineChart;
    private TestViewModel mTestViewModel;
    private List<Integer> mLineColors;


    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mTestViewModel = new ViewModelProvider(this.getActivity()).get(TestViewModel.class);
        }

        mLineColors = new ArrayList<>();
        mLineColors.add(getActivity().getResources().getColor(R.color.colorPositive));
        mLineColors.add(getActivity().getResources().getColor(R.color.colorNegative));
        mLineColors.add(getActivity().getResources().getColor(R.color.colorInconclusive));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        mLineChart = view.findViewById(R.id.line_chart);
        mLineChart.setAutoScaleMinMaxEnabled(true);
        mLineChart.setDescription(null);
        mLineChart.setNoDataText("No data added yet.");
        mLineChart.setExtraOffsets(0f,5f,20f,15f);

        XAxis xAxis = mLineChart.getXAxis();
        Legend xAxisLegend = mLineChart.getLegend();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(16f);
        xAxis.setValueFormatter(new XAxisValueFormatter());
        xAxisLegend.setWordWrapEnabled(true);
        xAxisLegend.setForm(Legend.LegendForm.CIRCLE);
        xAxisLegend.setXEntrySpace(20);
        xAxisLegend.setTextSize(14f);

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setStartAtZero(true);
        yAxis.setGranularity(1f);
        yAxis.setGranularityEnabled(true);
        yAxis.setTextSize(16f);
        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        mTestViewModel.getAllTests().observe(getViewLifecycleOwner(), tests -> {
            List<Entry> positivePlotPoints = new ArrayList<>();
            List<Entry> negativePlotPoints = new ArrayList<>();
            List<Entry> inconclusivePlotPoints = new ArrayList<>();

            List<Date> testDates = new ArrayList<>();
            for (Test test : tests) {
                testDates.add(test.getTestDate());
            }
            Set<Long> testDatesSet = new HashSet<>();
            for (Date date : testDates) {
                try {
                    DateFormat format = new SimpleDateFormat("d/MM/yyyy", Locale.getDefault());
                    Date floorDate = format.parse(format.format(date));
                    if (floorDate != null) {
                        long timeInDays = TimeUnit.MILLISECONDS.toDays(floorDate.getTime());
                        testDatesSet.add(timeInDays);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            List<Long> testDatesList = new ArrayList<>(testDatesSet);
            Collections.sort(testDatesList);

            for (Long date : testDatesList) {
                try {
                    int positiveTestsOnDate = 0;
                    int negativeTestsOnDate = 0;
                    int inconclusiveTestsOnDate = 0;

                    for (Test test : tests) {
                        DateFormat format = new SimpleDateFormat("d/MM/yyyy", Locale.getDefault());
                        Date floorDate = format.parse(format.format(test.getTestDate()));
                        if (floorDate != null) {
                            long testDate = TimeUnit.MILLISECONDS.toDays(floorDate.getTime());
                            if (testDate == date) {
                                if (test.getTestResult().equals("Positive")) {
                                    positiveTestsOnDate++;
                                } else if (test.getTestResult().equals("Negative")) {
                                    negativeTestsOnDate++;
                                } else {
                                    inconclusiveTestsOnDate++;
                                }
                            }
                        }
                    }
                    positivePlotPoints.add(new Entry(date, positiveTestsOnDate));
                    negativePlotPoints.add(new Entry(date, negativeTestsOnDate));
                    inconclusivePlotPoints.add(new Entry(date, inconclusiveTestsOnDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            List<LineDataSet> lineDataSets = new ArrayList<>();
            lineDataSets.add(new LineDataSet(positivePlotPoints, "Positive"));
            lineDataSets.add(new LineDataSet(negativePlotPoints, "Negative"));
            lineDataSets.add(new LineDataSet(inconclusivePlotPoints, "Inconclusive"));

            List<ILineDataSet> dataSets = new ArrayList<>();

            for (int i = 0; i < lineDataSets.size(); i++) {
                LineDataSet line = lineDataSets.get(i);
                line.setAxisDependency(YAxis.AxisDependency.LEFT);
                line.setColor(mLineColors.get(i));
                line.setLineWidth(3);
                line.setValueTextSize(12);
                line.setCircleColor(mLineColors.get(i));
                line.setCircleHoleColor(mLineColors.get(i));
                line.setCircleRadius(5);
                line.setValueFormatter(new LineValueFormatter());
                dataSets.add(line);
            }
            dataSets.get(0).setValueTextSize(12f);

            LineData data = new LineData(dataSets);
            mLineChart.setData(data);
            mLineChart.invalidate();
        });
        return view;
    }

    public static class XAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            DateFormat formatter = new SimpleDateFormat("d/MM", Locale.getDefault());
            Date date = new Date(TimeUnit.DAYS.toMillis((long) value));
            return formatter.format(date);
        }
    }

    public static class LineValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
//            return String.valueOf((int) entry.getY());
            return "";
        }
    }
}
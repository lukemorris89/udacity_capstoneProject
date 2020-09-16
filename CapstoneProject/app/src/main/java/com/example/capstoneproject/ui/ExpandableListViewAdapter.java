package com.example.capstoneproject.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.capstoneproject.R;
import com.example.capstoneproject.model.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    private String expandableListTitle;
    private List<Test> expandableListDetail;

    public ExpandableListViewAdapter(Context context, String expandableListTitle,
                                     List<Test> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(0);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Test test = (Test) getChild(0, expandedListPosition);
        String testId = String.format("%05d", test.getTestID());
        String patientId = test.getPatientID();
        Date testDate = test.getTestDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String testDateString = dateFormat.format(testDate);


        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.dashboard_expandable_listview_child, null);
        }
        TextView expandedListTestIDTextView = (TextView) convertView.findViewById(R.id.expandable_listview_child_textview_testId);
        TextView expandedListPatientIDTextView = (TextView) convertView.findViewById(R.id.expandable_listview_child_textview_patientId);
        TextView expandedListDateTimeTextView = (TextView) convertView.findViewById(R.id.expandable_listview_child_textview_datetime);
        expandedListTestIDTextView.setText(testId);
        expandedListPatientIDTextView.setText(patientId);
        expandedListDateTimeTextView.setText(testDateString);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.dashboard_expandable_listview_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.expandable_list_title);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}

package com.example.capstoneproject.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.capstoneproject.R;
import com.example.capstoneproject.model.Test;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private List<Test> mTestsList;
    private Context mContext;

    public TestAdapter(Activity context) {
        mTestsList = new ArrayList<>();
        mContext = context;
    }

    @Override
    public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.recyclerview_item, parent, false);
        return new TestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TestViewHolder holder, int position) {
        if (mTestsList != null) {
            Test current = mTestsList.get(position);
            holder.testIdTextView.setText(String.valueOf(current.getTestID()));
            holder.patientIdTextView.setText(current.getPatientID());
            holder.dateTimeTextView.setText(current.getTestTimeFormatted());
            holder.viewTestTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, RecordTestActivity.class);
                intent.putExtra("test_id", current.getTestID());
                mContext.startActivity(intent);
            });
        }
    }



    public void setTests(List<Test> tests){
        mTestsList.clear();
        mTestsList.addAll(tests);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mTestsList != null)
            return mTestsList.size();
        else return 0;
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        private final TextView testIdTextView;
        private final TextView patientIdTextView;
        private final TextView dateTimeTextView;
        private final TextView viewTestTextView;

        private TestViewHolder(View itemView) {
            super(itemView);
            testIdTextView = itemView.findViewById(R.id.recyclerview_textview_testId);
            patientIdTextView = itemView.findViewById(R.id.recyclerview_textview_patientId);
            dateTimeTextView = itemView.findViewById(R.id.recyclerview_textview_datetime);
            viewTestTextView = itemView.findViewById(R.id.recyclerview_viewtest_button);
        }
    }
}

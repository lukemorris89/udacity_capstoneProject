package com.example.capstoneproject.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.capstoneproject.R;
import com.example.capstoneproject.model.Test;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private List<Test> mTestsList;
    private Context mContext;

    @Override
    public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.recyclerview_item, parent, false);
        return new TestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TestViewHolder holder, int position) {
        if (mTestsList != null) {
            Test current = mTestsList.get(position);
            holder.testIdTextView.setText(current.getTestID());
            holder.patientIdTextView.setText(current.getPatientID());
            holder.dateTimeTextView.setText(current.getTestDateFormatted());
            holder.viewTestTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ViewTestActivity.class);
                    intent.putExtra("test_id", current.getTestID());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    void setTests(List<Test> tests){
        mTestsList = tests;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mTestsList != null)
            return mTestsList.size();
        else return 0;
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
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

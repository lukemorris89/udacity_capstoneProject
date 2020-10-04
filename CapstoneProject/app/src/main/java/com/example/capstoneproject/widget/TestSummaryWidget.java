package com.example.capstoneproject.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.example.capstoneproject.R;
import com.example.capstoneproject.data.TestDatabase;
import com.example.capstoneproject.model.Test;
import com.example.capstoneproject.ui.MainActivity;

import java.util.List;

public class TestSummaryWidget extends AppWidgetProvider {

    private static final String REFRESH_ACTION = "com.example.capstoneproject.widget.action.REFRESH";
    private static int[] mAppWidgetIds;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mAppWidgetIds = appWidgetIds;
        new UpdateWidgetAsyncTask().execute(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();

        if (REFRESH_ACTION.equals(action)) {
            new UpdateWidgetAsyncTask().execute(context);
        }
        super.onReceive(context, intent);
    }

    private static class UpdateWidgetAsyncTask extends AsyncTask<Context, Void, List<Test>> {
        private Context mContext;

        @Override
        protected List<Test> doInBackground(Context... contexts) {
            mContext = contexts[0];
            TestDatabase db = TestDatabase.getInstance(mContext);
            return db.testDao().loadAllTestsWidget();
        }

        @Override
        protected void onPostExecute(List<Test> tests) {
            if (mAppWidgetIds != null) {
                for (int appWidgetId : mAppWidgetIds) {
                    RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.test_summary_widget);
                    int positiveTestsCount = 0;
                    int negativeTestsCount = 0;
                    int inconclusiveTestsCount = 0;
                    if (tests != null && tests.size() > 0) {

                        for (Test test : tests) {
                            switch (test.getTestResult()) {
                                case "Positive":
                                    positiveTestsCount++;
                                    break;
                                case "Negative":
                                    negativeTestsCount++;
                                    break;
                                case "Inconclusive":
                                    inconclusiveTestsCount++;
                                    break;
                            }
                        }
                    }
                    views.setTextViewText(R.id.wheel_progress_positive_text, String.valueOf(positiveTestsCount));
                    views.setTextViewText(R.id.wheel_progress_negative_text, String.valueOf(negativeTestsCount));
                    views.setTextViewText(R.id.wheel_progress_inconclusive_text, String.valueOf(inconclusiveTestsCount));

                    Intent intent = new Intent(mContext, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.test_summary_widget_layout, pendingIntent);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }
    }
}


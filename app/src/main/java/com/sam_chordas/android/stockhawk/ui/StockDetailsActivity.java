package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodo on 2016. 09. 12..
 */

public class StockDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = StockDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_graph);

        Uri quoteUri = getIntent().getData();
        Log.d(LOG_TAG, quoteUri.toString());

        Cursor c = getContentResolver().query(
                quoteUri,
                null,
                null,
                null,
                QuoteColumns.CREATED + " ASC"
        );

        if (c != null) {
            if (c.moveToFirst()) {
                LineChart lineChartView = (LineChart) findViewById(R.id.linechart);
                float minValue = 0;
                float maxValue = 0;
                if (lineChartView != null) {
                    LineData set = new LineData();

                    List<Entry> entryList = new ArrayList<>();
                    int i = 0;
                    while (!c.isAfterLast()) {
                        float currentValue = Float.parseFloat(
                                c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE))
                        );
                        entryList.add(new Entry(i, currentValue));
                        i++;

                        if (maxValue < currentValue) {
                            maxValue = currentValue;
                        }

                        if (minValue > currentValue) {
                            minValue = currentValue;
                        }

                        c.moveToNext();
                    }
                    set.addDataSet(new LineDataSet(entryList, quoteUri.getLastPathSegment()));

                    lineChartView.setData(set);
                    lineChartView.invalidate();
                }
            }

            c.close();
        }
    }
}

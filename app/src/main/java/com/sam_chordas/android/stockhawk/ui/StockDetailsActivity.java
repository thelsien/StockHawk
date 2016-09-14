package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by frodo on 2016. 09. 12..
 */

public class StockDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = StockDetailsActivity.class.getSimpleName();
    private HashMap<Float, String> valueDateHashMap = new HashMap<>();

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
                QuoteColumns.CREATED + " DESC LIMIT 15"
        );

        if (c != null) {
            if (c.moveToLast()) {
                LineChart lineChartView = (LineChart) findViewById(R.id.linechart);
                if (lineChartView != null) {

                    LineDataSet lineDataSet = getLineDataSet(quoteUri, c);
                    lineDataSet.setColor(ColorTemplate.getHoloBlue());
                    lineChartView.setData(new LineData(lineDataSet));

                    setupXAxisWithTimeValues(lineChartView);
                    setupYAxis(lineChartView);

                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    lineChartView.setPinchZoom(false);
                    lineChartView.setScaleEnabled(false);
                    lineChartView.setDescription(String.format(getString(R.string.linechart_description_string), quoteUri.getLastPathSegment()));

                    Legend legend = lineChartView.getLegend();
                    legend.setEnabled(true);
                    legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);

                    lineChartView.invalidate();
                }
            }

            c.close();
        }
    }

    @NonNull
    private LineDataSet getLineDataSet(Uri quoteUri, Cursor c) {
        List<Entry> entryList = new ArrayList<>();
        int i = 0;
        while (!c.isBeforeFirst()) {
            float currentValue = Float.parseFloat(
                    c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE))
            );
            entryList.add(new Entry(i, currentValue));
            valueDateHashMap.put(Integer.valueOf(i).floatValue(), c.getString(
                    c.getColumnIndex(QuoteColumns.CREATED))
            );
            i++;

            c.moveToPrevious();
        }

        return new LineDataSet(entryList, quoteUri.getLastPathSegment());
    }

    private void setupXAxisWithTimeValues(LineChart lineChartView) {
        XAxis xAxis = lineChartView.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (valueDateHashMap.get(value) == null) {
                    return "";
                }
                return valueDateHashMap.get(value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
    }

    public void setupYAxis(LineChart lineChartView) {
        YAxis rightYAxis = lineChartView.getAxis(YAxis.AxisDependency.RIGHT);
        rightYAxis.setDrawLabels(false);
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawAxisLine(false);

        YAxis leftYAxis = lineChartView.getAxis(YAxis.AxisDependency.LEFT);
        leftYAxis.setDrawGridLines(false);
    }
}

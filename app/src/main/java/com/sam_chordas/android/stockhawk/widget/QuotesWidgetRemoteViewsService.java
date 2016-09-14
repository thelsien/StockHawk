package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import static com.sam_chordas.android.stockhawk.R.id.change;

public class QuotesWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mData = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (mData != null) {
                    mData.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                mData = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        null,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mData != null) {
                    mData.close();
                    mData = null;
                }
            }

            @Override
            public int getCount() {
                return mData == null ? 0 : mData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION
                        || mData == null || !mData.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                views.setTextViewText(R.id.stock_symbol, mData.getString(mData.getColumnIndex(QuoteColumns.SYMBOL)));
                views.setTextViewText(R.id.bid_price, mData.getString(mData.getColumnIndex("bid_price")));

                if (mData.getInt(mData.getColumnIndex("is_up")) == 1) {
                    views.setInt(change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);

                }
                if (Utils.showPercent) {
                    views.setTextViewText(change, mData.getString(mData.getColumnIndex("percent_change")));
                } else {
                    views.setTextViewText(change, mData.getString(mData.getColumnIndex("change")));
                }

                Log.d("QuotesWidget", "remote views completed, returning");

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mData.moveToPosition(position)) {
                    return mData.getLong(mData.getColumnIndex(QuoteColumns._ID));
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

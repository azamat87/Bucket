package com.example.pc.bucketdrops.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.pc.bucketdrops.AppBucketDrops;
import com.example.pc.bucketdrops.R;
import com.example.pc.bucketdrops.beans.Drop;
import com.example.pc.bucketdrops.extras.Util;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by PC on 31.01.2017.
 */

public class AdapterDrops extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeListener {

    public static final int COUNT_FOOTER = 1;
    public static final int COUNT_NO_ITEMS = 1;

    public static final int ITEM = 0;
    public static final int NO_ITEM = 1;
    public static final int FOOTER = 2;
    private ResetListener mResetListener;
    private int mFilterOption;

    private MarkListener mMarkListener;

    private LayoutInflater mInflater;
    private RealmResults<Drop> mResults;
    private Realm mRealm;
    private Context mContext;

    private AddListener mAddListener;

    public AdapterDrops(Context context, Realm realm,RealmResults<Drop> results) {
        mInflater = LayoutInflater.from(context);
        mRealm = realm;
        update(results);
    }

    public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results, AddListener addListener, MarkListener markListener, ResetListener resetListener) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        update(results);
        mRealm = realm;
        mAddListener = addListener;
        mMarkListener = markListener;
        mResetListener = resetListener;

    }

//    public void setAddListener(AddListener addListener) {
//        mAddListener = addListener;
//    }

    public void update(RealmResults<Drop> results) {
        mResults = results;
        mFilterOption = AppBucketDrops.load(mContext);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER) {
            View view = mInflater.inflate(R.layout.footer, parent, false);
            return new FooterHolder(view, mAddListener);
        } else if (viewType == NO_ITEM) {
            View view = mInflater.inflate(R.layout.na_item, parent, false);
            return new NoItemsHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.row_drop, parent, false);
            return new DropHolder(view, mMarkListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DropHolder) {
            DropHolder dropHolder = (DropHolder) holder;
            Drop drop = mResults.get(position);
            dropHolder.mTextWhat.setText(drop.getWhat());
            dropHolder.setWhat(drop.getWhat());
            dropHolder.setWhen(drop.getWhen());
            dropHolder.setBackground(drop.isCompleted());
        }
    }

    @Override
    public long getItemId(int position) {
        if (position < mResults.size()) {
            return mResults.get(position).getAdded();
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        if (!mResults.isEmpty()) {
            return mResults.size() + COUNT_FOOTER;
        } else {
            if (mFilterOption == Filter.LEAST_TIME_LEFT
                    || mFilterOption == Filter.MOSTE_TIME_LEFT
                    || mFilterOption == Filter.NONE) {
                return 0;
            } else {
                return COUNT_NO_ITEMS + COUNT_FOOTER;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!mResults.isEmpty()) {
            if (position < mResults.size()) {
                return ITEM;
            } else {
                return FOOTER;
            }
        } else {
            if (mFilterOption == Filter.COMPLETE
                    || mFilterOption == Filter.INCOMPLETE) {
                if (position == 0) {
                    return NO_ITEM;
                } else {
                    return FOOTER;
                }
            } else {
                return ITEM;
            }
        }
    }

    @Override
    public void onSwipe(int position) {
        if (position < mResults.size()) {
            mRealm.beginTransaction();
            mResults.get(position).deleteFromRealm();
            mRealm.commitTransaction();
            notifyItemRemoved(position);
        }
        resetFilterIfEmpty();
    }

    private void resetFilterIfEmpty() {
        if (mResults.isEmpty() && (mFilterOption == Filter.COMPLETE ||
                mFilterOption == Filter.INCOMPLETE)) {
            mResetListener.Reset();
        }
    }

    public void markComplete(int position) {
        if (position < mResults.size()) {
            mRealm.beginTransaction();
            mResults.get(position).setCompleted(true);
            mRealm.commitTransaction();
            notifyItemChanged(position);
        }
    }

    public static class DropHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTextWhat;
        TextView mTextWhen;
        MarkListener mMarkListener;
        Context mContext;
        View mItemView;

        public DropHolder(View itemView, MarkListener listener) {
            super(itemView);
            mItemView = itemView;
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
            mTextWhat = (TextView) itemView.findViewById(R.id.tv_what);
            mTextWhen = (TextView) itemView.findViewById(R.id.tv_when);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/raleway_thin.ttf");
            mTextWhat.setTypeface(typeface);
            mTextWhen.setTypeface(typeface);

            mMarkListener = listener;
        }

        public void setWhat(String what) {
            mTextWhat.setText(what);
        }

        public void setWhen(long when) {
            mTextWhen.setText(DateUtils.getRelativeTimeSpanString(when, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_MONTH));
        }

        @Override
        public void onClick(View view) {
            mMarkListener.onMark(getAdapterPosition());
        }

        public void setBackground(boolean completed) {
            Drawable drawable;
            if (completed) {
                drawable = ContextCompat.getDrawable(mContext, R.color.bg_drop_complete);
            } else {
                drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_row_drop);
            }
            Util.setBackground(itemView, drawable);
        }
    }

    public static class NoItemsHolder extends RecyclerView.ViewHolder {

        public NoItemsHolder(View itemView) {
            super(itemView);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button mBtnAdd;
        AddListener mAddListener;

//        public FooterHolder(View itemView) {
//            super(itemView);
//            mBtnAdd = (Button) itemView.findViewById(R.id.btn_footer);
//            mBtnAdd.setOnClickListener(this);
//        }


        public FooterHolder(View itemView, AddListener addListener) {
            super(itemView);
            mBtnAdd = (Button) itemView.findViewById(R.id.btn_footer);
            mBtnAdd.setOnClickListener(this);
            mAddListener = addListener;
        }

        @Override
        public void onClick(View view) {
            mAddListener.add();
        }
    }
}

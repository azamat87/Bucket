package com.example.pc.bucketdrops;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.pc.bucketdrops.adapters.AdapterDrops;
import com.example.pc.bucketdrops.adapters.AddListener;
import com.example.pc.bucketdrops.adapters.CompleteListener;
import com.example.pc.bucketdrops.adapters.Divider;
import com.example.pc.bucketdrops.adapters.Filter;
import com.example.pc.bucketdrops.adapters.MarkListener;
import com.example.pc.bucketdrops.adapters.ResetListener;
import com.example.pc.bucketdrops.adapters.SimpleTouchCallback;
import com.example.pc.bucketdrops.adapters.SwipeListener;
import com.example.pc.bucketdrops.beans.Drop;
import com.example.pc.bucketdrops.extras.Util;
import com.example.pc.bucketdrops.service.NotificationService;
import com.example.pc.bucketdrops.widgets.BucketRecyclerView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ActivityMain extends AppCompatActivity {

    Toolbar mToolbar;
    BucketRecyclerView mRecycler;

    Realm mRealm;
    RealmResults<Drop> results;
    View mEmptyView;
    AdapterDrops mAdapter;

    private RealmChangeListener mChangeListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            mAdapter.update(results);

        }
    };

    private MarkListener mMarkListener = new MarkListener() {
        @Override
        public void onMark(int position) {
            showDialogMark(position);
        }
    };

    private AddListener mAddListener = new AddListener() {
        @Override
        public void add() {
            showDialogAdd();
        }
    };

    private CompleteListener mCompleteListener = new CompleteListener() {
        @Override
        public void onComplete(int position) {
            mAdapter.markComplete(position);
        }
    };

    private ResetListener mResetListener = new ResetListener() {
        @Override
        public void Reset() {
            AppBucketDrops.save(ActivityMain.this, Filter.NONE);
            loadResult(Filter.NONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRealm = Realm.getDefaultInstance();
        int filterOption = AppBucketDrops.load(this);
        loadResult(filterOption);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mEmptyView = findViewById(R.id.empty_drops);
        mRecycler = (BucketRecyclerView) findViewById(R.id.rv_drops);
        mRecycler.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL));
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mRecycler.hideIfEmpty(mToolbar);
        mRecycler.showIfEmpty(mEmptyView);

        mAdapter = new AdapterDrops(this, mRealm, results, mAddListener, mMarkListener, mResetListener);
        mAdapter.setHasStableIds(true);

        mRecycler.setAdapter(mAdapter);

        SimpleTouchCallback callback = new SimpleTouchCallback(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecycler);

        setSupportActionBar(mToolbar);
        initBackgroundImage();
        Util.scheduleAlarm(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean handled = true;
        int filterOption = Filter.NONE;
        switch (id) {
            case R.id.action_add:
                showDialogAdd();
                break;
            case R.id.action_none:
                filterOption = Filter.NONE;

                break;
            case R.id.action_sort_ascending_date:
                filterOption = Filter.LEAST_TIME_LEFT;

                break;
            case R.id.action_sort_descending_date:
                filterOption = Filter.MOSTE_TIME_LEFT;

                break;
            case R.id.action_sort_complete:
                filterOption = Filter.COMPLETE;

                break;
            case R.id.action_sort_incomplete:
                filterOption = Filter.INCOMPLETE;

                break;
            default:
                handled = false;
                break;
        }
        AppBucketDrops.save(this, filterOption);
        loadResult(filterOption);
        return handled;
    }

    private void loadResult(int filterOption) {
        switch (filterOption) {
            case Filter.NONE:
                results = mRealm.where(Drop.class).findAllAsync();
                break;
            case Filter.LEAST_TIME_LEFT:
                results = mRealm.where(Drop.class).findAllSortedAsync("when");
                break;
            case Filter.MOSTE_TIME_LEFT:
                results = mRealm.where(Drop.class).findAllSortedAsync("when", Sort.DESCENDING);
                break;
            case Filter.COMPLETE:
                results = mRealm.where(Drop.class).equalTo("completed", true).findAllAsync();
                break;
            case Filter.INCOMPLETE:
                results = mRealm.where(Drop.class).equalTo("completed", false).findAllAsync();
                break;
        }
        results.addChangeListener(mChangeListener);
    }


    @Override
    protected void onStart() {
        super.onStart();
        results.addChangeListener(mChangeListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        results.removeChangeListener(mChangeListener);
    }

    private void initBackgroundImage() {
        ImageView imageView = (ImageView) findViewById(R.id.iv_background);
        Glide.with(this)
                .load(R.drawable.background)
                .centerCrop()
                .into(imageView);
    }

    public void showDialog(View view) {
        showDialogAdd();
    }

    public void showDialogAdd() {
        DialogAdd dialog = new DialogAdd();
        dialog.show(getSupportFragmentManager(), "Add");

    }

    public void showDialogMark(int position) {
        DialogMark dialog = new DialogMark();
        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        dialog.setArguments(bundle);
        dialog.setCompleteListener(mCompleteListener);
        dialog.show(getSupportFragmentManager(), "Mark");

    }
}

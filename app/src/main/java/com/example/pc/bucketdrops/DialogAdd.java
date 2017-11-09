package com.example.pc.bucketdrops;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.pc.bucketdrops.beans.Drop;
import com.example.pc.bucketdrops.widgets.BucketPickerView;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by PC on 27.01.2017.
 */

public class DialogAdd extends DialogFragment {

    private ImageButton mBtnClose;
    private EditText mInputWhat;
    private BucketPickerView mInputWhen;
    private Button mBtnAdd;

    public DialogAdd() {
    }

    private View.OnClickListener mBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_add_it:
                    addAction();
                    break;
                case R.id.btn_close:

                    dismiss();

                    break;
            }
        }
    };

    private void addAction() {

        String what = mInputWhat.getText().toString();

        long now = System.currentTimeMillis();
        Realm realm = Realm.getDefaultInstance();
        Drop drop = new Drop(what, now, mInputWhen.getTime(), false);
        realm.beginTransaction();
        realm.copyToRealm(drop);
        realm.commitTransaction();
        realm.close();

        dismiss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add, container, false);
        Realm.init(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnClose = (ImageButton) view.findViewById(R.id.btn_close);
        mInputWhat = (EditText) view.findViewById(R.id.ed_drop);
        mInputWhen = (BucketPickerView) view.findViewById(R.id.dpv_date);
        mBtnAdd = (Button) view.findViewById(R.id.btn_add_it);

        mBtnClose.setOnClickListener(mBtnListener);
        mBtnAdd.setOnClickListener(mBtnListener);
    }
}

package com.example.pc.bucketdrops.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.example.pc.bucketdrops.R;

/**
 * Created by PC on 03.02.2017.
 */

public class Divider extends RecyclerView.ItemDecoration {

    private Drawable divider;
    private int mOrientation;

    public Divider(Context context, int orientation) {
        divider = ContextCompat.getDrawable(context, R.drawable.divider);
        if (orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("This Item Decoration can be used only with a RecyclerView that uses a LinearLayoutManager with vertical orieantation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawHorizontalDivider(c, parent, state);
        }
    }

    private void drawHorizontalDivider(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left, top, right, bottom;
        left = parent.getPaddingLeft();
        right = parent.getWidth() - parent.getPaddingRight();
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            if (AdapterDrops.FOOTER != parent.getAdapter().getItemViewType(i)) {
                View current = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) current.getLayoutParams();
                top = current.getTop() - params.topMargin;
                bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        }
    }
}

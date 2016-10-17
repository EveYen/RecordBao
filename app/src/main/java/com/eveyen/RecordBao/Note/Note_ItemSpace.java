package com.eveyen.RecordBao.Note;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eveyen on 2016/9/9.
 */
public class Note_ItemSpace extends RecyclerView.ItemDecoration{
    private int space;

    public Note_ItemSpace(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildPosition(view) >= 0) {
            outRect.top = space;
            outRect.bottom = space;
            outRect.left = space;
            outRect.right = space;
        }
    }

}
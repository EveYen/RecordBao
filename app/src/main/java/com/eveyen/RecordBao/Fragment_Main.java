package com.eveyen.RecordBao;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eveyen.RecordBao.Note.Note_Adapter;
import com.eveyen.RecordBao.Note.Note_ItemSpace;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.util.ArrayList;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：便條紙畫面/實現右滑刪除/下拉刷新
 */

public class Fragment_Main extends Fragment {
    private View v;

    private SQL_implement item;
    private ArrayList<SQL_Item> lists;
    private RecyclerView rvContacts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_duration);
        rvContacts = (RecyclerView) v.findViewById(R.id.recyclerView);
        rvContacts.addItemDecoration(new Note_ItemSpace(spacingInPixels)); //便條紙間隔
        initRecyclerView();
        initSwipeView();
        return v;
    }

    public void initRecyclerView(){
        item = new SQL_implement(getContext());
        lists = SQL_implement.getAll();

        final Note_Adapter adapter = new Note_Adapter(getContext(),lists);

        rvContacts.getItemAnimator().setMoveDuration(300);
        rvContacts.getItemAnimator().setChangeDuration(300);
        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));

    }

    public void initSwipeView(){
        /**
         * 實現下拉刷新
         */
        final SwipeRefreshLayout swiperefreshlayout = (SwipeRefreshLayout) v.findViewById(R.id.demo_swiperefreshlayout);
        swiperefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swiperefreshlayout.setColorSchemeResources(R.color.swipe_blue,R.color.swipe_red,R.color.swipe_green,R.color.swipe_orenge);//刷新進度條顏色
        swiperefreshlayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initRecyclerView();
                        swiperefreshlayout.setRefreshing(false);
                        Snackbar.make(v, "已更新", Snackbar.LENGTH_SHORT).show();
                    }
                }, 2500);//刷新秒數
            }
        });
    }
}
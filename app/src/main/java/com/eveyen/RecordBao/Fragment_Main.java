package com.eveyen.RecordBao;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eveyen.RecordBao.Note.Note_Adapter;
import com.eveyen.RecordBao.Note.Note_ItemSpace;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.io.File;
import java.util.ArrayList;

/**
 *
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
        ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
            //
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                File f = new File(lists.get(position).getFileName());
                if(f.exists()){
                    item.delete(lists.get(position));
                    f.delete();
                    lists.remove(position);
                    adapter.notifyItemRemoved(position);
                    Snackbar.make(v, "文件已刪除", Snackbar.LENGTH_SHORT).show();
                }else {
                    Snackbar.make(v, "文件不存在", Snackbar.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mCallback);

        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        itemTouchHelper.attachToRecyclerView(rvContacts);

    }

    public void initSwipeView(){
        /**
         * 實現下拉刷新
         */
        final SwipeRefreshLayout swiperefreshlayout = (SwipeRefreshLayout) v.findViewById(R.id.demo_swiperefreshlayout);
        swiperefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,android.R.color.holo_orange_light,
                android.R.color.holo_green_light);//刷新進度條顏色
        swiperefreshlayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));

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
                }, 3500);//刷新秒數
            }
        });
    }
}

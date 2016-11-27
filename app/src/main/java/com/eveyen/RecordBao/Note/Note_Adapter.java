package com.eveyen.RecordBao.Note;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eveyen.RecordBao.CalendarHelper;
import com.eveyen.RecordBao.R;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：RecyclerView/點擊擴展/按鈕
 **/
public class Note_Adapter extends RecyclerView.Adapter<Note_Adapter.ViewHolder> {

    private Context mContext;
    private SQL_implement item;
    private List<SQL_Item> mlist;
    public static int opened = -1;
    public Note_Adapter(Context c , List<SQL_Item> list){
        mlist = list;
        mContext = c;
        item = new SQL_implement(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        final View contactView = LayoutInflater.from(context).inflate(R.layout.listitem_note, parent, false);
        //按下List元件
        ViewHolder viewHolder = new ViewHolder(contactView, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                if (opened == position) {
                    opened = -1;
                    notifyItemChanged(position);
                }
                else {
                    int oldOpened = opened;
                    opened = position;
                    notifyItemChanged(oldOpened);
                    notifyItemChanged(opened);
                }
            }
        });
        return viewHolder;
    }

    private void systemPlay(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/MP3");
        mContext.startActivity(intent);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SQL_Item temp = mlist.get(position);
        TextView tv_title = holder.tv_title;
        TextView tv_content = holder.tv_content;
        TextView tv_info = holder.tv_info;
        LinearLayout lv_note = holder.lv_note;
        holder.bind(position);
        lv_note.setBackgroundColor(temp.getColor());
        String[] stitle=temp.getTitle().split("_");
        tv_info.setText(temp.getId()+"\n時間："+temp.getScheduleDate() +"\n地點："+ temp.getScheduleLocation() + "\n與："+ temp.getContact() );
        tv_title.setText(stitle[1]+"/"+stitle[2]+"   "+stitle[3]+":"+stitle[4]+":"+stitle[5].split(".wav")[0]);
        tv_content.setText(temp.getContent());
        Log.e("NoteAdapter","top"+String.valueOf(temp.getId())+"="+String.valueOf(temp.getTop()));
        if(temp.getTop()==0){
            holder.btn_top.setBackgroundResource(R.drawable.note_ntop);
        }
        else{
            holder.btn_top.setBackgroundResource(R.drawable.note_top);
        }

        holder.btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(mlist.get(position).getFileName());
                if(f.exists()){
                    systemPlay(f);
                }else {
                    Snackbar.make(v, "無法播放", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(mlist.get(position).getFileName());
                if(f.exists()){
                    item.delete(mlist.get(position));
                    f.delete();
                    mlist.remove(position);
                    notifyItemRemoved(position);
                    Snackbar.make(v, "文件已刪除", Snackbar.LENGTH_SHORT).show();
                }else {
                    Snackbar.make(v, "文件不存在", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        holder.btn_addcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int fromPosition = position;
                //int toPosition = 1;
                //notifyItemMoved(fromPosition, toPosition);
                //建立事件開始時間
                SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm EEEE");
                Calendar beginTime = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();
                try {
                    beginTime.setTime(df2.parse(temp.getScheduleDate()));//建立事件結束時間
                    endTime.setTime(df2.parse(temp.getScheduleDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                CalendarHelper calIntent = new CalendarHelper();

                calIntent.setTitle(temp.getContent());
                calIntent.setDescription("與"+temp.getContact());
                calIntent.setBeginTimeInMillis(beginTime.getTimeInMillis());
                calIntent.setEndTimeInMillis(endTime.getTimeInMillis());
                calIntent.setLocation(temp.getScheduleLocation());

                Intent intent = calIntent.getIntentAfterSetting();//送出意圖
                mContext.startActivity(intent);
            }
        });
        holder.btn_top.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(temp.getTop()==0){
                    holder.btn_top.setBackgroundResource(R.drawable.note_top);
                    mlist.get(position).setTop(1);
                }
                else{
                    holder.btn_top.setBackgroundResource(R.drawable.note_ntop);
                    mlist.get(position).setTop(0);
                }
                item.update(mlist.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tv_title,tv_content,tv_info;
        public LinearLayout lv_note,lv_info;
        public MyViewHolderClick mListener;
        public Button btn_delete,btn_play, btn_addcal, btn_top;

        public ViewHolder(View itemView, MyViewHolderClick listener){
            super(itemView);
            mListener = listener;
            lv_note = (LinearLayout) itemView.findViewById(R.id.lv_note);
            lv_info = (LinearLayout) itemView.findViewById(R.id.lv_info);
            tv_title = (TextView) itemView.findViewById(R.id.ItemName);
            tv_content = (TextView) itemView.findViewById(R.id.ItemTrans);
            tv_info = (TextView) itemView.findViewById(R.id.ItemInfo);
            btn_play = (Button) itemView.findViewById(R.id.ItemPlay);
            btn_delete = (Button) itemView.findViewById(R.id.itemDel) ;
            btn_addcal = (Button) itemView.findViewById(R.id.ItemCal);
            btn_top = (Button) itemView.findViewById(R.id.ItemTop);
            lv_note.setOnClickListener(this);
        }

        public void bind(int pos) {
            if (pos == opened)
                lv_info.setVisibility(View.VISIBLE);
            else
                lv_info.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }
        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }
    }
}

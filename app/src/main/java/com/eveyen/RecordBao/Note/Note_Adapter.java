package com.eveyen.RecordBao.Note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eveyen.RecordBao.CKIP.Text_mining;
import com.eveyen.RecordBao.CalendarHelper;
import com.eveyen.RecordBao.R;
import com.eveyen.RecordBao.SQL.SQL_Item;
import com.eveyen.RecordBao.SQL.SQL_implement;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：RecyclerView/點擊擴展/按鈕
 **/
public class Note_Adapter extends RecyclerView.Adapter<Note_Adapter.ViewHolder> {

    private Context mContext;
    private SQL_implement item;
    private List<SQL_Item> mlist;
    Text_mining text_mining;
    public static int opened = -1;
    public int[] colors = {Color.parseColor("#ffa6bc"),//粉紅
            Color.parseColor("#FFA6CEFF"),//天空藍
            Color.parseColor("#FFFFEF9F"),//鵝黃色
            Color.parseColor("#FFB8FFBC"),//草綠色
            Color.parseColor("#FFFFC894"),//橘色
            };
    public Note_Adapter(Context c , List<SQL_Item> list){
        mlist = list;
        resetMList();
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
        if(temp.getScheduleLocation() == null) temp.setScheduleLocation("");
        tv_info.setText("時間："+temp.getScheduleDate() +"\n地點："+ temp.getScheduleLocation() + "\n與："+ temp.getContact() );
        tv_title.setText("記錄於"+stitle[1]+"/"+stitle[2]+"   "+stitle[3]+":"+stitle[4]+":"+stitle[5].split(".wav")[0]);
        tv_content.setText(temp.getContent());

        if(mlist.get(position).getTop()==0){
            holder.btn_top.setImageResource(R.drawable.note_ntop);
        }
        else{
            holder.btn_top.setImageResource(R.drawable.note_top);
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
            public void onClick(final View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("確定要刪除嗎?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
            }
        });
        holder.btn_addcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm EEEE");
                Calendar beginTime = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();
                boolean allday = false;
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
                calIntent.setAllDay(allday);
                calIntent.setLocation(temp.getScheduleLocation());

                Intent intent = calIntent.getIntentAfterSetting();//送出意圖
                mContext.startActivity(intent);
            }
        });
        holder.btn_top.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(temp.getTop()==0){
                    holder.btn_top.setImageResource(R.drawable.note_top);
                    temp.setTop(1);
                    item.update(temp);
                }
                else{
                    holder.btn_top.setImageResource(R.drawable.note_ntop);
                    temp.setTop(0);
                    item.update(temp);
                }
                resetMList();
                notifyDataSetChanged();
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Alert(temp,position);
            }
        });

        holder.btn_color.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ColorPicker colorPicker = new ColorPicker(mContext);
                colorPicker.setColors(colors).setRoundColorButton(true).setTitle("請選擇便條顏色");
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int colorposition,int color) {
                            if(colorposition>-1){
                            temp.setColor(color);
                            item.update(temp);
                            notifyItemChanged(position);
                            }
                    }
                    @Override
                    public void onCancel(){

                    }
                });
                colorPicker.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public void resetMList(){
        Collections.sort(mlist, new Comparator<SQL_Item>() {
            @Override
            public int compare(SQL_Item lhs, SQL_Item rhs) {
                return (int)lhs.getDatetime()-(int)rhs.getDatetime();
            }
        });
        Collections.sort(mlist, new Comparator<SQL_Item>() {
            @Override
            public int compare(SQL_Item lhs, SQL_Item rhs) {
                return rhs.getTop()-lhs.getTop();
            }
        });
    }

    public void Alert(final SQL_Item temp, final int position) {

        final Handler updateProHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 500) {
                    item.update(temp);
                    resetMList();
                    notifyDataSetChanged();
                }
            }
        };

        final View vitem = LayoutInflater.from(mContext).inflate(R.layout.editdialog, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setView(vitem);
        final EditText editText = (EditText) vitem.findViewById(R.id.edittext);
        editText.setText(temp.getContent());
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newcontent = editText.getText().toString();
                temp.setContent(editText.getText().toString());

                new Thread(){
                    @Override
                    public void run(){
                        text_mining = new Text_mining(mContext, newcontent); //傳上去CKIP
                        temp.setScheduleDate(text_mining.getDate());
                        try {
                            temp.setScheduleLocation(text_mining.getLocation());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        updateProHandler.sendEmptyMessage(500);
                    }
                }.start();
                Log.e("TAG",newcontent);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tv_title,tv_content,tv_info;
        public LinearLayout lv_note,lv_info;
        public MyViewHolderClick mListener;
        public ImageButton  btn_addcal , btn_edit , btn_color,btn_play, btn_delete, btn_top;

        public ViewHolder(View itemView, MyViewHolderClick listener){
            super(itemView);
            mListener = listener;
            lv_note = (LinearLayout) itemView.findViewById(R.id.lv_note);
            lv_info = (LinearLayout) itemView.findViewById(R.id.lv_info);
            tv_title = (TextView) itemView.findViewById(R.id.ItemName);
            tv_content = (TextView) itemView.findViewById(R.id.ItemTrans);
            tv_info = (TextView) itemView.findViewById(R.id.ItemInfo);
            btn_play = (ImageButton) itemView.findViewById(R.id.ItemPlay);
            btn_delete = (ImageButton) itemView.findViewById(R.id.itemDel) ;
            btn_addcal = (ImageButton) itemView.findViewById(R.id.ItemCal);
            btn_edit = (ImageButton) itemView.findViewById(R.id.ItemEdit);
            btn_color = (ImageButton) itemView.findViewById(R.id.ItemColor);
            btn_top = (ImageButton) itemView.findViewById(R.id.ItemTop);
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

package com.eveyen.RecordBao.Note;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eveyen.RecordBao.R;
import com.eveyen.RecordBao.SQL.SQL_Item;

import java.io.File;
import java.util.List;

/**
 *  作者：EveYen
 *  最後修改日期：10/12
 *  完成功能：RecyclerView
 **/
public class Note_Adapter extends RecyclerView.Adapter<Note_Adapter.ViewHolder> {

    private Context mContext;
    private List<SQL_Item> mlist;
    public Note_Adapter(Context c , List<SQL_Item> list){
        mlist = list;
        mContext = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        final View contactView = LayoutInflater.from(context).inflate(R.layout.listitem_note, parent, false);
        //按下List元件
        ViewHolder viewHolder = new ViewHolder(contactView, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                SQL_Item temp= mlist.get(position);
                File f = new File(mlist.get(position).getFileName());
                if(f.exists()){
                    systemPlay(f);
                }else {
                    Snackbar.make(contactView, "unable to play", Snackbar.LENGTH_SHORT).show();
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        SQL_Item temp = mlist.get(position);
        TextView tv_title = holder.tv_title;
        TextView tv_content = holder.tv_content;
        //TextView tv_time = holder.tv_time;
        LinearLayout lv_note = holder.lv_note;
        lv_note.setBackgroundColor(temp.getColor());
        String[] stitle=temp.getTitle().split("_");
        tv_title.setText(stitle[1]+"/"+stitle[2]+"   "+stitle[3]+":"+stitle[4]+":"+stitle[5].split(".wav")[0]);
        tv_content.setText(temp.getContent());
        //tv_time.setText(Long.toString(temp.getDatetime()));
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tv_title,tv_content,tv_time;
        public LinearLayout lv_note;
        public MyViewHolderClick mListener;
        public ViewHolder(View itemView, MyViewHolderClick listener){
            super(itemView);
            mListener = listener;
            lv_note = (LinearLayout) itemView.findViewById(R.id.lv_note);
            tv_title = (TextView) itemView.findViewById(R.id.ItemName);
            tv_content = (TextView) itemView.findViewById(R.id.ItemTrans);
            //tv_time = (TextView) itemView.findViewById(R.id.ItemTime);
            lv_note.setOnClickListener(this);
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

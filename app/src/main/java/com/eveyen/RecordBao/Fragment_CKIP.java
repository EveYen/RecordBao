package com.eveyen.RecordBao;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eveyen.RecordBao.CKIP.Text_mining;

import java.util.ArrayList;

/**
 *  作者：EveYen
 *  最後修改日期：10/30
 *  完成功能：測試CKIP介面
 */

public class Fragment_CKIP extends Fragment {
    /**
     * init View element
     */
    private View v;
    private EditText editText;
    private TextView textView;
    private Button OK, Clear;
    /**
     * text mining element
     */
    Text_mining text_mining;
    ArrayList<String> inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
    ArrayList<String> TagList = new ArrayList<String>();   //宣告動態陣列 存切詞的詞性
    ArrayList<Boolean> DoneList = new ArrayList<Boolean>();
    ArrayList<String[]> Contact = new ArrayList<String[]>();
    String SDate = "";
    String Person = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ckip, container, false);
        initView();
        initListener();
        return v;
    }


    public void initView(){
        editText = (EditText) v.findViewById(R.id.test_input);
        OK = (Button) v.findViewById(R.id.test_btn);
        Clear = (Button) v.findViewById(R.id.test_clear);
        textView = (TextView) v.findViewById(R.id.test_output);
    }

    public void initListener() {
        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText()!=null){
                    final String teststring = editText.getText().toString();
                    new Thread(){
                        @Override
                        public void run(){
                            text_mining = new Text_mining(getContext(),teststring);
                            inputList = text_mining.getInputList();
                            TagList = text_mining.getTagList();
                            DoneList = text_mining.getDoneList();
                            SDate = text_mining.getDate();
                            Person = text_mining.getPerson();
                            Contact = text_mining.getContactsName();
                            updateProHandler.sendEmptyMessage(500);
                        }
                    }.start();
                }
            }
        });
        Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    textView.setText(null);
                    editText.setText(null);

            }
        });
    }

    Handler updateProHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 500) {
                for(int i=0;i<inputList.size();i++){
                    textView.append(inputList.get(i));
                    textView.append("("+TagList.get(i)+")");
                    textView.append("["+DoneList.get(i)+"]\n");
                }
                textView.append("\n");
                if(!SDate.equals("")){
                    textView.append("時間："+ SDate);
                    textView.append("\n");
                }
                if(Person!=null){
                    textView.append("與："+ Person);
                    textView.append("\n");
                }
                for(int j=0;j<Contact.size();j++){
                    textView.append(Contact.get(j)[0]+"/" + Contact.get(j)[1]+"\n");
                }
            }
        }
    };
}

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
 * A simple {@link Fragment} subclass.
 */
public class Fragment_CKIP extends Fragment {
    private View v;
    private EditText editText;
    private TextView textView;
    private Button OK, Clear;
    Text_mining text_mining;

    ArrayList<String> inputList = new ArrayList<String>(); //宣告動態陣列 存切詞的name
    ArrayList<String> TagList = new ArrayList<String>();   //宣告動態陣列 存切詞的詞性
    String SDate = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ckip, container, false);
        editText = (EditText) v.findViewById(R.id.test_input);
        OK = (Button) v.findViewById(R.id.test_btn);
        Clear = (Button) v.findViewById(R.id.test_clear);
        textView = (TextView) v.findViewById(R.id.test_output);
        initListener();
        return v;
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
                            text_mining = new Text_mining(teststring);
                            inputList = text_mining.getInputList();
                            TagList = text_mining.getTagList();
                            SDate = text_mining.getDate();
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
                    textView.append("("+TagList.get(i)+")   ");
                }
                textView.append("\n");
                textView.append("時間："+SDate);
                textView.append("\n");

            }
        }
    };



}

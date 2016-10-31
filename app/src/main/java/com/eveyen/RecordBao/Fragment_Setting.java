package com.eveyen.RecordBao;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Setting extends Fragment {
    private View v;
    private Switch s_flow;

    public Fragment_Setting() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        s_flow = (Switch) v.findViewById(R.id.switch_flow);
        s_flow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getActivity().startService(new Intent(getActivity(),FloatWindows.class));
                } else {
                    getActivity().stopService(new Intent(getActivity(),FloatWindows.class));
                }
            }
        });
        return v;
    }

}

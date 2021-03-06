package com.example.pisua.pisua.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pisua.pisua.R;

/**
 * Created by Pisua on 2015/9/18.
 */
public class IntroFragment extends Fragment{

    private String destinationTitle;

    public IntroFragment() {
    }

    @SuppressLint("ValidFragment")
    public IntroFragment(String destinationTitle) {
        this.destinationTitle = destinationTitle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_destination, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.direction_text_view);

        title.setText(destinationTitle);
        return rootView;
    }

}
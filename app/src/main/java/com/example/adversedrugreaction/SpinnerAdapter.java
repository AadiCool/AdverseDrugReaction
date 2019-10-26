package com.example.adversedrugreaction;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Typeface font;

    SpinnerAdapter(@NonNull Context context, int resource, List<String> items) {
        super(context, resource, items);
        font = ResourcesCompat.getFont(context, R.font.oswaldlightitalic);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(font);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setTypeface(font);
        return view;
    }

}

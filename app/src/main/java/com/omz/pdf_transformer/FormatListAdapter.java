package com.omz.pdf_transformer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

public class FormatListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> formatNamesList;

    FormatListAdapter(Context context, ArrayList<String> formatNamesList) {
        this.context = context;
        this.formatNamesList = formatNamesList;
    }

    @Override
    public int getCount() {
        return formatNamesList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.formatNamesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.formatNamesList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = View.inflate(this.context, R.layout.format_names_listview, null);
        TextView noteNamesText = view.findViewById(R.id.formatNamesText);
        noteNamesText.setText(formatNamesList.get(position));
        return view;
    }
}


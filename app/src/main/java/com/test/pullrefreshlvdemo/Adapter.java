package com.test.pullrefreshlvdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by q742972035 on 2016/7/2.
 */
public class Adapter extends BaseAdapter {
    private List<String> mList;
    private LayoutInflater li;
    private Context mContext;

    public Adapter(Context context,List<String> list){
        mContext = context;
        mList = list;
        li = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = li.inflate(R.layout.item_list,null);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        TextView tv = (TextView) convertView.findViewById(R.id.tv);
        tv.setText(mList.get(position));
        return convertView;
    }
}

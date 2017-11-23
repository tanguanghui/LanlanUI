package com.ui.Adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.Interface.View.AdapterCallback;
import com.Interface.View.OnItemOnclickCallback;
import com.entity.IntelligentUpdateBean;
import com.uidemo.R;

import java.util.ArrayList;

/**
 * Created by Eren on 2017/5/22.
 */
public class IntelligentAdapter extends RecyclerView.Adapter {

    //关键词集合，用于适配器加载数据
    private ArrayList<ArrayList<IntelligentUpdateBean.VicCodetextBean>> myArrayList = new ArrayList<ArrayList<IntelligentUpdateBean.VicCodetextBean>>();
    private Context context;
    private OnItemOnclickCallback adapterCallback;
    private ReplayAdapter replayAdapter;

    public IntelligentAdapter(ArrayList<ArrayList<IntelligentUpdateBean.VicCodetextBean>> myArrayListt, Context context, OnItemOnclickCallback adapterCallback) {
        this.myArrayList=myArrayListt;
        this.context = context;
        this.adapterCallback = adapterCallback;
    }

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.intelligent_list, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;


        myViewHolder.key.setText("关键词：" + myArrayList.get(position).get(0).getKeyword());
        Log.e("1233", "1111" +myArrayList.get(position).get(0).getKeyword()+"  "+position);

        replayAdapter = new ReplayAdapter(myArrayList.get(position), context, new AdapterCallback() {
            @Override
            public void deletCallback(String keyword, String content) {

            }

            @Override
            public void updateCallback(String keyword, String content) {

            }
        });
        myViewHolder.content.setAdapter(replayAdapter);
        myViewHolder.linear_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterCallback.OnItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {

        return myArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView key;
        RecyclerView content;
        LinearLayout linear_main;


        public MyViewHolder(View view) {
            super(view);
            linear_main= (LinearLayout) view.findViewById(R.id.linear_main);
            key = (TextView) view.findViewById(R.id.intelligent_itemkey);
            content = (RecyclerView) view.findViewById(R.id.intelligent_itemcontent);
            content.setLayoutManager(new LinearLayoutManager(context));
        }
    }

}

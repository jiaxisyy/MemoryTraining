package com.example.hekd.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hekd on 2017/8/30.
 */

public class SelectAdapter extends RecyclerView.Adapter {
    private static final int DEFAULTSIZE = 4;
    private Context context;
    private List<Integer> numList;
    private OnMyItemClickListener onMyItemClickListener;
    private int isClick;

    public void setOnMyItemClickListener(OnMyItemClickListener onMyItemClickListener) {
        this.onMyItemClickListener = onMyItemClickListener;
    }

    public SelectAdapter(Context context, List<Integer> numList, int isClick) {
        this.context = context;
        this.numList = numList;
        this.isClick = isClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_select, parent, false);
        return new MyViewHolder(view, onMyItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (numList.size() != 0) {
            if (isClick == 0) {//没有点击,默认状态
                myViewHolder.tvItemNum.setText(numList.get(position).toString());
            } else if (isClick == 1) {// 正确
                myViewHolder.tvItemNum.setVisibility(View.INVISIBLE);
                myViewHolder.imageView.setVisibility(View.VISIBLE);
                myViewHolder.imageView.setImageResource(R.drawable.iv_btn_right);
            } else if (isClick == -1) {//错误
                myViewHolder.tvItemNum.setVisibility(View.INVISIBLE);
                myViewHolder.imageView.setVisibility(View.VISIBLE);
                myViewHolder.imageView.setImageResource(R.drawable.iv_btn_error);
            }
        }
    }

    public void afterClick(int isClick) {
        this.isClick = isClick;
        notifyDataSetChanged();
    }


    private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvItemNum;
        ImageView imageView;
        private OnMyItemClickListener onMyItemClickListener;

        public MyViewHolder(View itemView, OnMyItemClickListener onMyItemClickListener) {
            super(itemView);
            this.onMyItemClickListener = onMyItemClickListener;
            itemView.setOnClickListener(this);
            tvItemNum = (TextView) itemView.findViewById(R.id.tv_item_Num1);
            imageView = (ImageView) itemView.findViewById(R.id.iv_item_rightOrError1);
        }

        @Override
        public void onClick(View v) {
            if (onMyItemClickListener != null) {
                onMyItemClickListener.onItemClick(v, getPosition());

            }
        }
    }

    @Override
    public int getItemCount() {
        return DEFAULTSIZE;//返回默认
    }

    interface OnMyItemClickListener {

        void onItemClick(View view, int position);
    }
}

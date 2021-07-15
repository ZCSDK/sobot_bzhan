package com.sobot.chat.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sobot.chat.utils.ResourceUtils;

import java.util.List;

public abstract class SobotBaseGvAdapter<T> extends SobotBaseAdapter {
    protected LayoutInflater mInflater;


    public SobotBaseGvAdapter(Context context, List<T> list) {
        super(context, list);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        BaseViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(ResourceUtils
                    .getResLayoutId(context, getContentLayoutName()), null);
            holder = getViewHolder(context,convertView);
            convertView.setTag(holder);
        } else {
            holder = (BaseViewHolder) convertView.getTag();
        }
        holder.bindData(list.get(position), position);
        return convertView;
    }

    public abstract static class BaseViewHolder<T> {
        public BaseViewHolder(Context context, View view) {

        }

        public abstract void bindData(T data, int position);
    }

    /**
     * 获取item的布局
     *
     * @return
     */
    protected abstract String getContentLayoutName();

    /**
     * 获取Viewholder
     *
     * @return
     */
    protected abstract BaseViewHolder getViewHolder(Context context, View view);
}
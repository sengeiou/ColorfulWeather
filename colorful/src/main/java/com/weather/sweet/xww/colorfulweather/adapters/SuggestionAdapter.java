package com.weather.sweet.xww.colorfulweather.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weather.sweet.xww.colorfulweather.R;
import com.weather.sweet.xww.colorfulweather.entity.SuggestionEntity;

import java.util.List;

/**
 * 功能：
 *
 * @author : xww
 * @created at : 2019/4/5
 * @time : 14:54
 */
public class SuggestionAdapter extends BaseQuickAdapter<SuggestionEntity, BaseViewHolder> {

    public SuggestionAdapter(int layoutResId, @Nullable List<SuggestionEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SuggestionEntity item) {
        final AppCompatImageView icon = helper.getView(R.id.imgv_life_index_icon);
        final AppCompatTextView type = helper.getView(R.id.tv_life_index_type);
        final AppCompatTextView index = helper.getView(R.id.tv_life_index_value);
        final AppCompatTextView content = helper.getView(R.id.tv_life_index_content);

        icon.setImageDrawable(item.getIcon());
        type.setText(item.getSugType());
        index.setText(item.getSugIndex());
        content.setText(item.getSugContent());
    }
}
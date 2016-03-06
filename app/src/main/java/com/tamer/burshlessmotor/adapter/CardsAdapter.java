package com.tamer.burshlessmotor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.tamer.burshlessmotor.R;
import com.tamer.burshlessmotor.bean.Card;

import java.util.List;

/**
 * Created by liangzr on 16-3-6.
 */
public class CardsAdapter extends ArrayAdapter<Card> {

    private int resourceId;

    public CardsAdapter(Context context, int textViewResourceId, List<Card> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Card card = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.controlLayout = (LinearLayout) view.findViewById(R.id.list_item_speed_control);
            viewHolder.switchLayout = (LinearLayout) view.findViewById(R.id.list_item_switch);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (card.getType() == Card.CONTROL_BAR) {
            viewHolder.controlLayout.setVisibility(View.VISIBLE);
            viewHolder.switchLayout.setVisibility(View.VISIBLE);
        } else if (card.getType() == Card.SWITCH) {
            viewHolder.switchLayout.setVisibility(View.VISIBLE);
            viewHolder.controlLayout.setVisibility(View.VISIBLE);
        }
        Log.d("TAG", "添加" + card.getType());
        return view;
    }

    class ViewHolder {
        LinearLayout controlLayout;

        LinearLayout switchLayout;
    }
}

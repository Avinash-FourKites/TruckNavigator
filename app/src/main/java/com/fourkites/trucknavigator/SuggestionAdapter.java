package com.fourkites.trucknavigator;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Avinash on 19/04/16.
 */
public class SuggestionAdapter extends ArrayAdapter<Suggestion> {

    private final Context context;
    private final int layoutResourceId;
    private final ArrayList<Suggestion> data;


    public SuggestionAdapter(Context context, int layoutResourceId, ArrayList<Suggestion> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SuggesstionHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SuggesstionHolder();
            holder.suggesstionText = (TextView) row.findViewById(R.id.address);
            holder.suggesstionText.setTextColor(context.getResources().getColor(android.R.color.black));

            row.setTag(holder);
        } else {
            holder = (SuggesstionHolder) row.getTag();
        }
        holder.suggesstionText.setText(data.get(position).getName());
        return row;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Suggestion getItem(int position) {
        return super.getItem(position);
    }

    static class SuggesstionHolder {
        TextView suggesstionText;
    }
}

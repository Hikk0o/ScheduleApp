package com.hikko.scheduleapp.adapters;

import android.content.ClipData.Item;
import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.hikko.scheduleapp.R;

import java.util.List;
import java.util.Map;

public class ListAdapter extends SimpleAdapter {

    private int resourceLayout;
    private Context mContext;

    public ListAdapter(Context context, List<? extends Map<String, ?>> data,
                       @LayoutRes int resource, String[] from, @IdRes int[] to) {
        super(context, data, resource, from, to);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Spinner spinner = v.findViewById(R.id.activity_type_spinner);
        ArrayAdapter<CharSequence> s_adapter = ArrayAdapter.createFromResource(v.getContext(),
                R.array.activityType, R.layout.spinner_item);
        s_adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner.setAdapter(s_adapter);

        TextView activityNameAutoCompleteText = v.findViewById(R.id.ActivityNameAutoCompleteText);
        int maxLength = v.getResources().getInteger(R.integer.max_ActivityNameAutoCompleteText);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        activityNameAutoCompleteText.setFilters(fArray);

        return v;
    }

}

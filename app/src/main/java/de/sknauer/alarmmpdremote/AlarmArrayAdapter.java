package de.sknauer.alarmmpdremote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.sknauer.alarmmpdremote.model.Alarm;

public class AlarmArrayAdapter extends ArrayAdapter<Alarm> {

    private final Context context;
    private final ArrayList<Alarm> alarms;

    public AlarmArrayAdapter(Context context, ArrayList<Alarm> alarms) {
        super(context, R.layout.rowlayout,alarms );
        this.context = context;
        this.alarms=alarms;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Alarm alarm = alarms.get(position);
        View rowView;





        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        } else {
            rowView = convertView;
        }
        TextView tv_time = (TextView) rowView.findViewById(R.id.tv_time);
        if(alarm.getMinute()<10)
            tv_time.setText(alarm.getHour()+":0"+alarm.getMinute());
        else
            tv_time.setText(alarm.getHour()+":"+alarm.getMinute());






        return rowView;
    }


}

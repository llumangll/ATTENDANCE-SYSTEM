package com.nirma.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class SessionAdapter extends ArrayAdapter<ClassSession> {

    public SessionAdapter(Context context, List<ClassSession> sessions) {
        super(context, 0, sessions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Get the data item for this position
        ClassSession session = getItem(position);

        // 2. Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_class_card, parent, false);
        }

        // 3. Lookup view for data population
        TextView tvSubject = convertView.findViewById(R.id.tvSubject);
        TextView tvProf = convertView.findViewById(R.id.tvProf);

        // 4. Populate the data into the template view using the data object
        tvSubject.setText(session.getSubject());
        tvProf.setText("👨‍🏫 " + session.getProfessorName());

        // 5. Return the completed view to render on screen
        return convertView;
    }
}
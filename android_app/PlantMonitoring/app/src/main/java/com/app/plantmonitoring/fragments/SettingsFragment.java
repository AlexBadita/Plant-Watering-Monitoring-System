package com.app.plantmonitoring.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.plantmonitoring.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refDelays = database.getReference().child("Delays");

    private final List<String> timeUnits = Arrays.asList("seconds", "minutes", "hours", "days");

    private TextInputEditText measureValue;
    private AutoCompleteTextView measureUnit;
    private TextInputEditText pumpTime;
    private TextInputEditText threshold;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        measureValue = view.findViewById(R.id.measureValue);
        measureUnit = view.findViewById(R.id.measureUnit);
        pumpTime = view.findViewById(R.id.pumpTime);
        threshold = view.findViewById(R.id.threshold);
        Button save = view.findViewById(R.id.save);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, timeUnits);
        measureUnit.setAdapter(unitAdapter);
        // the minimum number of characters the user has to type in the edit box before the drop down list is shown
        measureUnit.setThreshold(0);

        getData();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        return view;
    }

    private void saveData(){
        String measure = measureValue.getText().toString();
        String pump = pumpTime.getText().toString();
        String thresh = threshold.getText().toString();
        String unit = measureUnit.getText().toString();
        try {
            if((unit.equals("days") && Integer.parseInt(measure) > 7) || (unit.equals("hours") && Integer.parseInt(measure) > 168) ||
                    (unit.equals("minutes") && Integer.parseInt(measure) > 10080) || (unit.equals("seconds") && Integer.parseInt(measure) > 604800)){
                Toast.makeText(view.getContext(), "Please select a measure value of max 7 days", Toast.LENGTH_LONG).show();
            }
            else if(Integer.parseInt(measure) == 0 || Integer.parseInt(pump) == 0){
                Toast.makeText(view.getContext(), "Please don't enter a value of 0 for delay or pump", Toast.LENGTH_LONG).show();
            }
            else if(Integer.parseInt(thresh) == 0 || Integer.parseInt(thresh) >= 100){
                Toast.makeText(view.getContext(), "Please select a threshold between 0 and 100", Toast.LENGTH_LONG).show();
            }
            else {
                setData();
                Toast.makeText(view.getContext(), "Data saved", Toast.LENGTH_LONG).show();
                getData();
            }
        } catch (Exception e){
            Toast.makeText(view.getContext(), "Please leave no empty field", Toast.LENGTH_LONG).show();
        }
    }

    private void getData() {
        refDelays.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Object> values = new ArrayList<>();
                for(DataSnapshot value : snapshot.getChildren()){
                    values.add(value.getValue());
                }
                measureValue.setText(values.get(1).toString());
                pumpTime.setText(values.get(2).toString());
                threshold.setText(values.get(3).toString());
                measureUnit.setHint(values.get(4).toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(view.getContext(), "Retrieving data failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setData() {
        refDelays.child("Change").setValue(true);
        refDelays.child("Measure").setValue(Long.parseLong(Objects.requireNonNull(measureValue.getText()).toString()));
        refDelays.child("Pump").setValue(Integer.parseInt(Objects.requireNonNull(pumpTime.getText()).toString()));
        refDelays.child("Threshold").setValue(Integer.parseInt(Objects.requireNonNull(threshold.getText()).toString()));
        refDelays.child("Unit").setValue(measureUnit.getText().toString());
    }
}
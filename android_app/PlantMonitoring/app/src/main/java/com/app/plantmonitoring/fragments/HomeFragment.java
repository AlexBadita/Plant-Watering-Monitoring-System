package com.app.plantmonitoring.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.plantmonitoring.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refValues = database.getReference().child("Values");

    private TextView humidity;
    private TextView temperature;
    private TextView light;
    private TextView moisture;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);

        humidity = view.findViewById(R.id.humidity);
        temperature = view.findViewById(R.id.temperature);
        light = view.findViewById(R.id.light);
        moisture = view.findViewById(R.id.moisture);

        getData();

        return view;
    }

    private void getData(){
        refValues.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> values = new ArrayList<>();
                for(DataSnapshot value : snapshot.getChildren()){
                    values.add(value.getValue(String.class));
                }
                humidity.setText(values.get(0));
                light.setText(values.get(1));
                moisture.setText(values.get(2));
                temperature.setText(values.get(3));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(view.getContext(), "Retrieving data failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
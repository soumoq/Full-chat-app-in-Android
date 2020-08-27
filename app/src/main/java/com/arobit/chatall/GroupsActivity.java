package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.*;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listGroup = new ArrayList<>();

    private DatabaseReference groupRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);


        init();
        displayGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String groupName = adapterView.getItemAtPosition(position).toString();
                Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });


    }

    private void init() {
        try {
            groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
            groupRef.keepSynced(true);
            listView = findViewById(R.id.list_view_g);
            arrayAdapter = new ArrayAdapter<String>(GroupsActivity.this, R.layout.support_simple_spinner_dropdown_item, listGroup);
            listView.setAdapter(arrayAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
        }

    }

    public void displayGroup() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();

                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }

                listGroup.clear();
                listGroup.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
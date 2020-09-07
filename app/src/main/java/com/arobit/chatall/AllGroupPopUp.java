package com.arobit.chatall;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
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

public class AllGroupPopUp extends AppCompatDialogFragment {
    private DatabaseReference groupRef;
    private FirebaseAuth auth;
    private ListView listView;
    private ArrayList<String> listGroup = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.CustomDialog);
        final Dialog builder = new Dialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.all_group_layout, null);
        listView = view.findViewById(R.id.list_view_g);
        init();


        displayGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String groupName = adapterView.getItemAtPosition(position).toString();
                final String uId = auth.getUid();

                DatabaseReference rootRef;
                rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userInfo = rootRef.child("NewUsers").child(uId);
                userInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String phoneNo = snapshot.child("phone").getValue().toString();
                        //Toast.makeText(getContext(),groupName + " " + uId + " " + phoneNo,Toast.LENGTH_LONG).show();

                        HashMap<String, Object> addGroup = new HashMap<>();
                        addGroup.put(uId, phoneNo);
                        groupRef.child(groupName).updateChildren(addGroup);
                        builder.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Already in this group", Toast.LENGTH_LONG).show();
                    }
                });


            }
        });

        builder.setContentView(view);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return builder;
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        groupRef = FirebaseDatabase.getInstance().getReference().child("new_group");
        arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, listGroup);
        listView.setAdapter(arrayAdapter);
        groupRef.keepSynced(true);
    }

    private void displayGroup() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Set<String> set = new HashSet<String>();
                set.clear();
                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                listGroup.addAll(set);
                arrayAdapter.notifyDataSetChanged();

                // Toast.makeText(getContext(),set.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

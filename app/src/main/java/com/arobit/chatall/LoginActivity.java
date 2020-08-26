package com.arobit.chatall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth auth;
    private EditText phone, name;
    private Button sendOtp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            init();

            sendOtp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String phoneNo = phone.getText().toString();
                        String fullName = name.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                        intent.putExtra("phone", "+91" + phoneNo);
                        intent.putExtra("name", fullName);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "2 Error: " + e, Toast.LENGTH_LONG).show();
                    }

                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "1 Error: " + e, Toast.LENGTH_LONG).show();
        }

    }

    private void init() {
        phone = findViewById(R.id.phone_no);
        name = findViewById(R.id.full_name);
        sendOtp = findViewById(R.id.send_otp);
        auth = FirebaseAuth.getInstance();

    }
    
}
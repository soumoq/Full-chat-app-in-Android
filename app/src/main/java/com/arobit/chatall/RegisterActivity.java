package com.arobit.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private String phone, name;
    private EditText otp;
    private Button verify;
    private FirebaseAuth mAuth;
    private String verificationId;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);

            init();

            sendOtp(phone);


            verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String otp1 = otp.getText().toString();
                    if (TextUtils.isEmpty(otp1) && otp1.length() < 6) {
                        Toast.makeText(getApplicationContext(), "Enter 6 digit otp", Toast.LENGTH_LONG).show();
                    } else
                        verifyCode(otp1);
                }
            });


        } catch (Exception e) {
            Toast.makeText(this, "1 Error: " + e, Toast.LENGTH_LONG).show();
        }
    }


    private void init() {
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        name = intent.getStringExtra("name");
        otp = findViewById(R.id.otp);
        verify = findViewById(R.id.verify);
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;

        }


        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otp.setText(code);
                Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    rootRef.child("NewUsers").child(currentUserId).setValue("");

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
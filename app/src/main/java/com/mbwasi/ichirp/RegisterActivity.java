package com.mbwasi.ichirp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    private static final String TAG = "RegisterActivity";
    Button registerBtn;
    EditText usernameET, emailET, passwordET, password2ET;
    FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    public ProgressBar progressBar;

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressBar = findViewById(R.id.progressBar);
        registerBtn = findViewById(R.id.registerButton);
        usernameET = findViewById(R.id.usernameET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        password2ET = findViewById(R.id.password2ET);
    }

    public void registerClicked(View view) {

        if (!validateForm()) {
            return;
        }

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        createAccount(email, password);
    }

    public boolean validateForm() {
        boolean valid = true;
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String password2 = password2ET.getText().toString();
        String username = usernameET.getText().toString();

        if (TextUtils.isEmpty(email)) {
            valid = false;
            showToastError("Email required.");
        }
        if (TextUtils.isEmpty(password)) {
            valid = false;
            showToastError("Password required.");
        }

        if (TextUtils.isEmpty(password2)) {
            valid = false;
            showToastError("Repeat password required.");
        }
        if (TextUtils.isEmpty(username)) {
            valid = false;
            showToastError("Username required.");
        }
        if (password.length() < 8) {
            valid = false;
            showToastError("Password must be at least 8 letters.");
        }
        if (!password.equals(password2)) {
            valid = false;
            showToastError("Passwords do not match.");
        }
        return valid;
    }

    public void showToastError(String msg) {
        Toast.makeText(RegisterActivity.this, msg,
                Toast.LENGTH_LONG).show();
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);

        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            //  user = mAuth.getCurrentUser();
                            createUserinDB();
                            sendEmailVerification();

                            Toast.makeText(RegisterActivity.this, "Authenticated!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //TODO: Replace with Toast libary 'com.github.GrenderG:Toasty:1.4.2'
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressBar();
                    }
                });
    }

    private void createUserinDB() {
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("id", user.getUid());
        dataMap.put("email", emailET.getText().toString()); //TODO: Implement field re-store state
        dataMap.put("imageURL", "default");
        dataMap.put("username", usernameET.getText().toString().toLowerCase());
        dataMap.put("status", "offline");

        dbRef.setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //TODO: Remove register activity from backstack
                    // Toast.makeText(RegisterActivity.this, "Email Verified, loggin you in.", Toast.LENGTH_LONG).show();

                } else {
                    //TODO: What to do if save is unsuccessful?? Have logic inside app to re-try creation of user data
                    Log.e(TAG, "onComplete: Saving to DB failed");
                    Toast.makeText(RegisterActivity.this, "DB save failed.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendEmailVerification() {
        user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.d(TAG, "onAuthStateChanged");

        if (user != null) {
            Task usertask = mAuth.getCurrentUser().reload();
            usertask.addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    user = mAuth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        Log.d(TAG, "Email verified");
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "onSuccess: Email not verified.");
                    }
                }
            });
        } else {
            Log.d(TAG, "onAuthStateChanged: User is null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
        user = mAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }
}
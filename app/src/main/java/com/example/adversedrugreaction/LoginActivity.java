package com.example.adversedrugreaction;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private boolean acceptedPermission = true;
    private EditText username;
    private EditText password;
    private TelephonyManager telephonyManager;
    private FirebaseFirestore db;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            acceptedPermission = false;
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        } else {
            assert telephonyManager != null;
            Log.d("Android", "Android ID : " + telephonyManager.getImei());
        }

        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submitLogin);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (acceptedPermission) validate();
                else {
                    final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle("Insufficient Permissions");
                    alertDialog.setMessage("You have not accepted sufficient permissions. Please restart the app");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                            LoginActivity.this.finish();
                        }
                    });
                }
            }
        });

    }

    private void validate() {
        submit.setText(R.string.authenticating);
        submit.setEnabled(false);

        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();
        Log.d("username", usernameText);

        db.collection("Users")
                .whereEqualTo("Name", usernameText)
                .whereEqualTo("Password", passwordText)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                try {
                                    Log.d("DOCUMENT-ID", document.getId());
                                    addLoginDetails(document.getId());
                                } catch (Exception ignored) {
                                }
                            }
                            if (task.getResult().isEmpty()) {
                                submit.setText(R.string.login);
                                submit.setEnabled(true);
                                final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                                alertDialog.setTitle("Error Fetching Data");
                                alertDialog.setMessage("Invalid Credentials");
                                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.cancel();
                                    }
                                });
                                alertDialog.show();
                            }
                        } else {
                            submit.setText(R.string.login);
                            submit.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Failed to Login", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                submit.setText(R.string.login);
                submit.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Failed to Login", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void addLoginDetails(String documentId) throws SecurityException {

        final Map<String, Object> loginData = new HashMap<>();
        loginData.put(telephonyManager.getImei(), new Timestamp((new Date()).getTime()));
        Log.d("Android", "Android ID : " + telephonyManager.getImei());

        db.collection("Users")
            .document(documentId)
            .update("LoginDetails", FieldValue.arrayUnion(loginData))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        submit.setText(R.string.login);
                        submit.setEnabled(true);
                        Log.d("LOGIN", "Success");
                        Intent intent = new Intent(LoginActivity.this, EditDrugs.class);
                        intent.putExtra("Username", username.getText().toString());
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                submit.setText(R.string.login);
                submit.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Failed to Login", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                acceptedPermission = true;
            }
        }
    }

}

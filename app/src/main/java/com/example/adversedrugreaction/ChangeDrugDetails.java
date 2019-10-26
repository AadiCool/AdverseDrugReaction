package com.example.adversedrugreaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeDrugDetails extends AppCompatActivity {

    private TextView drugName;
    private EditText genericName;
    private EditText description;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_drug_details);

        db = FirebaseFirestore.getInstance();

        drugName = findViewById(R.id.getDrugName);
        genericName = findViewById(R.id.getGenericName);
        description = findViewById(R.id.getDescriptionText);

        drugName.setText(Objects.requireNonNull(getIntent().getExtras()).getString("drugName"));

        if(Objects.requireNonNull(getIntent().getExtras()).getBoolean("hasData")){
            genericName.setText(getIntent().getExtras().getString("genericName"));
            description.setText(getIntent().getExtras().getString("description"));
            setListner();
        }

        (findViewById(R.id.cancelDrug)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        (findViewById(R.id.submitDrug)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataonDatabase();
            }
        });
    }

    public void setListner(){
        db.collection("Drugs").document(drugName.getText().toString())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d( "Listen failed.", Objects.requireNonNull(e.getLocalizedMessage()));
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            genericName.setText(snapshot.getString("Generic Name"));
                            description.setText(snapshot.getString("Description"));
                            Log.d("Firebase", "Details updated");
                        } else {
                            Log.d("Firebase","Current data: null");
                        }
                    }
                });
    }

    private void setDataonDatabase(){
        Map<String, Object> data = new HashMap<>();
        data.put("Description", description.getText().toString().trim());
        data.put("Generic Name", genericName.getText().toString().trim());
        db.collection("Drugs")
                .document(drugName.getText().toString().trim())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Data Firebase", "Submitted");
                        Intent intent = new Intent(ChangeDrugDetails.this, AdrEditableActivity.class);
                        intent.putExtra("drugName", drugName.getText().toString());
                        intent.putExtra("genericName", genericName.getText().toString());
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase Error", Objects.requireNonNull(e.getLocalizedMessage()));
                        Toast.makeText(ChangeDrugDetails.this, "Sorry, we cannot change", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}

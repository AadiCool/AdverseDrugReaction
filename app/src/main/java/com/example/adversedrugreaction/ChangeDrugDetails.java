package com.example.adversedrugreaction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeDrugDetails extends AppCompatActivity {

    private TextView drugName;
    private EditText genericName;
    private EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_drug_details);

        drugName = findViewById(R.id.getDrugName);
        genericName = findViewById(R.id.getGenericName);
        description = findViewById(R.id.getDescriptionText);

        if(Objects.requireNonNull(getIntent().getExtras()).getBoolean("hasData")){
            genericName.setText(getIntent().getExtras().getString("genericName"));
            description.setText(getIntent().getExtras().getString("description"));
        }
        drugName.setText(getIntent().getExtras().getString("drugName"));

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

    private void setDataonDatabase(){
        Map<String, Object> data = new HashMap<>();
        data.put("Description", description.getText().toString().trim());
        data.put("Generic Name", genericName.getText().toString().trim());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Drugs")
                .document(drugName.getText().toString().trim())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Data Firebase", "Submitted");
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

package com.example.adversedrugreaction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class AdrEditableActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adr_editable);

        final String drugName = Objects.requireNonNull(getIntent().getExtras()).getString("drugName");

        progressBar = findViewById(R.id.deleteAdrProgress);

        TextView textView = findViewById(R.id.setDrugName);
        textView.setText(drugName);
        TextView textView1 = findViewById(R.id.setGenericName);
        textView1.setText(getIntent().getExtras().getString("genericName"));

        RecyclerView adrlistEditable = findViewById(R.id.setadrlistEditable);
        adrlistEditable.setAdapter(new AdrAdapter(AdrEditableActivity.this, drugName, progressBar));
        adrlistEditable.setLayoutManager(new LinearLayoutManager(this));

        (findViewById(R.id.addNewAdr)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView(drugName);
            }
        });

    }

    private void dialogView(final String drugName){
        final AlertDialog.Builder builder = new AlertDialog.Builder(AdrEditableActivity.this);
        LayoutInflater li = LayoutInflater.from(AdrEditableActivity.this);
        final View promptsView = li.inflate(R.layout.prompt2, null, false);
        builder.setView(promptsView);
        builder.setTitle("ADR name");
        final EditText input = promptsView.findViewById(R.id.getAdrNameText);
        final Spinner spinner = promptsView.findViewById(R.id.adrTypeDropdown);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(this.getResources().getString(R.string.more_common_adrs));
        arrayList.add(this.getResources().getString(R.string.less_common_adrs));
        arrayList.add(this.getResources().getString(R.string.rare_found_adrs));
        SpinnerAdapter dataAdapter = new SpinnerAdapter(this, android.R.layout.simple_spinner_item, arrayList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                progressBar.setVisibility(View.VISIBLE);
                String adrName = input.getText().toString();
                String spinnerSelection = String.valueOf(spinner.getSelectedItem());
                String fieldName="";
                if (spinnerSelection.equals(getResources().getString(R.string.less_common_adrs)))
                    fieldName = "Less common";
                else if (spinnerSelection.equals(getResources().getString(R.string.more_common_adrs)))
                    fieldName = "More common";
                else if (spinnerSelection.equals(getResources().getString(R.string.rare_found_adrs)))
                    fieldName = "Rare";
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Drugs").document(drugName)
                        .update(fieldName, FieldValue.arrayUnion(adrName))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase", "ADR added");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase", "ADR not added");
                            }
                        });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}

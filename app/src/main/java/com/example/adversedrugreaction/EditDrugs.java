package com.example.adversedrugreaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class EditDrugs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drugs);

        TextView textView = findViewById(R.id.loggedinuser);
        textView.setText(String.format("Logged in as %s", Objects.requireNonNull(getIntent().getExtras()).getString("Username")));

        RecyclerView adrlistEditable = findViewById(R.id.ADRlistEditable);
        DrugAdapter drugAdapter = new DrugAdapter(EditDrugs.this);
        adrlistEditable.setAdapter(drugAdapter);
        adrlistEditable.setLayoutManager(new LinearLayoutManager(this));


        (findViewById(R.id.addDrug))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(EditDrugs.this);
                        LayoutInflater li = LayoutInflater.from(EditDrugs.this);
                        View promptsView = li.inflate(R.layout.prompt1, null, false);
                        builder.setView(promptsView);
                        builder.setTitle("Drug Name");
                        final EditText input = promptsView.findViewById(R.id.getDrugNameText);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(EditDrugs.this, ChangeDrugDetails.class);
                                intent.putExtra("hasData", false);
                                intent.putExtra("drugName", input.getText().toString());
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
    }
}

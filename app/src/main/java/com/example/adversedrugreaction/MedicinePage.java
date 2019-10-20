package com.example.adversedrugreaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Objects;

public class MedicinePage extends AppCompatActivity {

    private TextView genericName;
    private TextView description;
    private Button moreCommon;
    private Button lessCommon;
    private Button rare;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_page);

        final String medicineOption = (String) Objects.requireNonNull(getIntent().getExtras()).getString("medicine");
        assert medicineOption != null;
        Log.e("MEDICINE", medicineOption);

        TextView medicineName = (TextView) findViewById(R.id.medicineName);
        medicineName.setText(medicineOption.toUpperCase());
        genericName = (TextView) findViewById(R.id.genericName);
        description = (TextView) findViewById(R.id.descriptionText);

        moreCommon = (Button) findViewById(R.id.moreCommon);
        moreCommon.setEnabled(false);
        moreCommon.setText(R.string.pleaseWait);

        lessCommon = (Button) findViewById(R.id.lessCommon);
        lessCommon.setEnabled(false);
        lessCommon.setText(R.string.pleaseWait);

        rare = (Button) findViewById(R.id.rare);
        rare.setEnabled(false);
        rare.setText(R.string.pleaseWait);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Source source = Source.CACHE;

        db.collection("Drugs").document(medicineOption)
                .get(source).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressBar.setVisibility(View.GONE);

                moreCommon.setEnabled(true);
                moreCommon.setText(R.string.more_common_adrs);

                lessCommon.setEnabled(true);
                lessCommon.setText(R.string.less_common_adrs);

                rare.setEnabled(true);
                rare.setText(R.string.rare_found_adrs);

                Medicine medicine = new Medicine(Objects.requireNonNull(documentSnapshot.getData()));
                genericName.setText(medicine.getGenericName());
                description.setText(medicine.getDescription());

                setButtonListner(0, moreCommon, medicine,  medicineOption);
                setButtonListner(1, lessCommon, medicine,  medicineOption);
                setButtonListner(2, rare, medicine,  medicineOption);
            }
        });
    }

    private void setButtonListner(final int id, Button button, final Medicine medicine, final String  medicineOption){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicinePage.this, AdrShow.class);
                intent.putExtra("name", medicineOption);
                switch (id){
                    case 0 :
                        intent.putExtra("data", medicine.getMoreCommon());
                        intent.putExtra("heading", "More Common ADRs");
                        break;
                    case 1 :
                        intent.putExtra("data", medicine.getLessCommon());
                        intent.putExtra("heading", "Less Common ADRs");
                        break;
                    case 2 :
                        intent.putExtra("data", medicine.getRare());
                        intent.putExtra("heading", "Rare Found ADRs");
                        break;
                }
                startActivity(intent);
            }
        });
    }
}

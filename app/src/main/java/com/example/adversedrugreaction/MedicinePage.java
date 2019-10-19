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
    private ListView moreCommon;
    private ListView lessCommon;
    private ListView rare;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_page);

        String medicineOption = (String) Objects.requireNonNull(getIntent().getExtras()).getString("medicine");
        Log.e("MEDICINE", medicineOption);

        TextView medicineName = (TextView) findViewById(R.id.medicineName);
        assert medicineOption != null;
        medicineName.setText(medicineOption.toUpperCase());
        genericName = (TextView) findViewById(R.id.genericName);
        description = (TextView) findViewById(R.id.descriptionText);
        moreCommon = (ListView) findViewById(R.id.moreCommon);
        lessCommon = (ListView) findViewById(R.id.lessCommon);
        rare = (ListView) findViewById(R.id.rare);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Source source = Source.CACHE;

        db.collection("Drugs").document(medicineOption)
                .get(source).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressBar.setVisibility(View.GONE);
                Medicine medicine = new Medicine(Objects.requireNonNull(documentSnapshot.getData()));
                genericName.setText(medicine.getGenericName());
                description.setText(medicine.getDescription());
                SimpleAdapter simpleAdapter = new SimpleAdapter(MedicinePage.this, medicine.getLessCommon(),
                        R.layout.medicinelist, new String[]{"adr"}, new int[]{R.id.medicineListComponent});
                lessCommon.setAdapter(simpleAdapter);
                simpleAdapter = new SimpleAdapter(MedicinePage.this, medicine.getMoreCommon(),
                        R.layout.medicinelist, new String[]{"adr"}, new int[]{R.id.medicineListComponent});
                moreCommon.setAdapter(simpleAdapter);
                simpleAdapter = new SimpleAdapter(MedicinePage.this, medicine.getRare(),
                        R.layout.medicinelist, new String[]{"adr"}, new int[]{R.id.medicineListComponent});
                rare.setAdapter(simpleAdapter);

                setListListner(moreCommon);
                setListListner(lessCommon);
                setListListner(rare);
            }
        });
    }

    private void setListListner(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                Log.e("ITEM", Objects.requireNonNull(map.get("adr")));
                final AlertDialog alertDialog = new AlertDialog.Builder(MedicinePage.this).create();
                String source = "Search on Google";
                SpannableString spannableString = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    spannableString = setCustomFontTypeSpan(MedicinePage.this, source, source.length(), R.font.oswaldmedium);
                    alertDialog.setTitle(spannableString);
                } else
                    alertDialog.setTitle(source);

                source = "Google the following symptom:\n" + Objects.requireNonNull(map.get("adr"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    spannableString = setCustomFontTypeSpan(MedicinePage.this, source, source.length(), R.font.oswaldlight);
                    alertDialog.setMessage(spannableString);
                } else
                    alertDialog.setMessage(source);

                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, Objects.requireNonNull(map.get("adr")));
                        startActivity(intent);
                    }
                });
                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private SpannableString setCustomFontTypeSpan(Context context, String source, int endIndex, int font) {
        final SpannableString spannableString = new SpannableString(source);
        Typeface myTypeface = Typeface.create(ResourcesCompat.getFont(context, font), Typeface.NORMAL);
        spannableString.setSpan(new TypefaceSpan(myTypeface),
                0, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}

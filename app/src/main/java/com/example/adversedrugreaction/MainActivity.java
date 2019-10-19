package com.example.adversedrugreaction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> items2 = new ArrayList<>();
    private SpinnerDialog spinnerDialog;
    private Button selectMedicine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectMedicine = findViewById(R.id.selectMedicine);
        selectMedicine.setEnabled(false);
        selectMedicine.setText("Please Wait");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Source source = Source.CACHE;

        db.collection("Drugs")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        items2.add(document.getId());
                        items.add(document.getId().toUpperCase());
                        Log.d("Medicine", document.getId());
                    }
                    setListData();
                }
            }
        });
    }

    private void setListData(){
        spinnerDialog = new SpinnerDialog(MainActivity.this, items, "Select Medicine", R.style.DialogAnimations_SmileWindow, "Close");
        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                Toast.makeText(MainActivity.this, item + "  " + position+"", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this , MedicinePage.class);
                intent.putExtra("medicine", items2.get(position));
                startActivity(intent);
            }
        });
        selectMedicine.setEnabled(true);
        selectMedicine.setText("Select Medicine");
        selectMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });
    }
}

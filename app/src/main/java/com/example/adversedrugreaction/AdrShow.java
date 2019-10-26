package com.example.adversedrugreaction;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AdrShow extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adr_show);

        TextView textView = findViewById(R.id.ADRname);
        TextView name = findViewById(R.id.medicineADRname);
        listView = findViewById(R.id.ADRlist);

        ArrayList<HashMap<String, String>> listData = (ArrayList<HashMap<String, String>>) Objects.requireNonNull(getIntent().getExtras()).get("data");
        assert listData != null;

        String heading = (String) Objects.requireNonNull(getIntent().getExtras()).getString("heading");
        assert heading != null;

        String nameVal = (String) Objects.requireNonNull(getIntent().getExtras()).getString("name");
        assert nameVal != null;

        textView.setText(heading);
        name.setText(nameVal);

        SimpleAdapter simpleAdapter = new SimpleAdapter(AdrShow.this, listData,
                R.layout.medicinelist, new String[]{"adr"}, new int[]{R.id.medicineListComponent});
        listView.setAdapter(simpleAdapter);
        setListListner();

    }

    private void setListListner() {

        listView.setDivider(null);
        listView.setDividerHeight(10);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                Log.e("ITEM", Objects.requireNonNull(map.get("adr")));
                try {
                    final AlertDialog alertDialog = new AlertDialog.Builder(AdrShow.this).create();
                    alertDialog.setTitle("Search on Google");
                    alertDialog.setMessage("Google the following ADR:\n" + Objects.requireNonNull(map.get("adr")));
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
                } catch (Exception e){
                    Toast.makeText(AdrShow.this,"Sorry, Google Search is not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

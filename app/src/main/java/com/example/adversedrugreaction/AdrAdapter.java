package com.example.adversedrugreaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdrAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<AdrAdapter.ViewHolder> {

    private ArrayList<Map<String, String>> adrList;
    private Context context;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    String drugName = "";

    public AdrAdapter(Context context, String drugName, ProgressBar progressBar) {
        this.context = context;
        this.adrList = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.progressBar = progressBar;
        this.drugName = drugName;
        refreshList();
    }

    private void refreshList() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Drugs").document(drugName)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e != null) {
                            Log.d("Listen failed.", Objects.requireNonNull(e.getLocalizedMessage()));
                            return;
                        }
                        adrList.clear();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            try {
                                ArrayList<String> moreCommon = (ArrayList<String>) Objects.requireNonNull(documentSnapshot.get("More common"));
                                adrList.addAll(makeGroupuing(context.getResources().getString(R.string.more_common_adrs), moreCommon));
                            } catch (Exception ignored) {
                                Log.d("ADR", "No More Common ADR present");
                            }
                            try {
                                ArrayList<String> lessCommon = (ArrayList<String>) Objects.requireNonNull(documentSnapshot.get("Less common"));
                                adrList.addAll(makeGroupuing(context.getResources().getString(R.string.less_common_adrs), lessCommon));
                            } catch (Exception ignored) {
                                Log.d("ADR", "No Less Common ADR present");
                            }
                            try {
                                ArrayList<String> rare = (ArrayList<String>) Objects.requireNonNull(documentSnapshot.get("Rare"));
                                adrList.addAll(makeGroupuing(context.getResources().getString(R.string.rare_found_adrs), rare));
                            } catch (Exception ignored) {
                                Log.d("ADR", "No Rare Found ADR present");
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
    }

    private ArrayList<Map<String, String>> makeGroupuing(String grpName, ArrayList<String> data) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        for (String x : data) {
            Map<String, String> dataPoint = new HashMap<>();
            dataPoint.put("data", x);
            dataPoint.put("type", grpName);
            result.add(dataPoint);
        }
        return result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View adrEditableView = inflater.inflate(R.layout.adr_list_editable, parent, false);
        return new AdrAdapter.ViewHolder(adrEditableView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.adrNameEditable.setText(adrList.get(position).get("data"));
        holder.adrTypeEditable.setText(adrList.get(position).get("type"));
        holder.deleteADR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                removeFromDb(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String adrName = adrList.get(position).get("data");
                try {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Search on Google");
                    alertDialog.setMessage("Google the following ADR:\n" + adrName);
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                            intent.putExtra(SearchManager.QUERY, adrName);
                            context.startActivity(intent);
                        }
                    });
                    alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                } catch (Exception e) {
                    Toast.makeText(context, "Sorry, Google Search is not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void removeFromDb(int position) {
        String type = adrList.get(position).get("type");
        String data = adrList.get(position).get("data");
        String fieldName = "";
        assert data != null;
        Log.d("ADR name", data);
        DocumentReference documentReference = db.collection("Drugs").document(drugName);
        assert type != null;
        if (type.equals(context.getResources().getString(R.string.less_common_adrs)))
            fieldName = "Less common";
        else if (type.equals(context.getResources().getString(R.string.more_common_adrs)))
            fieldName = "More common";
        else if (type.equals(context.getResources().getString(R.string.rare_found_adrs)))
            fieldName = "Rare";
        documentReference.update(fieldName, FieldValue.arrayRemove(data))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Firebase", "ADR successfully deleted");
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "Error deleting ADR"+e.getLocalizedMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return adrList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView adrNameEditable;
        TextView adrTypeEditable;
        Button deleteADR;

        ViewHolder(View itemView) {
            super(itemView);
            this.adrNameEditable = itemView.findViewById(R.id.showAdrEditable);
            this.adrTypeEditable = itemView.findViewById(R.id.showAdrTypeEditable);
            this.deleteADR = itemView.findViewById(R.id.deleteAdr);
        }
    }

}

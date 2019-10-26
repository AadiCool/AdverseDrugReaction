package com.example.adversedrugreaction;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class DrugAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<DrugAdapter.ViewHolder> {

    private ArrayList<String> drugList;
    private ArrayList<Medicine> drugDetailsList;
    private FirebaseFirestore db;
    private Context context;
    private ProgressBar progressBar;

    DrugAdapter(Context context, ProgressBar progressBar) {
        this.context = context;
        this.drugList = new ArrayList<>();
        this.drugDetailsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        this.progressBar = progressBar;
        refreshList();
    }

    private void refreshList() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Drugs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e != null) {
                            Log.e("Listen failed.", Objects.requireNonNull(e.getLocalizedMessage()));
                            return;
                        }
                        drugList.clear();
                        assert queryDocumentSnapshots != null;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            drugList.add(doc.getId());
                            drugDetailsList.add(new Medicine(doc.getData()));
                        }
                        notifyDataSetChanged();
                    }
                });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View drugEditableView = inflater.inflate(R.layout.medicinelisteditable, parent, false);
        return new ViewHolder(drugEditableView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.drugNameEditable.setText(drugList.get(position));
        holder.deletedrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                removeFromDb(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Clicked", drugList.get(position));
                Intent intent = new Intent(context, ChangeDrugDetails.class);
                intent.putExtra("hasData", true);
                intent.putExtra("genericName",drugDetailsList.get(position).getGenericName());
                intent.putExtra("description",drugDetailsList.get(position).getDescription());
                intent.putExtra("drugName",drugList.get(position));
                context.startActivity(intent);
            }
        });
    }

    private void removeFromDb(int position) {
        Log.d("Firebase", "Starting to remove");
        String drugName = drugList.get(position);
        db.collection("Drugs").document(drugName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "DocumentSnapshot successfully deleted!");
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error deleting document", Objects.requireNonNull(e.getLocalizedMessage()));
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView drugNameEditable;
        Button deletedrug;

        ViewHolder(View itemView) {
            super(itemView);
            this.drugNameEditable = itemView.findViewById(R.id.showDrugEditable);
            this.deletedrug = itemView.findViewById(R.id.deleteDrug);
        }
    }
}

package com.example.adversedrugreaction;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private ArrayList<String> adrList;
    private ArrayList<Medicine> adrDetailsList;
    private FirebaseFirestore db;
    private Context context;

    DrugAdapter(Context context) {
        this.context = context;
        this.adrList = new ArrayList<>();
        this.adrDetailsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        refreshList();
    }

    private void refreshList() {
        db.collection("Drugs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Listen failed.", Objects.requireNonNull(e.getLocalizedMessage()));
                            return;
                        }
                        adrList.clear();
                        assert queryDocumentSnapshots != null;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            adrList.add(doc.getId());
                            adrDetailsList.add(new Medicine(doc.getData()));
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
        View adrEditableView = inflater.inflate(R.layout.medicinelisteditable, parent, false);
        return new ViewHolder(adrEditableView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.adrNameEditable.setText(adrList.get(position));
        holder.deleteADR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.deleteADR.setText(R.string.pleaseWait);
                holder.deleteADR.setEnabled(false);
                removeFromDb(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Clicked", adrList.get(position));
                Intent intent = new Intent(context, ChangeDrugDetails.class);
                intent.putExtra("hasData", true);
                intent.putExtra("genericName",adrDetailsList.get(position).getGenericName());
                intent.putExtra("description",adrDetailsList.get(position).getDescription());
                intent.putExtra("drugName",adrList.get(position));
                context.startActivity(intent);
            }
        });
    }

    private void removeFromDb(int position) {
        Log.d("Firebase", "Starting to remove");
        String adrName = adrList.get(position);
        db.collection("Drugs").document(adrName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error deleting document", Objects.requireNonNull(e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return adrList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView adrNameEditable;
        Button deleteADR;

        ViewHolder(View itemView) {
            super(itemView);
            this.adrNameEditable = itemView.findViewById(R.id.showADReditable);
            this.deleteADR = itemView.findViewById(R.id.deleteADR);
        }
    }
}

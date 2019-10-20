package com.example.adversedrugreaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.example.adversedrugreaction.R.string.scanBarcode;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> items2 = new ArrayList<>();
    private SpinnerDialog spinnerDialog;
    private Button selectMedicine;
    private Button scanBarcode;
    private Button chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectMedicine = findViewById(R.id.selectMedicine);
        selectMedicine.setEnabled(false);
        selectMedicine.setText(R.string.pleaseWait);

        chat = findViewById(R.id.chat);
        chat.setText(R.string.chat);
        chat.setEnabled(false);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        scanBarcode = findViewById(R.id.scanBarcode);
        scanBarcode.setEnabled(false);
        scanBarcode.setText(R.string.pleaseWait);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                Toast.makeText(MainActivity.this, item, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this , MedicinePage.class);
                intent.putExtra("medicine", items2.get(position));
                startActivity(intent);
            }
        });
        selectMedicine.setEnabled(true);
        selectMedicine.setText(R.string.selectMedicine);
        selectMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });

        scanBarcode.setEnabled(true);
        scanBarcode.setText(R.string.scanBarcode);
        scanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchpermission();
            }
        });

        chat.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult cropimage = CropImage.getActivityResult(data);
            assert cropimage != null;
            Uri imageUri = cropimage.getUri();
            scanBarcode(imageUri);
        }
    }

    private void barCodeNotFound(String msg){
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void fetchpermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        }
    }

    private void scanBarcode(Uri uri) {
        scanBarcode.setEnabled(false);
        scanBarcode.setText(R.string.processing);

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_CODE_128,
                                FirebaseVisionBarcode.FORMAT_EAN_13,
                                FirebaseVisionBarcode.FORMAT_EAN_8,
                                FirebaseVisionBarcode.FORMAT_CODE_39)
                        .build();
        Log.d("BARCODE", "Image Taken");
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector(options);
            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            for (FirebaseVisionBarcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                int valueType = barcode.getValueType();
                                Log.d("BARCODE", rawValue + " " + valueType);
                                findMedicine(rawValue);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            barCodeNotFound("Sorry, Barcode is not found");
                            scanBarcode.setEnabled(true);
                            scanBarcode.setText(R.string.scanBarcode);
                        }
                    });

        } catch (IOException e) {
            barCodeNotFound("Sorry, it can't be processed");
            scanBarcode.setEnabled(true);
            scanBarcode.setText(R.string.scanBarcode);
            e.printStackTrace();
        }
    }

    private void findMedicine(String medicine){
        scanBarcode.setEnabled(true);
        scanBarcode.setText(R.string.scanBarcode);
        try {
            String code = medicine.replaceAll("[^a-zA-Z]", "").toLowerCase();
            code = code.substring(0, 1).toUpperCase() + code.substring(1);
            Toast.makeText(MainActivity.this, code, Toast.LENGTH_SHORT).show();
            if(!items2.contains(code)) {
                barCodeNotFound("Sorry, we don't have this medicine");
                throw new Exception("Not Found");
            }
            Intent intent = new Intent(MainActivity.this , MedicinePage.class);
            intent.putExtra("medicine",code);
            startActivity(intent);
            Log.e("BARCODE", code);
        }catch (Exception e){
            barCodeNotFound("Sorry, Barcode is not found");
        }
    }
}

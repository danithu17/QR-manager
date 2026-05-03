package com.example.qrmanager.db;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FirebaseStorageHelper {
    private StorageReference mStorage;

    public FirebaseStorageHelper() {
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    public void uploadPdf(File file, String fileName, OnUploadListener listener) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference pdfRef = mStorage.child("reports/" + fileName);

        pdfRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e.getMessage());
                });
    }

    public interface OnUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }
}

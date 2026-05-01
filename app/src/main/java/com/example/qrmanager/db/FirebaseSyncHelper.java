package com.example.qrmanager.db;

import com.example.qrmanager.model.QRCode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseSyncHelper {
    private static final String DATABASE_PATH = "qr_inventory";
    private DatabaseReference mDatabase;

    public FirebaseSyncHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference(DATABASE_PATH);
    }

    public void syncToCloud(List<QRCode> qrCodes) {
        // Simple sync: overwrite the cloud node with the latest local data
        mDatabase.setValue(qrCodes)
                .addOnSuccessListener(aVoid -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    // Failure
                });
    }

    public void uploadBackup(List<QRCode> qrCodes) {
        // Upload a dated backup
        String timestamp = String.valueOf(System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("backups").child(timestamp).setValue(qrCodes);
    }
}

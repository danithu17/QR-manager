package com.example.qrmanager;

import android.content.Intent;
import android.net.Uri;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrmanager.adapter.QRCodeAdapter;
import com.example.qrmanager.db.AppDatabase;
import com.example.qrmanager.db.FirebaseSyncHelper;
import com.example.qrmanager.db.FirebaseStorageHelper;
import com.example.qrmanager.model.QRCode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QRCodeAdapter adapter;
    private AppDatabase db;
    private FirebaseSyncHelper firebaseSyncHelper;
    private FirebaseStorageHelper firebaseStorageHelper;
    private Uri selectedImageUri;

    private TextView tvTotalSold, tvPending, tvPreorders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        firebaseSyncHelper = new FirebaseSyncHelper();
        firebaseStorageHelper = new FirebaseStorageHelper();

        checkBiometricAuth();
        signInAnonymously();
        
        initViews();
        setupRecyclerView();
        setupBottomNav();
        observeData("All"); // Default filter
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTotalSold = findViewById(R.id.tvTotalSoldValue);
        tvPending = findViewById(R.id.tvPendingValue);
        tvPreorders = findViewById(R.id.tvPreorderValue);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        fabAdd.setOnClickListener(v -> showAddDialog());
        
        findViewById(R.id.tvTotalSoldValue).setOnClickListener(v -> generateSalesReport());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QRCodeAdapter();
        adapter.setOnItemActionListener(new QRCodeAdapter.OnItemActionListener() {
            @Override
            public void onSell(QRCode qrCode) {
                showSellDialog(qrCode, "Sold");
            }

            @Override
            public void onReserve(QRCode qrCode) {
                showSellDialog(qrCode, "Pre-ordered");
            }

            @Override
            public void onWhatsApp(QRCode qrCode) {
                String url = "https://api.whatsapp.com/send?phone=" + qrCode.getCustomerPhone() + 
                             "&text=Hello " + qrCode.getCustomerName() + ", your QR code " + qrCode.getName() + " is ready!";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

            @Override
            public void onExportPdf(QRCode qrCode) {
                exportSingleQRCodeAsPdf(qrCode);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void observeData(String filter) {
        // Observe list and Sync to Cloud
        db.qrCodeDao().getAllQRCodes().observe(this, qrCodes -> {
            if (qrCodes != null) {
                firebaseSyncHelper.syncToCloud(qrCodes);
            }
            // Apply filter to adapter
            if (filter.equals("All")) {
                adapter.setQrCodes(qrCodes);
            } else {
                db.qrCodeDao().getQRCodesByStatus(filter).observe(this, filteredCodes -> adapter.setQrCodes(filteredCodes));
            }
        });

        // Observe Stats
        db.qrCodeDao().getSoldCount().observe(this, count -> tvTotalSold.setText(String.valueOf(count)));
        db.qrCodeDao().getPendingCount().observe(this, count -> tvPending.setText(String.valueOf(count)));
        db.qrCodeDao().getPreorderCount().observe(this, count -> tvPreorders.setText(String.valueOf(count)));
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                observeData("All");
            } else if (id == R.id.nav_inventory) {
                observeData("Available");
            } else if (id == R.id.nav_preorders) {
                observeData("Pre-ordered");
            } else if (id == R.id.nav_sales) {
                observeData("Sold");
            }
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_qr, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText etName = dialogView.findViewById(R.id.etQRName);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        dialogView.findViewById(R.id.btnUploadImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String category = etCategory.getText().toString();
            String path = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            if (!name.isEmpty()) {
                QRCode qr = new QRCode(name, category, path, "Available", System.currentTimeMillis());
                insertQRCode(qr);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }

    private void signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(this, authResult -> {
                    // Signed in
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(MainActivity.this, "Cloud Sync Auth Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finish(); // Close app if auth fails
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authenticated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Secure Login")
                .setSubtitle("Use biometric to access QR Manager")
                .setNegativeButtonText("Exit")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void generateSalesReport() {
        db.qrCodeDao().getAllQRCodes().observe(this, qrCodes -> {
            if (qrCodes == null || qrCodes.isEmpty()) return;
            
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Header
            paint.setFakeBoldText(true);
            paint.setTextSize(24);
            paint.setColor(Color.BLUE);
            canvas.drawText("QR MANAGER - SALES REPORT", 150, 60, paint);

            paint.setFakeBoldText(false);
            paint.setTextSize(14);
            paint.setColor(Color.DKGRAY);
            canvas.drawText("Generated on: " + new java.util.Date().toString(), 50, 90, paint);

            // Table Header
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2f);
            canvas.drawLine(50, 110, 545, 110, paint);
            canvas.drawText("Item Name", 50, 130, paint);
            canvas.drawText("Customer Name", 200, 130, paint);
            canvas.drawText("Contact", 400, 130, paint);
            canvas.drawLine(50, 140, 545, 140, paint);

            int y = 160;
            paint.setTextSize(12);
            for (QRCode qr : qrCodes) {
                if (qr.getStatus().equals("Sold")) {
                    canvas.drawText(qr.getName(), 50, y, paint);
                    canvas.drawText(qr.getCustomerName(), 200, y, paint);
                    canvas.drawText(qr.getCustomerPhone(), 400, y, paint);
                    y += 25;
                    
                    if (y > 800) break; // Simple page limit check
                }
            }

            document.finishPage(page);
            String fileName = "SalesReport_" + System.currentTimeMillis() + ".pdf";
            File file = new File(getExternalFilesDir(null), fileName);
            try {
                document.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "Report saved locally", Toast.LENGTH_SHORT).show();
                
                // Upload to Firebase
                firebaseStorageHelper.uploadPdf(file, fileName, new FirebaseStorageHelper.OnUploadListener() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Toast.makeText(MainActivity.this, "Report uploaded to Cloud!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MainActivity.this, "Cloud upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            document.close();
        });
    }

    private void showSellDialog(QRCode qrCode, String newStatus) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sell_qr, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        ((TextView)dialogView.findViewById(R.id.tvDialogTitle)).setText(newStatus.equals("Sold") ? "Complete Sale" : "Reserve Code");
        EditText etName = dialogView.findViewById(R.id.etCustomerName);
        EditText etPhone = dialogView.findViewById(R.id.etCustomerPhone);

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            if (!name.isEmpty() && !phone.isEmpty()) {
                qrCode.setCustomerName(name);
                qrCode.setCustomerPhone(phone);
                qrCode.setStatus(newStatus);
                updateQRCode(qrCode);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Fill details", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void exportSingleQRCodeAsPdf(QRCode qrCode) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Background
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, 400, 600, paint);

        // Header
        paint.setColor(Color.BLUE);
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText(qrCode.getName(), 50, 50, paint);

        paint.setTextSize(14);
        paint.setColor(Color.DKGRAY);
        paint.setFakeBoldText(false);
        canvas.drawText("Category: " + qrCode.getCategory(), 50, 80, paint);
        canvas.drawText("Status: " + qrCode.getStatus(), 50, 100, paint);

        // Placeholder for QR Image (in a real app, you'd load the bitmap from qrCode.getImagePath())
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(50, 130, 350, 430, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        canvas.drawText("[ QR IMAGE PLACEHOLDER ]", 120, 280, paint);
        canvas.drawText("ID: " + qrCode.getId(), 50, 460, paint);

        document.finishPage(page);
        String fileName = "QR_" + qrCode.getName().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(getExternalFilesDir(null), fileName);
        
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Exporting PDF to Cloud...", Toast.LENGTH_SHORT).show();
            
            firebaseStorageHelper.uploadPdf(file, fileName, new FirebaseStorageHelper.OnUploadListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Toast.makeText(MainActivity.this, "QR PDF Uploaded Successfully!", Toast.LENGTH_LONG).show();
                    // Optionally share the link
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "PDF Upload Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();
    }

    private void insertQRCode(QRCode qr) {
        Executors.newSingleThreadExecutor().execute(() -> db.qrCodeDao().insert(qr));
    }

    private void updateQRCode(QRCode qr) {
        Executors.newSingleThreadExecutor().execute(() -> db.qrCodeDao().update(qr));
    }
}

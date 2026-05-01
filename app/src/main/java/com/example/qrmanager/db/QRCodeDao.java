package com.example.qrmanager.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qrmanager.model.QRCode;

import java.util.List;

@Dao
public interface QRCodeDao {
    @Insert
    void insert(QRCode qrCode);

    @Update
    void update(QRCode qrCode);

    @Query("SELECT * FROM qr_codes ORDER BY timestamp DESC")
    LiveData<List<QRCode>> getAllQRCodes();

    @Query("SELECT * FROM qr_codes WHERE status = :status ORDER BY timestamp DESC")
    LiveData<List<QRCode>> getQRCodesByStatus(String status);

    @Query("SELECT COUNT(*) FROM qr_codes WHERE status = 'Sold'")
    LiveData<Integer> getSoldCount();

    @Query("SELECT COUNT(*) FROM qr_codes WHERE status = 'Pending'")
    LiveData<Integer> getPendingCount();

    @Query("SELECT COUNT(*) FROM qr_codes WHERE status = 'Pre-ordered'")
    LiveData<Integer> getPreorderCount();
}

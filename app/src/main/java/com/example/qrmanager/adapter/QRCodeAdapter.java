package com.example.qrmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qrmanager.R;
import com.example.qrmanager.model.QRCode;

import java.util.ArrayList;
import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRViewHolder> {

    private List<QRCode> qrCodes = new ArrayList<>();
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onSell(QRCode qrCode);
        void onReserve(QRCode qrCode);
        void onWhatsApp(QRCode qrCode);
        void onExportPdf(QRCode qrCode);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setQrCodes(List<QRCode> qrCodes) {
        this.qrCodes = qrCodes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qr_code, parent, false);
        return new QRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRViewHolder holder, int position) {
        QRCode qrCode = qrCodes.get(position);
        holder.tvName.setText(qrCode.getName());
        String subtitle = qrCode.getCategory();
        if (qrCode.getCustomerName() != null && !qrCode.getCustomerName().isEmpty()) {
            subtitle += " • " + qrCode.getCustomerName();
        }
        holder.tvCategory.setText(subtitle);
        holder.tvStatus.setText(qrCode.getStatus().toUpperCase());

        // Glide for image loading
        Glide.with(holder.itemView.getContext())
                .load(qrCode.getImagePath())
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(holder.ivQR);

        holder.btnAction.setVisibility(qrCode.getStatus().equals("Available") ? View.VISIBLE : View.GONE);
        holder.btnReserve.setVisibility(qrCode.getStatus().equals("Available") ? View.VISIBLE : View.GONE);
        
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onSell(qrCode);
        });

        holder.btnReserve.setOnClickListener(v -> {
            if (listener != null) listener.onReserve(qrCode);
        });

        holder.btnWhatsApp.setVisibility(qrCode.getCustomerPhone() != null && !qrCode.getCustomerPhone().isEmpty() ? View.VISIBLE : View.GONE);
        holder.btnWhatsApp.setOnClickListener(v -> {
            if (listener != null) listener.onWhatsApp(qrCode);
        });

        holder.btnExportPdf.setOnClickListener(v -> {
            if (listener != null) listener.onExportPdf(qrCode);
        });

        // Simple Fade-in animation
        setFadeAnimation(holder.itemView);
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return qrCodes.size();
    }

    static class QRViewHolder extends RecyclerView.ViewHolder {
        ImageView ivQR;
        TextView tvName, tvCategory, tvStatus;
        Button btnAction, btnReserve;
        View btnWhatsApp, btnExportPdf;

        public QRViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQR = itemView.findViewById(R.id.ivQRCode);
            tvName = itemView.findViewById(R.id.tvQRName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnReserve = itemView.findViewById(R.id.btnReserve);
            btnWhatsApp = itemView.findViewById(R.id.btnWhatsApp);
            btnExportPdf = itemView.findViewById(R.id.btnExportPdf);
        }
    }
}

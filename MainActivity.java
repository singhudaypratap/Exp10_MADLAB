package com.example.audiovideo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvFileInfo;
    private ImageView ivPreview;
    private VideoView vvPreview;
    private static final long MAX_SIZE_MB = 5; // Example threshold

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    handleFile(uri, "image");
                }
            }
    );

    private final ActivityResultLauncher<String> pickVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    handleFile(uri, "video");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvFileInfo = findViewById(R.id.tvFileInfo);
        ivPreview = findViewById(R.id.ivPreview);
        vvPreview = findViewById(R.id.vvPreview);
        Button btnPickImage = findViewById(R.id.btnPickImage);
        Button btnPickVideo = findViewById(R.id.btnPickVideo);

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnPickVideo.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));
    }

    private void handleFile(Uri uri, String type) {
        long fileSize = getFileSize(uri);
        double fileSizeInMB = fileSize / (1024.0 * 1024.0);

        String info = String.format(Locale.getDefault(), "Type: %s\nSize: %.2f MB", type, fileSizeInMB);
        tvFileInfo.setText(info);

        if (fileSizeInMB > MAX_SIZE_MB) {
            Toast.makeText(this, "Large file detected! Handling accordingly.", Toast.LENGTH_LONG).show();
            // Implement special handling for large files here
        }

        if ("image".equals(type)) {
            ivPreview.setVisibility(View.VISIBLE);
            vvPreview.setVisibility(View.GONE);
            ivPreview.setImageURI(uri);
        } else {
            ivPreview.setVisibility(View.GONE);
            vvPreview.setVisibility(View.VISIBLE);
            vvPreview.setVideoURI(uri);
            vvPreview.start();
        }
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return 0;
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }
}

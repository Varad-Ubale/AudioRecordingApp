package com.vktech.audiorecorderapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioRecorderActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private MediaRecorder mediaRecorder;
    private String filePath = "";
    private TextView statusText;
    private Button recordBtn, stopBtn, goToListBtn;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        statusText = findViewById(R.id.statusText);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);
        goToListBtn = findViewById(R.id.goToListBtn);

        recordBtn.setOnClickListener(view -> {
            if (checkPermissions()) {
                startRecording();
            } else {
                requestPermissions();
            }
        });

        stopBtn.setOnClickListener(view -> stopRecording());

        goToListBtn.setOnClickListener(view -> {
            startActivity(new Intent(AudioRecorderActivity.this, AudioManagerActivity.class));
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE_PERMISSIONS);
    }

    private void startRecording() {
        if (isRecording) {
            Toast.makeText(this, "Already recording", Toast.LENGTH_SHORT).show();
            return;
        }

        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings");
        if (!dir.exists()) dir.mkdirs();

        filePath = dir.getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            statusText.setText("Recording started...");
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                statusText.setText("Recording Saved into the storage."); //+ filePath to show the file path
                Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error stopping recorder", Toast.LENGTH_SHORT).show();
            }
            mediaRecorder = null;
            isRecording = false;
        } else {
            Toast.makeText(this, "Not recording", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkPermissions()) {
                startRecording();
            } else {
                Toast.makeText(this, "Microphone permission is required to record", Toast.LENGTH_LONG).show();
            }
        }
    }
}

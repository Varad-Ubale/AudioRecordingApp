package com.vktech.audiorecorderapp;

import android.widget.EditText;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AudioManagerActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<File> recordings = new ArrayList<>();
    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_manager);

        listView = findViewById(R.id.audioListView);
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings");
        File[] files = dir.listFiles();

        ArrayList<String> names = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                recordings.add(f);
                names.add(f.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            File selected = recordings.get(position);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(selected.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Playing: " + selected.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            File selected = recordings.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Action");
            builder.setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    // Rename
                    final EditText input = new EditText(this);
                    input.setHint("Enter new file name");

                    new AlertDialog.Builder(this)
                            .setTitle("Rename File")
                            .setView(input)
                            .setPositiveButton("Rename", (d, w) -> {
                                String newName = input.getText().toString().trim();
                                if (!newName.isEmpty()) {
                                    if (!newName.endsWith(".3gp")) {
                                        newName += ".3gp";
                                    }
                                    File newFile = new File(selected.getParent(), newName);
                                    if (selected.renameTo(newFile)) {
                                        recordings.set(position, newFile);
                                        names.set(position, newFile.getName());
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else if (which == 1) {
                    // Delete
                    new AlertDialog.Builder(this)
                            .setTitle("Delete File")
                            .setMessage("Are you sure you want to delete this recording?")
                            .setPositiveButton("Delete", (d, w) -> {
                                if (selected.delete()) {
                                    recordings.remove(position);
                                    names.remove(position);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
            builder.show();
            return true;
        });
    }
}

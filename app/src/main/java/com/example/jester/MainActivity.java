package com.example.jester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WorkspaceView workspace;
    private ImageView addButton, downArrowButton, magnifierLensButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialization();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.saveMenuButton) {
            save();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void save() {
        Bitmap bitmap = ((WorkspaceView) findViewById(R.id.workspace)).getMainBitmap();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, arrayOutputStream);

        FileOutputStream fileOutputStream = null;

        if (isPermissionStorageGranted()) {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "DebugData");

            if (!folder.exists()) {
                Log.d("Folder created: ", String.valueOf(folder.mkdir()));
            }

            File file = new File(folder, "Image-" + System.currentTimeMillis() + ".jpg");
            try {
                Log.d("File created: ", String.valueOf(file.createNewFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file.exists()) {
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(arrayOutputStream.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isPermissionStorageGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { // permission is automatically granted on sdk < 23 upon installation
            return true;
        }
    }

    private void initialization() {
        workspace = findViewById(R.id.workspace);
        addButton = findViewById(R.id.addButton);
        downArrowButton = findViewById(R.id.arrowDownButton);
        magnifierLensButton = findViewById(R.id.magnifierLensButton);
        final ConstraintLayout constraintLayout = findViewById(R.id.instrumentsLayout);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButton.setVisibility(View.GONE);
                downArrowButton.setVisibility(View.GONE);
                magnifierLensButton.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.INVISIBLE);
                workspace.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = workspace.getLayoutParams();
                params.width = 300;
                params.height = 300;
                workspace.setLayoutParams(params);
                // TODO thinking about what will be preferred
//                workspace.setBackground(getDrawable(R.drawable.android));
            }
        });

        setLogo();
    }

    private void setLogo() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_jester);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }
}
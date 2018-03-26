package com.pyrooka.crossy_road_unlocker;

import android.Manifest;
import android.animation.TimeAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_REQUEST_CODE = 999;

    private final static String BASE_PATH =
            Environment.getExternalStorageDirectory().toString() +
                    "/Android/data/com.yodo1.crossyroad/files/";

    private final static String CONFIG_NAME = "latest-save.dat";

    private final static String CONFIG_PATH = BASE_PATH + CONFIG_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the button.
        final Button unlockButton = (Button) findViewById(R.id.button_unlock);

        // Check READ and WRITE permissions.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }

        // On the Unlock button click.
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unlockButton.setEnabled(false);

                InputStream is = getResources().openRawResource(
                        getResources().getIdentifier("latest_save", "raw", getPackageName())
                );

                try {
                    FileOutputStream fos = new FileOutputStream(new File(BASE_PATH + "latest-save.dat"));

                    int character;

                    while ((character = is.read()) != -1) {
                        fos.write(character);
                        fos.flush();
                    }

                    fos.close();

                    Toast.makeText(view.getContext(), "Unlocked.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("EXCEPTION", "Cannot write modified config. " + e.getMessage());
                    Toast.makeText(view.getContext(), "Cannot unlock.", Toast.LENGTH_SHORT).show();
                } finally {
                    unlockButton.setEnabled(true);
                }

                /*

                Boolean result = checkConfig(CONFIG_PATH);

                if (!result) {
                    Toast.makeText(view.getContext(), "Config not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String unzippedFileName = unzip(CONFIG_PATH, BASE_PATH);

                if (unzippedFileName == null || unzippedFileName == "") {
                    Toast.makeText(view.getContext(), "Error while unzipping.", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder content = new StringBuilder();

                try {


                    BufferedReader bf = new BufferedReader(new FileReader(BASE_PATH + unzippedFileName));

                    String currentLine;
                    while ((currentLine = bf.readLine()) != null) {
                        content.append(currentLine).append("\n");
                    }

                    // Remove the last line. (\n)
                    int length = content.length();
                    content.delete(length - 1, length + 1);

                } catch (IOException e) {
                    Log.e("EXCEPTION", "Error occured while readin the config file. " + e.getMessage());
                    return;
                }

                String config = content.toString();

                Toast.makeText(view.getContext(), config, Toast.LENGTH_SHORT).show();

                config = config.replace("false", "true");


                Toast.makeText(view.getContext(), config, Toast.LENGTH_SHORT).show();

                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(BASE_PATH + unzippedFileName));
                    bw.write(config);
                    bw.close();
                } catch (IOException e) {
                    Log.e("EXCEPTION", "Cannot write modified config. " + e.getMessage());
                }

                boolean zipResult = zip(unzippedFileName, BASE_PATH + unzippedFileName, CONFIG_PATH);

                if (!zipResult) {
                    Toast.makeText(view.getContext(), "Cannot zip config.", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(view.getContext(), "UNLOCKED", Toast.LENGTH_SHORT).show();
                */
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, nothing to do.
                } else {
                    Toast.makeText(this, "Missing external storage read/write permission(s). The app won't work!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        return;
    }

    protected boolean checkConfig(String path) {
        try {
            File config = new File(path);

            boolean asd = config.exists();

            if (config.exists()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("EXCEPTION", "Error while checking the config. " + e.getMessage());
        }

        return false;
    }

    public boolean zip(String fileName, String filePath, String zipFilePath) {

        int BUFFER = 4096;

        try {
            /*
            FileInputStream fis = new FileInputStream(filePath);
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            GZIPOutputStream zos = new GZIPOutputStream(fos);

            byte[] buffer = new byte[1024];
            int len;

            while ((len = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }

            zos.close();
            fos.close();
            fis.close();

            return true;

            */
            FileOutputStream dest = new FileOutputStream(zipFilePath);
            DeflaterOutputStream dos = new DeflaterOutputStream(new BufferedOutputStream(dest));

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            BufferedInputStream origin = new BufferedInputStream(new FileInputStream(filePath), BUFFER);

            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                dos.write(data, 0, count);
            }

            dos.close();

            return true;
        } catch (Exception e) {
            Log.e("EXCEPTION", "Error while zipping config. " + e.getMessage());
        }

        return false;
    }

    public String unzip(String zipFile, String targetLocation) {
        try {
            String unzippedFileName = null;

            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {

                FileOutputStream fout = new FileOutputStream(targetLocation + ze.getName());
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }

                zin.closeEntry();
                fout.close();

                unzippedFileName = ze.getName();
            }
            zin.close();

            return unzippedFileName;
        } catch (Exception e) {
            Log.e("EXCEPTION", "Error while unzipping config. " + e.getMessage());
        }

        return null;
    }
}

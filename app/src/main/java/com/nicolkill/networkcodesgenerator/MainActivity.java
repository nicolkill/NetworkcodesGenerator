package com.nicolkill.networkcodesgenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_VALIDATION = 1;

    private TextInputLayout mNumber;
    private Button mButton;

    private MessageDigest mMessageDigest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNumber = (TextInputLayout) findViewById(R.id.number);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFile(Integer.parseInt(mNumber.getEditText().getText().toString()));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mButton.setEnabled(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionsRejected();
            }
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_VALIDATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_VALIDATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mButton.setEnabled(true);
            } else {
                showPermissionsRejected();
            }
        }
    }

    private void showPermissionsRejected() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permissions_not_granted)
                .setPositiveButton(R.string.close, null)
                .create()
                .show();
    }

    private void createFile(int number) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "El almacenamiento no esta disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "/networkcodesgenerator");
            Toast.makeText(this, "Creando", Toast.LENGTH_SHORT).show();
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file, "users.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            wr.println("/ip hotspot user");
            wr.println("add name=admin");
            for (int i = 0;i < number;i++) {
                wr.println("add limit-uptime=1h name=1kl6vz9 password=09yg");
                wr.println("add limit-uptime=1h name=" + getMD5WithLength(i, 7) + " password=" + getMD5WithLength(i, 4));
            }
            wr.close();
            Toast.makeText(this, "Creado", Toast.LENGTH_SHORT).show();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(sendIntent, "Enviar a..."));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private String getMD5WithLength(int i, int length) throws NoSuchAlgorithmException {
        return getMD5(i).substring(0, length);
    }

    private String getMD5(int i) throws NoSuchAlgorithmException {
        return getMD5((i + System.currentTimeMillis()) + "");
    }

    private String getMD5(String plaintext) throws NoSuchAlgorithmException {
        if (mMessageDigest == null) {
            mMessageDigest = MessageDigest.getInstance("MD5");
        }
        mMessageDigest.reset();
        mMessageDigest.update(plaintext.getBytes());
        byte[] digest = mMessageDigest.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }
        return hashtext;
    }

}

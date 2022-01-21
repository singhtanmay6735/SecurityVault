package com.dev.security;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.FileUtils;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.google.android.material.snackbar.Snackbar;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class Dashboard extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 201;
    private static final int CAMERA_REQUEST_CODE = 202;

    Button upload, capture, openstorage;
    View v;
    ImageView imageView;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dashboard);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

        upload = findViewById(R.id.uploadbtn);
        capture = findViewById(R.id.capturebtn);
        openstorage = findViewById(R.id.openstrgbtn);


        if(!checkPermission()){
            requestPermission();
        }

        //getApplicationContext().getExternalFilesDirs("SecurityVault/encryptedImages");

        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root, "/SecurityVault/encryptedImages");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Toast.makeText(getApplicationContext(), dir.exists() + "", Toast.LENGTH_SHORT).show();

        upload.setOnClickListener(this);
        capture.setOnClickListener(this);
        openstorage.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.uploadbtn:
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                activityResultLauncher.launch(chooseFile);
                break;
            case R.id.capturebtn:
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, CAMERA_REQUEST_CODE);
                break;
            case R.id.openstrgbtn:
                Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
                String path = Environment.getExternalStorageDirectory() + "/SecretVault/encryptedImages/";
                Uri uri = Uri.parse(path);
                chooser.addCategory(Intent.CATEGORY_OPENABLE);
                chooser.setDataAndType(uri, "*/*");
                startActivityForResult(chooser, STORAGE_REQUEST_CODE);
                break;

        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                String fullPath = UriUtils.getPathFromUri(getApplicationContext(), Uri.parse(data.getData().toString()));
                Toast.makeText(getApplicationContext(), fullPath, Toast.LENGTH_SHORT).show();

                KeyGenerator keyGenerator = null;
                try {
                    SecretKeySpec secretKey;
                    byte[] key;
                    String myKey = "SecretVaultbyTanmaySingh";

                    MessageDigest sha = null;
                    key = myKey.getBytes("UTF-8");
                    System.out.println(key.length);
                    sha = MessageDigest.getInstance("SHA-1");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key, 16); // use only first 128 bit
                    System.out.println(key.length);
                    System.out.println(new String(key, "UTF-8"));
                    secretKey = new SecretKeySpec(key, "AES");

                    Encryption.set(getApplicationContext(), fullPath, secretKey);
                    Encryption.getFile();
                    Encryption.encryptFile();
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }

            }
        }
    });


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:

                String fullPath = UriUtils.getPathFromUri(getApplicationContext(), Uri.parse(data.getData().toString()));
                try {
                    SecretKeySpec secretKey;
                    byte[] key;
                    String myKey = "SecretVaultbyTanmaySingh";

                    MessageDigest sha = null;
                    key = myKey.getBytes("UTF-8");
                    System.out.println(key.length);
                    sha = MessageDigest.getInstance("SHA-1");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key, 16); // use only first 128 bit
                    System.out.println(key.length);
                    System.out.println(new String(key, "UTF-8"));
                    secretKey = new SecretKeySpec(key, "AES");

                    Encryption.set(getApplicationContext(), fullPath, secretKey);
                    Encryption.getFile();
                    Encryption.decryptFile();
                    openFile(fullPath);
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }

                break;
            case CAMERA_REQUEST_CODE:
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                String root = Environment.getExternalStorageDirectory().toString();
                String timestamp = System.currentTimeMillis()+"";
                String name = "/SecurityVault/encryptedImages/"+timestamp+".jpg";
                String path = root+name;
                File file = new File(root, name);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                try {
                    SecretKeySpec secretKey;
                    byte[] key;
                    String myKey = "SecretVaultbyTanmaySingh";

                    MessageDigest sha = null;
                    key = myKey.getBytes("UTF-8");
                    System.out.println(key.length);
                    sha = MessageDigest.getInstance("SHA-1");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key, 16); // use only first 128 bit
                    System.out.println(key.length);
                    System.out.println(new String(key, "UTF-8"));
                    secretKey = new SecretKeySpec(key, "AES");

                    Encryption.set(getApplicationContext(), path, secretKey);
                    Encryption.getFile();
                    Encryption.encryptFile();
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void openFile(final String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(path));
        intent.setDataAndType(uri, "*/*");
        startActivity(intent);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean read = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean write = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean camera = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (read && write && camera)
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Storage and Camera.", Toast.LENGTH_SHORT).show();
                    else {

                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Storage and Camera.", Toast.LENGTH_SHORT).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                createDialog("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void createDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Dashboard.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
package com.dev.security;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class Encryption {
    static File file;
    static byte[] fileContent;
    static String path;
    static SecretKey key;
    static Context mContext;

    public static void set(Context mContext, String path, SecretKey key){
        Encryption.path = path;
        Encryption.key = key;
        Encryption.mContext = mContext;
    }

    public static void getFile() throws IOException {

        File f = new File(path);
        InputStream is = null;
        try {
            is = new FileInputStream(f);
        } catch (FileNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        byte[] content = null;
        try {
            content = new byte[is.available()];
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            is.read(content);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        file = f;
        fileContent = content;
        //return content;
    }


    public static void encryptFile() throws IOException {
        Cipher cipher;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveFile(encrypted);
        //return encrypted;

    }

    public static void decryptFile() throws IOException {
        Cipher cipher;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveFile(decrypted);
    }

    public static void saveFile(byte[] bytes) throws IOException {

        if(bytes != null) {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        }

    }
}

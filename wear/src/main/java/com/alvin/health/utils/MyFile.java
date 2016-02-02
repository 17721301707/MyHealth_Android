package com.alvin.health.utils;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by alvin on 2015/11/18.
 *
 */
public class MyFile {
    private String mPath;
    private static final String TAG = "Alvin";
    public MyFile() {
        mPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp";
        File path = new File(mPath);
        if (!path.exists()) {
            path.mkdir();
        }
    }

    /**
     * Write String to file
     * @param name file name
     * @param input input context
     */
    public void write(final String name, final String input) {

        File file = new File(mPath + "/" + name);
        try {
            FileOutputStream out = new FileOutputStream(file, true);
            out.write(input.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Delete File
     * @param name file name
     */
    public boolean deleteFile(String name) {
        boolean res = false;
        File file = new File(mPath + "/" + name);
        if (file.exists()) {
            res = file.delete();
        }
        return res;
    }

    /**
     * Get File as Asset Format
     * @param name file name
     * @return asset
     */
    public Asset getFile(String name) {
        Asset mAsset = null;
        File file = new File(mPath + "/" + name);
        if (file.exists()) {
            try {
                int len = 0;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                FileInputStream inputStream = new FileInputStream(file);
                byte [] buffer = new byte[255];
                while( (len = inputStream.read(buffer)) != -1){
                    outStream.write(buffer, 0, len);
                }
                mAsset = Asset.createFromBytes(outStream.toByteArray());
                outStream.flush();
                inputStream.close();
                outStream.close();
            } catch (Exception e) {
                Log.d(TAG, "read  file exception:" + e.toString());
            }
        }
        return  mAsset;
    }
}

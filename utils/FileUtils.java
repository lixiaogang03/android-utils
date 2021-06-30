package com.wif.baseservice.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static void writeFile(File file, String message) {
        try {
            writeFileMayException(file, message);
        } catch (IOException e) {
            Log.w(TAG, "writeFile " + file.getAbsolutePath() + " fail: " + e.getMessage());
        }
    }

    /**
     * Write file
     *
     * @param file    Write file
     * @param message Written content
     * @throws IOException e
     */
    public static void writeFileMayException(File file, String message) throws IOException {
        FileWriter fWriter = new FileWriter(file);
        fWriter.write(message);
        fWriter.close();
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            boolean success = file.delete();
            Log.i(TAG, "delete " + file.getAbsolutePath() + (success ? " success" : " fail"));
        }
    }

    public static String readFile(File file) {
        try {
            return readFileMayException(file);
        } catch (IOException e) {
            Log.w(TAG, "readFile " + file.getAbsolutePath() + " fail: " + e.getMessage());
        }
        return null;
    }

    /**
     * Read file
     *
     * @param file Read file
     * @return Read content
     * @throws IOException e
     */
    public static String readFileMayException(File file)
            throws IOException {
        try (FileReader fRead = new FileReader(file)) {
            BufferedReader buffer = new BufferedReader(fRead);
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = buffer.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        }
    }

}

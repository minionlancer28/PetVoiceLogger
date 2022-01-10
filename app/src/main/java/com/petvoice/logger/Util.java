package com.petvoice.logger;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Half;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {

    public static final String TAG = "Util";

    private static BufferedWriter csvWriter = null;
    private static BufferedWriter logWriter = null;
    private static String csvFilePath = null;
    private static String csvFileName = null;
    private static String logFilePath = null;
    private static String logFileName = null;
    private static String mp4FilePath = null;
    private static String mp4FileName = null;


    /** Create a file Uri for saving an image or video */

    public static void initLogWriter(){
        logWriterClose();
        setLogFile();
        try {
            logWriter = new BufferedWriter(new FileWriter(logFilePath));
        }catch (Exception e){
            logWriter = null;
            e.printStackTrace();
        }
    }

    public static void initCsvWriter(){
        csvWriterClose();
        setOutputMediaFile();
        try {
            csvWriter = new BufferedWriter(new FileWriter(csvFilePath));
            csvWriter.append("time,gFx,gFy,gFz,wx,wy,wz,Bx,By,Bz,interpolation,temp");
            csvWriter.newLine();
        }catch (Exception e){
            csvWriter = null;
            e.printStackTrace();
        }
    }

    public static String getCsvFileName(){
        if( csvFileName == null) return "";
        return csvFileName;
    }

    public static String getLogFileName(){
        if( logFileName == null) return "";
        return logFileName;
    }

    public static String getMp4FileName(){
        if( mp4FileName == null) return "";
        return mp4FileName;
    }
    public static String getMp4FilePath(){
        if( mp4FilePath == null) return "";
        return mp4FilePath;
    }



    /** Create a File for saving an image or video */
    private static boolean setOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "PVLogger");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("BLESensor", "failed to create directory");
                return false;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


        mp4FileName = "VID_"+ timeStamp + ".mp4";
        mp4FilePath = mediaStorageDir.getPath() + File.separator + mp4FileName;

        csvFileName = "DATA_"+ timeStamp +".csv";
        csvFilePath = mediaStorageDir.getPath() + File.separator+ csvFileName;
        return true;
    }

    private static void setLogFile(){
        logFileName = "log.txt";
        logFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator+ "PVLogger"+File.separator + logFileName;
    }
    public static void logWriterClose(){
        if (logWriter != null){
            try {
                logWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void csvWriterClose(){
        if (csvWriter != null){
            try {
                csvWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void  printLog(String text)
    {
        if (logWriter == null) return;
        try{
            logWriter.append(text);
            logWriter.newLine();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean exportData(String time,float[] accel, float[] gyro, float[] compass, float temp, boolean isGetting){

        try
        {
            //BufferedWriter for performance, true to set append to file flag

//            Calendar calendar = Calendar.getInstance();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE dd-MMM-yyyy hh-mm-ss a,");
//            buf.append(simpleDateFormat.format(calendar.getTime()));
            csvWriter.append(time);
            csvWriter.append(",");
            for(int i =0; i<3; i++){
                csvWriter.append(zeroPrint(accel[i]));
                csvWriter.append(",");
            }
            for(int i =0; i<3; i++){
                csvWriter.append(zeroPrint(gyro[i]));
                csvWriter.append(",");
            }
            for(int i =0; i<3; i++){
                csvWriter.append(zeroPrint(compass[i]));
//                csvWriter.append(i<2?",":"");
                csvWriter.append(",");
            }
            if (isGetting){
                csvWriter.append("0");
            }else{
                csvWriter.append("1");
            }
            csvWriter.append(",");
            csvWriter.append(zeroPrint(temp));

            csvWriter.newLine();
            return true;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public static String zeroPrint(float value){
        if(value == 0.0f){
            return "0";
        }
        else{
            return String.valueOf(value);
        }
    }
    public static String formatTime(float time){
        NumberFormat formatter = new DecimalFormat("0.00");
        return formatter.format(time);
    }

    public static void exportMp4ToGallery(Context context) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, csvFilePath);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + csvFilePath)));
    }

    private static short bytesToInt16(byte[] bytes, int offset ){
        try {
            int temp1 = bytes[offset] & 0xFF;
            int temp2 = bytes[offset + 1] & 0xFF;

            return (short) (temp2 <<8 | temp1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    public static float[] getAccel(byte[] bytes, int offset){

        try{
            short[] intAccel = getInt16(bytes, offset);
            float[] accel = new float[3];

            accel[0] = (float) ((double)intAccel[0] * 32.0/65536.0);
            accel[1] = (float) ((double)intAccel[1] * 32.0/65536.0);
            accel[2] = (float) ((double)intAccel[2] * 32.0/65536.0);
            return accel;
        }catch(Exception e){
            e.printStackTrace();
        }
        float[] empty = {(float)0.0,(float)0.0,(float)0.0};
        return empty;
    }

    public static float[] getGyro(byte[] bytes, int offset){
        try{
            short[] intGryo = getInt16(bytes, offset);
            float[] gyro = new float[3];
            gyro[0] = (float) ((double)intGryo[0] * 4000.0/65536.0);
            gyro[1] = (float) ((double)intGryo[1] * 4000.0/65536.0);
            gyro[2] = (float) ((double)intGryo[2] * 4000.0/65536.0);
            return gyro;
        }catch(Exception e){
            e.printStackTrace();
        }
        float empty[] = {(float)0.0,(float)0.0,(float)0.0};
        return empty;
    }

    public static float[] getCompass(byte[] bytes, int offset){

        try{
            short[] intCompass = getInt16(bytes, offset);
            float[] compass = new float[3];
            compass[0] = (float) ((double)intCompass[0] * 600.0/4096.0);
            compass[1] = (float) ((double)intCompass[1] * 600.0/4096.0);
            compass[2] = (float) ((double)intCompass[2] * 600.0/4096.0);
            return compass;
        }catch(Exception e){
            e.printStackTrace();
        }
        float[] empty = {(float)0.0,(float)0.0,(float)0.0};
        return empty;
    }

    public static float getTemperature(byte[] bytes){
        try {
            int temp1 = bytes[19] & 0xFF;
            int temp2 = bytes[20] & 0xFF;
            short temp = (short) (temp2<<8 | temp1);
            float temperature = (float) (-45.0 + 175.0 * (double)temp/65535.0);
            return temperature;
        }catch (Exception e){
            e.printStackTrace();
        }
        return (float) 0.0;
    }

    private static short[] getInt16(byte[] bytes, int offset){
        short[] ret = new short[3];
        ret[0] = bytesToInt16(bytes, offset);
        ret[1] = bytesToInt16(bytes, offset+2);
        ret[2] = bytesToInt16(bytes, offset+4);
        return ret;
    }


    //    public static float[] Half(byte[] data, int offset){
//        float[] result = new float[3];
//        for(int i = 0, j=offset; i< 3; i++, j+=2){
//            result[i] = getHalf(new byte[]{data[j],data[j+1]});
//        }
//        return result;
//    }
//    public static float getHalf(byte[] buffer){
//        byte first = buffer[1];
//        byte second = buffer[0];
//        byte sign = (byte)(first>>7);
//        byte stored = (byte)((first & 0x7C) >>2);
//        byte implicit = (byte)((stored==0)?0:1);
//        short significand = (short)(((short)(first & (short)0x0003)<<8) | (short)second);
//        float result = (float)Math.pow(-1,sign) * (float)Math.pow(2,(stored - 15)) * (implicit + (float)(significand/1024f));
//        return result;
//    }
//
//    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
}

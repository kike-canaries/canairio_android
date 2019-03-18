package com.hpsaturn.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Antonio Vanegas @hpsaturn on 3/12/17.
 */

public class FileTools {

    public static final String TAG = FileTools.class.getSimpleName();

    public static String saveByteArrayImage(byte[] face) {
        File file;
        try {
            file = getTempFilePath();
            FileOutputStream fos = new FileOutputStream(file.getPath());
            fos.write(face);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file.getPath();
    }

    public static class saveImage extends AsyncTask<byte[], Void, Void> {

        private final byte[] image;

        public saveImage(byte[] image) {
            this.image = image;
        }

        @Override
        protected Void doInBackground(byte[]... jpeg) {

            File file = getTempFilePath();
            try {
                FileOutputStream fos = new FileOutputStream(file.getPath());
                fos.write(image);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class saveBitmap extends AsyncTask<Bitmap, Void, Void> {

        private final Bitmap image;

        public saveBitmap(Bitmap image) {
            this.image = image;
        }

        @Override
        protected Void doInBackground(Bitmap... jpeg) {

            File file = getTempFilePath();
            try {
                FileOutputStream fos = new FileOutputStream(file.getPath());
                image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static class saveDownloadFile extends AsyncTask<byte[], Void, Void> {

        private final byte[] downloadFile;
        private final String dirName;
        private final String fileName;

        public saveDownloadFile(byte[] data, String dirName, String fileName) {
            this.downloadFile = data;
            this.dirName = dirName;
            this.fileName = fileName;
        }

        @Override
        protected Void doInBackground(byte[]... data) {
            Logger.d(TAG, "[SD] saveDownloadFile /"+dirName+"/"+fileName);
            File file = getDownloadFilePath(dirName,fileName);
            Logger.d(TAG, "[SD] path: "+file.getAbsolutePath());
            try {
                FileOutputStream fos = new FileOutputStream(file.getPath());
                fos.write(downloadFile);
                fos.close();
                Logger.i(TAG, "[SD] saveDownloadFile done");
            } catch (IOException e) {
                Logger.e(TAG, "[SD] saveDownloadFile failed!");
                e.printStackTrace();
            }
            return null;
        }
    }

    private static File getDownloadStorageDir(String dirName) {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard.getAbsolutePath() + "/" + dirName);
        if (!file.mkdir()) {
            Logger.i(TAG,"[SD] '"+ dirName+ "' mkdir false! (maybe it already exists)");
        }
        return file;
    }

    @NonNull
    private static File getDownloadFilePath(String dirName, String fileName) {
        return new File(getDownloadStorageDir(dirName), fileName);
    }

    @NonNull
    private static File getTempFilePath() {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/temp/");
        dir.mkdir();

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return new File(dir, dateFormat.format(date) + ".jpg");
    }

    public static byte[] convertNV21toRGBA888(Context ctx, byte[] data, int mWidth, int mHeight){
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Allocation bmData = renderScriptNV21ToRGBA888(
                ctx,
                mWidth,
                mHeight,
                data);
        bmData.copyTo(bitmap);
        return byteArrayFromBitmap(bitmap);
    }

    public static byte[] byteArrayFromBitmap(Bitmap bmp) {
        byte[] byteArray;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }


    private byte[] getImageFromPath(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        return getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
    }


    @Deprecated
    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    @Deprecated
    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }


    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static File getImageFile(String name) {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/temp/");
        return new File(dir, name);
    }

    public static boolean isPackageInstalled(Context ctx, String packagename) {
        try {
            ctx.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}


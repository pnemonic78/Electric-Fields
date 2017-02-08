package com.github.fields.electric;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Task to save a bitmap to a file.
 *
 * @author moshe.w
 */
public class SaveFileTask extends AsyncTask<Bitmap, File, File> {

    protected final Context context;
    protected final DateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);

    public SaveFileTask(Context context) {
        this.context = context;
    }

    @Override
    protected File doInBackground(Bitmap... params) {
        File folderPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(folderPictures, context.getString(R.string.app_folder_pictures));
        folder.mkdirs();

        Bitmap bitmap = params[0];
        File file = new File(folder, generateFileName());

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i(TAG, "save success: " + file);
            return file;
        } catch (IOException e) {
            Log.e(TAG, "save failed: " + file, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        if (file != null) {
            Toast.makeText(context, context.getString(R.string.saved, file.getPath()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled(File file) {
        super.onCancelled(file);
        file.delete();
    }

    protected String generateFileName() {
        return "ef-" + timestampFormat.format(new Date()) + ".png";
    }
}

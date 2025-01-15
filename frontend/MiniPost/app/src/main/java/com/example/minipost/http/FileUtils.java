package com.example.minipost.http;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtils {

    public static String getPathFromUri(Context context, Uri uri) {
        String result = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    result = cursor.getString(index);
                }
            }
            cursor.close();
        }
        return result;
    }
}
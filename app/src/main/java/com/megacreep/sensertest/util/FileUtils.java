package com.megacreep.sensertest.util;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * Created by megacreep on 4/14/2016.
 */
public class FileUtils {

    public static BufferedWriter createFileWriter(Context context, String name) throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory(), "data");
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        return new BufferedWriter(new FileWriter(new File(dir, name)));
    }

    public static class CSVFileWriter {
        private BufferedWriter bw;
        public CSVFileWriter(Context context, String name) throws IOException {
            if (!name.endsWith(".csv")) {
                name = name + ".csv";
            }
            bw = createFileWriter(context, name);
        }

        public void writeEvent(SensorEvent event) throws IOException {
            bw.write(Long.toString(event.timestamp));
            bw.write(", ");
            for (int i = 0; i < event.values.length; i++) {
                bw.write(String.valueOf(event.values[i]));
                if (i != event.values.length - 1) {
                    bw.write(", ");
                }
            }
            bw.write("\n");
        }

        public void close() throws IOException {
            bw.flush();
            bw.close();
        }
    }
}

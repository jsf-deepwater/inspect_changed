package x.datautil;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by phy on 2016/11/14.
 * 创建csv文件
 */

public class CSV {
    static String DEFAULT_PATH = FileUtils.DEFAULT_LOG_PATH;//default path
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");
    static String DEFAULT_NAME = "WP_Log_" + sdf.format(System.currentTimeMillis());//default name
    static String file_suffix = ".csv";
    String file_path;
    String file_name;
    File csvFile;
    BufferedWriter writer;
    final static String COLUMN_SEPERATOR = ",";


    public CSV(String file_name, String file_path) {
        this.file_name = file_name;
        this.file_path = file_path;
        if (TextUtils.isEmpty(file_path)) {
            this.file_path = DEFAULT_PATH;
        }
        if (TextUtils.isEmpty(file_name)) {
            this.file_name = DEFAULT_NAME;
        }
        createFile();
    }

    private File createFile() {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + File.separator + file_path;
            File fPath = new File(path);
            if (!fPath.exists()) {
                fPath.mkdirs();
            }
            csvFile = new File(path, file_name + file_suffix);
            /*if (csvFile.exists()) {
                csvFile.delete();
            }*/
            if (!csvFile.exists()) {
                csvFile.createNewFile();
            }
            csvFile.setExecutable(true);
            csvFile.setReadable(true);
            csvFile.setWritable(true);
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(csvFile));
            }
            return csvFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeLine(String... lineStr) throws IOException {
        if (writer != null) {
            for (String col : lineStr) {
                if (col == null) {
                    col = "";
                }
                writer.write(col);
                writer.write(COLUMN_SEPERATOR);
            }
            writer.newLine();
            writer.flush();
        }
    }

    public void end() {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public String getFile_path() {
        return file_path;
    }

    public String getFile_name() {
        return file_name;
    }

    public File getFile() {
        return csvFile;
    }
}

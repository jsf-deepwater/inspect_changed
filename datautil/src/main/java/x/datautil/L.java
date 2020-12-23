package x.datautil;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by phy on 2018/2/5.
 */

public class L {
    static boolean fileLog = false;
    static boolean consoleLog = false;
    static boolean dbLog = false;
    static String D_TAG = "LOG";
    static WeakReference<CSV> csvRef;
    static WeakReference<XDbManager> mDbManagerRef;
    final static String COLUMN_SEPERATOR = CSV.COLUMN_SEPERATOR;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");

    public static void initConsoleLog(String tag) {
        consoleLog = true;
        if (!TextUtils.isEmpty(tag)) {
            D_TAG = tag;
        }
    }

    /**
     * 文件和db产生的文件存在冲突，暂时不使用这个
     */
    private static void initFileLog() {
        fileLog = true;
        if (fileLog) {
            CSV csv = new CSV(null, null);
            csvRef = new WeakReference<CSV>(csv);
        }
    }

    public static void initDbLog(Context context,XDbManager mDbManager) {
        dbLog = true;
        if (dbLog) {
            mDbManagerRef = new WeakReference<XDbManager>(mDbManager);
        }
    }

    public static void d(String... str) {
        d(D_TAG, str);
    }

    public static void e(String... str) {
        e(D_TAG, str);
    }

    public static void i(String... str) {
        i(D_TAG, str);
    }

    public static void w(String... str) {
        w(D_TAG, str);
    }

    private static void w(String tag, String... str) {
        if (consoleLog) {
            Log.w(tag, arrayToLogString(str));
        }
        if (fileLog) {
            writeFileLog(str);
        }
        if (dbLog) {
            writeDbLog(str);
        }
    }

    private static void i(String tag, String... str) {
        if (consoleLog) {
            Log.i(tag, arrayToLogString(str));
        }
        if (fileLog) {
            writeFileLog(str);
        }
        if (dbLog) {
            writeDbLog(str);
        }
    }

    private static void d(String tag, String... str) {
        if (consoleLog) {
            Log.d(tag, arrayToLogString(str));
        }
        if (fileLog) {
            writeFileLog(str);
        }
        if (dbLog) {
            writeDbLog(str);
        }
    }

    private static void e(String tag, String... str) {
        if (consoleLog) {
            Log.e(tag, arrayToLogString(str));
        }
        if (fileLog) {
            writeFileLog(str);
        }
        if (dbLog) {
            writeDbLog(str);
        }
    }


    private static void writeFileLog(String... str) {
        if (csvRef != null) {
            try {
                if (csvRef.get() != null) {
                    csvRef.get().writeLine(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeDbLog(String... str) {
        if (dbLog && mDbManagerRef != null) {
            try {
                XDbManager manager = mDbManagerRef.get();
                if (manager != null) {
                    manager.writeLog(arrayToDbString(str));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static File createLogFromDb(Context context, XDbManager dbManager, long from, long to) {
        try {
            CSV csv = new CSV(null, null);
            List<XDbManager.LogInfo> data = dbManager.getLog(from, to);
            if (data != null) {
                for (XDbManager.LogInfo info : data) {
                    csv.writeLine(sdf.format(info.time), info.info);
                }
            }
            csv.end();
            return csv.getFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String arrayToDbString(String... str) {
        if (str == null || str.length == 0) {
            return "no data.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            sb.append(str[i]);
            if (i != str.length - 1) {
                sb.append(COLUMN_SEPERATOR);
            }
        }
        return sb.toString();
    }

    public static String arrayToLogString(String... str) {
        if (str == null || str.length == 0) {
            return "no data.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            sb.append(str[i]);
            if (i != str.length - 1) {
                sb.append("   ");
            }
        }
        return sb.toString();
    }

    public static void end() {
        if (fileLog && csvRef != null) {
            try {
                csvRef.get().end();
            } catch (Exception e) {
            }
        }
    }

    public static String getLogFilePath() {
        if (fileLog && csvRef != null) {
            try {
                CSV csv = csvRef.get();
                return csv.file_path + csv.file_name + csv.file_suffix;
            } catch (Exception e) {
            }
        }
        return null;
    }
}

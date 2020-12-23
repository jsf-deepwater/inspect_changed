package phy.jsf.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;

import phy.jsf.R;
import phy.jsf.data.User;
import phy.network.WpServer;
import x.datautil.L;

import static phy.jsf.config.TEST_FLAG;

public class Settings {
    private final static String CONFIG = "config";

    public final static int PERM_WATCH=1;
    public final static int PERM_ADMIN=2;
    public final static int PERM_DEV=3;

    public final static int TASK_UNSTART=0;
    public final static int TASK_CACHE=1;
    public final static int TASK_ERR=2;
    public final static int TASK_COMMIT=3;
    public final static int TASK_ERR_OVER=4;

    public final static int TASK_DAY =1;
    public final static int TASK_NIGHT=2;



    public final static SimpleDateFormat SDF_DATE=new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat SDF_DATE_TIME=new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");

    public final  static String SETTINGS_SERVER_URL="SETTINGS_SERVER_URL";
    public static String server_ip;
    public static String token;

    public static String[] TASK_TYPE;
    public static void init(Context context){
        TASK_TYPE=context.getResources().getStringArray(R.array.task_type);

        test_add_data(context);
        get(context);
    }

    public static boolean test_add_data(Context context){
        if(TEST_FLAG){
            //check
            if(DbManager.getDbManager(context).login_check("hm","123")!=null){
                L.d("test data has already init.");
                return false;
            }
            //add user.
            User user=new User();
            user.user_name="hm";
            user.role=PERM_DEV;
            user.dis_name="humin.xxx.xxx";
            user.pwd="123";
//            "hm","humin.xxx.xxx","123",PERM_DEV
            DbManager.getDbManager(context).addUser(user);
            //add device
//            Device device=new Device("FE9738011063","小长毛兽","曙光大厦C座",3,302);
//            DbManager.getDbManager(context).addDevice(device);
//            String content="{\"id\":\"FE9738011063\",\"content\":\"temp\",\"check\":\"temp<18\"}";
//            Task task=new Task("M0001", "温度检查", TASK_TYPE_MONTH,TASK_STATE_UNCOMMIT, "\t2020-10-01", null,null, "FE9738011063", content);
//            DbManager.getDbManager(context).addTask(task);
        }
        return false;
    }

    public static String head_url;
    public static User curUser;
    static SharedPreferences sharedPreference;

    private static SharedPreferences getPreferences(Context context) {
        if (sharedPreference == null) {
            sharedPreference = context.getSharedPreferences(CONFIG, Activity.MODE_PRIVATE);
        }
        return sharedPreference;
    }

    public static void clear(){
        curUser=null;
        head_url=null;
    }

    public static void get(Context context) {
        SharedPreferences sp = getPreferences(context);
        if (sp != null) {
            server_ip=sp.getString(SETTINGS_SERVER_URL, WpServer.DEF_SERVER_IP);
        }
    }

    public static void apply(Context context) {
        SharedPreferences sp = getPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Settings.SETTINGS_SERVER_URL,server_ip);
        editor.apply();
    }

}

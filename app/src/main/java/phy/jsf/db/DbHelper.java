package phy.jsf.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static phy.jsf.db.DbAttrs.C_CHECK_TIME;
import static phy.jsf.db.DbAttrs.C_CHECK_USER;
import static phy.jsf.db.DbAttrs.C_DIS_NAME;
import static phy.jsf.db.DbAttrs.C_EMAIL;
import static phy.jsf.db.DbAttrs.C_FORM_DAY_NIGHT;
import static phy.jsf.db.DbAttrs.C_FORM_ID;
import static phy.jsf.db.DbAttrs.C_FORM_NAME;
import static phy.jsf.db.DbAttrs.C_FORM_TYPE;
import static phy.jsf.db.DbAttrs.C_ID_CARD;
import static phy.jsf.db.DbAttrs.C_JOB_CODE;
import static phy.jsf.db.DbAttrs.C_PHONE;
import static phy.jsf.db.DbAttrs.C_PWD;
import static phy.jsf.db.DbAttrs.C_REMARK;
import static phy.jsf.db.DbAttrs.C_ROLE;
import static phy.jsf.db.DbAttrs.C_R_USER_ID;
import static phy.jsf.db.DbAttrs.C_STATUS;
import static phy.jsf.db.DbAttrs.C_TASK_BUILDING;
import static phy.jsf.db.DbAttrs.C_TASK_COMMIT_TIME;
import static phy.jsf.db.DbAttrs.C_TASK_CONTENT;
import static phy.jsf.db.DbAttrs.C_TASK_CREATE_TIME;
import static phy.jsf.db.DbAttrs.C_TASK_DEVICE_FLOOR;
import static phy.jsf.db.DbAttrs.C_TASK_DEVICE_ID;
import static phy.jsf.db.DbAttrs.C_TASK_DEVICE_NAME;
import static phy.jsf.db.DbAttrs.C_TASK_DEVICE_ROOM;
import static phy.jsf.db.DbAttrs.C_TASK_EDIT_CONTENT;
import static phy.jsf.db.DbAttrs.C_TASK_ID;
import static phy.jsf.db.DbAttrs.C_TASK_SCHEDULER_TIME;
import static phy.jsf.db.DbAttrs.C_TASK_STATE;
import static phy.jsf.db.DbAttrs.C_TASK_UPLOAD_STATE;
import static phy.jsf.db.DbAttrs.C_TASK_UPLOAD_TIME;
import static phy.jsf.db.DbAttrs.C_USER_ID;
import static phy.jsf.db.DbAttrs.C_USER_NAME;
import static phy.jsf.db.DbAttrs.T_TASK;
import static phy.jsf.db.DbAttrs.T_USER;

public class DbHelper extends SQLiteOpenHelper {
    static int version =DbAttrs.VERSION;//版本号

    public DbHelper(Context context) {
        super(context, DbAttrs.DB_NAME, null, DbAttrs.VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql_user="create table "+T_USER+"("
                +C_USER_ID+" text,"
                +C_ID_CARD+" text,"
                +C_JOB_CODE+" text,"
                +C_DIS_NAME+" text,"
                +C_PWD+" text,"
                +C_PHONE +" text,"
                +C_REMARK +" text,"
                +C_USER_NAME+" text,"
                +C_EMAIL+" text,"
                +C_STATUS+" integer,"
                +C_ROLE+" integer"
                +");";
        sqLiteDatabase.execSQL(sql_user);

        String sql_task="create table "+T_TASK+"("
                +C_TASK_ID+" text,"
                +C_FORM_ID+" text,"
                +C_FORM_NAME+" text,"
                +C_FORM_TYPE+" int,"
                +C_FORM_DAY_NIGHT+" integer,"
                +C_TASK_DEVICE_ID+" text,"
                +C_TASK_DEVICE_NAME+" text,"
                +C_TASK_BUILDING+" text,"
                +C_TASK_DEVICE_FLOOR+" text,"
                +C_TASK_DEVICE_ROOM+" text,"
                +C_TASK_CONTENT+" text,"
                +C_TASK_EDIT_CONTENT+" text,"
                +C_TASK_SCHEDULER_TIME+" int,"
                +C_TASK_CREATE_TIME+" int,"
                +C_TASK_UPLOAD_TIME+" int,"
                +C_TASK_STATE+" int,"
                +C_R_USER_ID+" text,"
                +C_CHECK_USER+" text,"
                +C_TASK_COMMIT_TIME+" int,"
                +C_CHECK_TIME+" int,"
                +C_TASK_UPLOAD_STATE+" int"
                +");";
        sqLiteDatabase.execSQL(sql_task);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

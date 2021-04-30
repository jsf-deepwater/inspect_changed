package phy.jsf.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import phy.jsf.data.Task;
import phy.jsf.data.User;
import x.datautil.L;
import x.datautil.XDbManager;

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
import static phy.jsf.db.DbAttrs.C_SCAN_TIME;
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

/***
 * 直接在这里增加权限控制
 * ***/
public class DbManager implements XDbManager {
    private final static Object mLock = new Object();
    static DbManager manager;
    DbHelper dbHelper;
//    SQLiteDatabase db;
    public static DbManager getDbManager(Context context) {
        synchronized (mLock) {
            if (manager == null) {
                manager = new DbManager();
                manager.dbHelper = new DbHelper(context);
//                manager.db = manager.dbHelper.getWritableDatabase();
            }
            return manager;
        }
    }

    public void addUser(User user) {
        if(user.user_name!=null){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(C_USER_NAME, user.user_name);
            cv.put(C_DIS_NAME, user.dis_name);
            cv.put(C_USER_ID,user.user_id);
            cv.put(C_ID_CARD, user.id_card);
            cv.put(C_JOB_CODE, user.job_code);
            cv.put(C_PWD, user.pwd);
            cv.put(C_PHONE, user.phone);
            cv.put(C_REMARK, user.remark);
            cv.put(C_STATUS, user.status);
            cv.put(C_EMAIL, user.email);
            cv.put(C_ROLE,user.role);
            db.insert(T_USER, null, cv);
        }
    }

    public boolean update_task_from_Edit(Task localTask,Task editTask){
       localTask.edit_content=editTask.edit_content;
       localTask.state=editTask.state;
       localTask.user_id=editTask.user_id;
       if(!TextUtils.isEmpty(editTask.check_user)){
           localTask.check_user=editTask.check_user;
           localTask.check_time=editTask.check_time;
       }
        localTask.upload_time=editTask.upload_time;
        localTask.commit_time=editTask.commit_time;
        localTask.upload_state=editTask.upload_state;
        localTask.create_time=editTask.create_time;
        return true;
    }

    public boolean  update_task_from_Server(Task localTask,Task serverTask){
        localTask.task_id=serverTask.task_id;
        localTask.form_id=serverTask.form_id;
        localTask.form_type=serverTask.form_type;
        localTask.form_name=serverTask.form_name;
        localTask.form_day_night=serverTask.form_day_night;
        localTask.device_id=serverTask.device_id;
        localTask.device_name=serverTask.device_name;
        localTask.building=serverTask.building;
        localTask.device_floor=serverTask.device_floor;
        localTask.device_name=serverTask.device_name;
        localTask.scheduler_time=serverTask.scheduler_time;
        //localTask.create_time=serverTask.create_time;
        if(localTask.content==null||!localTask.content.equals(serverTask.content)){
            localTask.edit_content=null;
        }
        return true;
    }
    public void addTask(Task task,boolean fromServer){
        if(!TextUtils.isEmpty(task.task_id)&&!TextUtils.isEmpty(task.form_id)){
            ArrayList<Task> localTasks=new ArrayList<>();
            getTaskByTaskId(task.task_id,localTasks);
            Task localTask;
            if(localTasks.size()!=0){
                localTask=localTasks.get(0);
                if(fromServer){
                    update_task_from_Server(localTask,task);
                }else{
                    update_task_from_Edit(localTask,task);
                }
            }else{
                localTask=task;
            }
            dbHelper.getWritableDatabase().delete(T_TASK,C_TASK_ID+" = ?",new String[]{localTask.task_id});
            L.e("add task...");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(C_TASK_ID, localTask.task_id);
            cv.put(C_FORM_ID,localTask.form_id);
            cv.put(C_FORM_TYPE, localTask.form_type);
            cv.put(C_FORM_NAME,localTask.form_name);
            cv.put(C_FORM_DAY_NIGHT, localTask.form_day_night);
            cv.put(C_TASK_DEVICE_ID,localTask.device_id);
            cv.put(C_TASK_DEVICE_NAME,localTask.device_name);
            cv.put(C_TASK_BUILDING,localTask.building);
            cv.put(C_TASK_DEVICE_FLOOR, localTask.device_floor);
            cv.put(C_TASK_DEVICE_ROOM,localTask.device_room);
            cv.put(C_TASK_CONTENT,localTask.content);
            cv.put(C_TASK_EDIT_CONTENT,localTask.edit_content);
            cv.put(C_TASK_SCHEDULER_TIME,localTask.scheduler_time);
            cv.put(C_TASK_CREATE_TIME,localTask.create_time);
            cv.put(C_TASK_COMMIT_TIME,localTask.commit_time);
            cv.put(C_TASK_STATE,localTask.state);
            cv.put(C_R_USER_ID,localTask.user_id);
            cv.put(C_CHECK_USER,localTask.check_user);
            cv.put(C_CHECK_TIME,localTask.check_time);
            cv.put(C_TASK_UPLOAD_TIME,localTask.upload_time);
            cv.put(C_TASK_UPLOAD_STATE,localTask.upload_state);
            long id=db.insert(T_TASK, null, cv);
            L.e("insert res:"+id);
        }
    }

    private void getTaskFromCursor(ArrayList<Task> taskList,Cursor cursor){
        while(cursor.moveToNext()){
            Task task=new Task();
            task.setTask_id(cursor.getString(cursor.getColumnIndex(C_TASK_ID)));
            task.setForm_id(cursor.getString(cursor.getColumnIndex(C_FORM_ID)));
            task.setForm_name(cursor.getString(cursor.getColumnIndex(C_FORM_NAME)));
                    task.setForm_type(cursor.getInt(cursor.getColumnIndex(C_FORM_TYPE)));
                    task.setForm_day_night(cursor.getInt(cursor.getColumnIndex(C_FORM_DAY_NIGHT)));
                    task.setDevice_id(cursor.getString(cursor.getColumnIndex(C_TASK_DEVICE_ID)));
                    task.setDevice_name(cursor.getString(cursor.getColumnIndex(C_TASK_DEVICE_NAME)));
                    task.setBuilding(cursor.getString(cursor.getColumnIndex(C_TASK_BUILDING)));
                    task.setDevice_floor(cursor.getString(cursor.getColumnIndex(C_TASK_DEVICE_FLOOR)));
                    task.setDevice_room(cursor.getString(cursor.getColumnIndex(C_TASK_DEVICE_ROOM)));
                    task.setContent(cursor.getString(cursor.getColumnIndex(C_TASK_CONTENT)));
                    task.setEdit_content(cursor.getString(cursor.getColumnIndex(C_TASK_EDIT_CONTENT)));
                    task.setScheduler_time(cursor.getLong(cursor.getColumnIndex(C_TASK_SCHEDULER_TIME)));
                    task.setCreate_time(cursor.getLong(cursor.getColumnIndex(C_TASK_CREATE_TIME)));
                    task.setUpload_time(cursor.getLong(cursor.getColumnIndex(C_TASK_UPLOAD_TIME)));
                    task.setState(cursor.getInt(cursor.getColumnIndex(C_TASK_STATE)));
                    task.setUser_id(cursor.getString(cursor.getColumnIndex(C_R_USER_ID)));
                    task.setCheck_user(cursor.getString(cursor.getColumnIndex(C_CHECK_USER)));
                    task.setCheck_time(cursor.getLong(cursor.getColumnIndex(C_CHECK_TIME)));
                    task.setCommit_time(cursor.getLong(cursor.getColumnIndex(C_TASK_COMMIT_TIME)));
                    task.upload_state=cursor.getInt(cursor.getColumnIndex(C_TASK_UPLOAD_STATE));
                    L.e("task id:"+task.task_id);
            taskList.add(task);
        }
    }
    private void getUserFromCursor(ArrayList<User> userList,Cursor cursor){
        while(cursor.moveToNext()){
            User user=new User();
            user.user_id=cursor.getString(cursor.getColumnIndex(C_USER_ID));
            user.job_code=cursor.getString(cursor.getColumnIndex(C_JOB_CODE));
            user.id_card=cursor.getString(cursor.getColumnIndex(C_ID_CARD));
            user.dis_name=cursor.getString(cursor.getColumnIndex(C_DIS_NAME));
            user.pwd=cursor.getString(cursor.getColumnIndex(C_PWD));
            user.phone=cursor.getString(cursor.getColumnIndex(C_PHONE));
            user.remark=cursor.getString(cursor.getColumnIndex(C_REMARK));
            user.status=cursor.getInt(cursor.getColumnIndex(C_STATUS));
            user.email=cursor.getString(cursor.getColumnIndex(C_EMAIL));
            user.user_name=cursor.getString(cursor.getColumnIndex(C_USER_NAME));
            user.role=cursor.getInt(cursor.getColumnIndex(C_ROLE));
            userList.add(user);
        }
    }
    public void getTaskByDeviceId(String did,ArrayList<Task> taskList){
        taskList.clear();
        String sql="select * from "+T_TASK+" where "+C_TASK_DEVICE_ID+" = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{did});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }



    public void  getTaskByLike(String key,ArrayList<Task> taskList){
//        taskList.clear();
        String sql="select * from "+T_TASK+" where "+
                C_TASK_DEVICE_ID+" like ? or "+C_TASK_DEVICE_NAME+" like ? or "
                +C_FORM_NAME +" like ? or "+C_TASK_BUILDING +" like ? or "
                +C_TASK_DEVICE_FLOOR +" like ? or "+C_TASK_DEVICE_ROOM +" like ? "
                ;
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{key,key,key,key,key,key});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void  getTaskByLike1(String key,ArrayList<Task> taskList, Long planTime){
//        taskList.clear();
        String sql="select * from "+T_TASK+" where "+
                C_TASK_SCHEDULER_TIME + "= ? and "
                + " ( "
                + C_TASK_DEVICE_ID+" like ? or "+C_TASK_DEVICE_NAME+" like ? or "
                + C_FORM_NAME +" like ? or "+C_TASK_BUILDING +" like ? or "
                + C_TASK_DEVICE_FLOOR +" like ? or "+C_TASK_DEVICE_ROOM +" like ? "
                + " ) ";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(planTime),key,key,key,key,key,key});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void  getTaskByFormState(int state,ArrayList<Task> taskList){
//        taskList.clear();
        String sql=null;
        Cursor cursor=null;
        if(state<Settings.TASK_COMMIT){
             sql="select * from "+T_TASK+" where "+
                    C_TASK_STATE+" = ?";
             cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(state)});
        }else{
            sql="select * from "+T_TASK+" where "+
                    C_TASK_STATE+" = ? or "+C_TASK_STATE+" = ? ";
            cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(Settings.TASK_COMMIT),String.valueOf(Settings.TASK_ERR_OVER)});
        }

        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void  getTaskByFormType(int type,ArrayList<Task> taskList){
//        taskList.clear();
        String sql="select * from "+T_TASK+" where "+
                C_FORM_TYPE+" = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(type)});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void getDelayTask(ArrayList<Task> taskList){
//        taskList.clear();
        String sql="select * from "+T_TASK+" where "+
                C_TASK_STATE+" < ? and "+
                C_TASK_COMMIT_TIME+" < "+C_TASK_SCHEDULER_TIME ;
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(Settings.TASK_COMMIT)});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void getTaskByTaskId(String taskId,ArrayList<Task> taskList){
        taskList.clear();
        String sql="select * from "+T_TASK+" where "+C_TASK_ID+" = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{taskId});
        getTaskFromCursor(taskList,cursor);
        cursor.close();
    }

    public void getUserById(String userId,ArrayList<User> userList){
        userList.clear();
        String sql="select * from "+T_USER+" where "+C_USER_ID+" = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{userId});
        getUserFromCursor(userList,cursor);
        cursor.close();

    }

    public void getAllUnUploadTask(ArrayList<Task> taskList){
        taskList.clear();
        String sql;
        Cursor cursor;
        sql="select * from "+T_TASK+ " where "+C_TASK_EDIT_CONTENT +" not null and "+C_TASK_UPLOAD_STATE+" = ?" ;
        cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(0)});
        getTaskFromCursor(taskList,cursor);
        cursor.close();

    }

    public void getAllTask(ArrayList<Task> taskList,User user){
        taskList.clear();
        String sql;
        Cursor cursor;
//        if(user.role==Settings.PERM_WATCH){
//            sql="select * from "+T_TASK +" where "+ C_TASK_STATE +" < ?";
//            cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(Settings.TASK_ERR)});
//        }else if(user.role==Settings.PERM_ADMIN){
//            sql="select * from "+T_TASK+" where "+C_TASK_STATE+" = ?";
//            cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(Settings.TASK_ERR)});
//        }else if(user.role==Settings.PERM_DEV){
            sql="select * from "+T_TASK;
            cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{});
//        }else{return;}
        getTaskFromCursor(taskList,cursor);
        L.e("getAllTask,cnt:"+taskList.size());
        cursor.close();
    }
    public void getAllTask1(ArrayList<Task> taskList,Long planTime){
        taskList.clear();
        String sql;
        Cursor cursor;
        sql="select * from "+T_TASK +" where "+C_TASK_SCHEDULER_TIME+" = ?";
        cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(planTime)});
        getTaskFromCursor(taskList,cursor);
        L.e("getAllTask,cnt:"+taskList.size());
        cursor.close();
    }
    public void getAllUser(ArrayList<User> userList){
        userList.clear();
        String sql="select * from "+T_USER;
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{});
        getUserFromCursor(userList,cursor);
        cursor.close();
    }

    public void getManagerUser(ArrayList<User> userList){
        userList.clear();
        String sql="select * from "+T_USER+" where "+C_ROLE+" = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(Settings.PERM_ADMIN)});
        getUserFromCursor(userList,cursor);
        cursor.close();
    }

//    public void update_task(Task task){
//        dbHelper.getWritableDatabase().delete(T_TASK,C_TASK_ID+" = ?",new String[]{task.task_id});
//
//    }

    public void set_upload_complete_task(String task_id){
//        String sql="update "+T_TASK+" SET "+C_TASK_UPLOAD_STATE+" = " + 1 +" where "+C_TASK_ID+" = "+task_id;
        ContentValues cv=new ContentValues();
        cv.put(C_TASK_UPLOAD_STATE,1);
        int res=dbHelper.getWritableDatabase().update(T_TASK,cv,C_TASK_ID+" = ?",new String[]{task_id});
//        dbHelper.getWritableDatabase().execSQL(sql);
        L.e("upload res:"+res);
    }

    public User login_check(String login_name,String pwd){
        Cursor cursor = dbHelper.getReadableDatabase().query(T_USER, null, C_USER_NAME + " = ? and "+ C_PWD+" = ?", new String[]{login_name,pwd}, null, null, null);
        User user=null;
        if(cursor.moveToFirst()){
            user=new User();
            user.user_id= cursor.getString(cursor.getColumnIndex(C_USER_ID));
            user.id_card=cursor.getString(cursor.getColumnIndex(C_ID_CARD));
            user.job_code= cursor.getString(cursor.getColumnIndex(C_JOB_CODE));
            user.dis_name=cursor.getString(cursor.getColumnIndex(C_DIS_NAME));
            user.phone= cursor.getString(cursor.getColumnIndex(C_PHONE));
            user.pwd=cursor.getString(cursor.getColumnIndex(C_PWD));
            user.user_name= cursor.getString(cursor.getColumnIndex(C_USER_NAME));
            user.remark=cursor.getString(cursor.getColumnIndex(C_REMARK));
            user.email=cursor.getString(cursor.getColumnIndex(C_EMAIL));
            user.status= cursor.getInt(cursor.getColumnIndex(C_STATUS));
            user.role=cursor.getInt(cursor.getColumnIndex(C_ROLE));
        }
        cursor.close();
        return user;
    }

    public void updateScanTime(String device_id, long scan_time){
        ContentValues values = new ContentValues();
        values.put(C_SCAN_TIME, scan_time);
        dbHelper.getWritableDatabase().update(T_TASK, values, C_TASK_DEVICE_ID+"=?", new String[]{device_id});
    }


    @Override
    public void writeLog(String info) {

    }

    @Override
    public List<LogInfo> getLog(long from, long to) {
        return null;
    }
}

package phy.jsf

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONArray
import org.json.JSONObject
import phy.jsf.data.Task
import phy.jsf.data.User
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import phy.network.HttpSender
import phy.network.HttpSender.*
import phy.network.WpServer
import x.datautil.L
import x.frame.BaseService
import java.util.*
import kotlin.collections.ArrayList


class DataService : BaseService() {
    //总数量总页数和list的关系是？

    inner class DataProcess{
        var curPage:Int=0
        var totalCount:Int=0
    }

    var sync_state:Int=0
    val SYNC_USER:Int=1
    val SYNC_FORM:Int=2
    var SYNC_OVER=SYNC_USER or SYNC_FORM
    var SYNC_ERR=-1
    var action_sync=false
    companion object {
        public val ACTION_LOGIN="phy.jsf.ACTION_LOGIN"
        public val ACTION_PULL_DATA="phy.jsf.ACTION_PULL_DATA"
        public val ACTION_PUSH_TASK="phy.jsf.ACTION_PUSH_TASK"
        public val ACTION_UPDATE_SERVER="phy.jsf.ACTION_UPDATE_SERVER"
        public val ACTION_UPDATE_SERVER_OVER="phy.jsf.ACTION_UPDATE_SERVER_OVER"
        public val EXTRA_SERVER_UPDATE_RES="phy.jsf.EXTRA_SERVER_UPDATE_RES"
        public val EXTRA_TASK="phy.jsf.EXTRA_TASK"
        public val EXTRA_USERNAME="phy.jsf.EXTRA_USERNAME";
        public val EXTRA_PWD="phy.jsf.EXTRA_PWD";

        public val MSG_AUTO_SYNC=0
        
        private const val PAGE_SIZE:Int=32
        private const val CHANNEL_ONE_ID="phy.jsf.inspect.CH"
        private const val CHANNEL_ONE_NAME="同步数据"
    }


    lateinit var mHttpSender: HttpSender
    lateinit var mService:DataService

    var mHandler : Handler = @SuppressLint("HandlerLeak")
    object:Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if(msg!!.what==MSG_AUTO_SYNC){
                pullUserList()
                pullFormList()
                pushAllForm()
            }
        }
    }
    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if(ACTION_UPDATE_SERVER.equals(action)){
                sync_state=0
                action_sync=true
                pullUserList()
                pullFormList()
            }
            if(ACTION_PUSH_TASK.equals(action)){
                var task:Task=intent.getParcelableExtra(EXTRA_TASK)
                if(task!=null){
                    pushForm(task)
                }
            }
            else if(ACTION_LOGIN.equals(action)){
                var username:String =intent.getStringExtra(EXTRA_USERNAME)
                var pwd:String=intent.getStringExtra(EXTRA_PWD)
                login(username,pwd)
            }
        }
    }
    val mListener:HttpSender.HttpUpdateListener = HttpSender.HttpUpdateListener { http_type, mapOutput ->
        when {
            WpServer.URL_USER_LOGIN==http_type->{
                L.e("URL_USER_LOGIN get response.")

            }
            WpServer.URL_FORM_LIST == http_type -> {
                L.e("URL_FORM_LIST get response.")
                if(mapOutput.containsKey(LIST)){
                    var list= mapOutput[LIST] as ArrayList<Task>
                    sync_state=sync_state or SYNC_FORM
                    if(list!=null&&list.size>0){
                        for(task in list){
                            DbManager.getDbManager(mService).addTask(task,true)
                        }
                    }
                }else{
                    sync_state=SYNC_ERR
                }

            }
            WpServer.URL_USER_LIST == http_type -> {
                if(mapOutput.containsKey(LIST)){
                    sync_state=sync_state or SYNC_USER
                    var list= mapOutput[LIST] as ArrayList<User>
                    if(list!=null&&list.size>0){
                        L.e("URL_USER_LIST get response.")
                        for(user in list){
                            var exist=ArrayList<User>()
                            exist.clear()
                            DbManager.getDbManager(mService).getUserById(user.user_id, exist)
                            if(exist.size==0){
                                DbManager.getDbManager(mService).addUser(user)
                            }
                        }
                        var userList:ArrayList<User> = ArrayList()
                        DbManager.getDbManager(mService).getAllUser(userList)
                        for(user in userList){
                            L.e("user_id:${user.user_id},user_name:${user.user_name},pwd:${user.pwd}")
                        }
                    }
                }else{
                    sync_state=SYNC_ERR
                }
            }
            WpServer.URL_FORM_UPLOAD == http_type -> {
                L.e("URL_FORM_UPLOAD get response.")
                if(mapOutput.containsKey(CODE)){
                    var code=mapOutput.get(CODE) as Int
                    if(code==200){
                        if(mapOutput.containsKey(SOURCE_REQ_DATA)){
                            var obj:JSONObject =mapOutput.get(SOURCE_REQ_DATA) as JSONObject
                            var arr:JSONArray=obj.getJSONArray(CONTENTS)
                            for(i in 0 until arr.length()){
                                var jox:JSONObject= arr[i] as JSONObject
                                var task_id=jox.getString(PLAN_ID)
                                DbManager.getDbManager(mService).set_upload_complete_task(task_id)
                            }
                        }
                    }
                }
//                var mapobj:JSONObject=JSONObject(map)
//                var ja:JSONArray=JSONArray()
//                ja.put(mapobj)
//                var httpobj:JSONObject =JSONObject()
//                httpobj.put(CONTENTS,ja)
            }

        }
        action_sync=false;
        if(sync_state==SYNC_OVER){
            var intent=Intent(ACTION_UPDATE_SERVER_OVER)
            intent.putExtra(EXTRA_SERVER_UPDATE_RES,true)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }else if(sync_state==SYNC_ERR){
            var intent=Intent(ACTION_UPDATE_SERVER_OVER)
            intent.putExtra(EXTRA_SERVER_UPDATE_RES,false)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_PUSH_TASK)
        intentFilter.addAction(ACTION_LOGIN)
        intentFilter.addAction(ACTION_UPDATE_SERVER)
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter)

    }

    /**
     * for handler msg.
     */
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)


    }

    private fun pushAllForm(){
        var tasks:ArrayList<Task> = ArrayList()
        DbManager.getDbManager(mService).getAllUnUploadTask(tasks)
        if(tasks.size!=0){
            var ja:JSONArray=JSONArray()
            var httpobj:JSONObject =JSONObject()
            httpobj.put(CONTENTS,ja)

            for(task in tasks){
                var map: HashMap<String,Any> =HashMap<String, Any>(12)
                map[HttpSender.FORM_ID]=task.form_id
                map[HttpSender.PLAN_ID] = task.task_id
                map[HttpSender.USER_ID] = task.user_id//task.user_id
                if(!TextUtils.isEmpty(task.check_user)){
                    map[HttpSender.CHECK_USER] = task.check_user
                }
                if(task.commit_time !=  0.toLong()){
                    map[HttpSender.COMMIT_TIME] = Settings.SDF_DATE_TIME.format( task.commit_time)
                }
                if(task.check_time != 0.toLong()){
                    map[HttpSender.CHECK_TIME] = Settings.SDF_DATE_TIME.format( task.check_time)
                }
                map[HttpSender.STATUS] = task.state
                map[HttpSender.CONTENT] = task.edit_content    //JSONObject(task.edit_content)
                map[HttpSender.CREATE_TIME] =Settings.SDF_DATE_TIME.format(task.create_time)
                map[HttpSender.UPDATE_TIME] = Settings.SDF_DATE_TIME.format(Date())
                var mapobj:JSONObject=JSONObject(map)
                ja.put(mapobj)
            }
            mHttpSender.request(WpServer.URL_FORM_UPLOAD,httpobj,httpobj,mListener)
        }
    }

    private fun login( name:String,pwd:String ){
        var map: HashMap<String,Any> =HashMap<String, Any>(2)
        map[HttpSender.USERNAME] = name
        map[HttpSender.PWD] = pwd
        mHttpSender.request(WpServer.URL_USER_LOGIN,map,map,mListener)
    }

    private fun pushForm(task:Task){
        var map: HashMap<String,Any> =HashMap<String, Any>(12)
        map[HttpSender.FORM_ID]=task.form_id
        map[HttpSender.PLAN_ID] = task.task_id
        map[HttpSender.USER_ID] = task.user_id//task.user_id
        if(!TextUtils.isEmpty(task.check_user)){
            map[HttpSender.CHECK_USER] = task.check_user

        }
        if(task.commit_time !=  0.toLong()){
            map[HttpSender.COMMIT_TIME] = Settings.SDF_DATE_TIME.format( task.commit_time)
        }
        if(task.check_time !=  0.toLong()){
            map[HttpSender.CHECK_TIME] = Settings.SDF_DATE_TIME.format( task.check_time)
        }
        map[HttpSender.STATUS] = task.state
        map[HttpSender.CONTENT] = task.edit_content    //JSONObject(task.edit_content)
        map[HttpSender.CREATE_TIME] =Settings.SDF_DATE_TIME.format(task.create_time)
        map[HttpSender.UPDATE_TIME] = Settings.SDF_DATE_TIME.format(Date())
        var mapobj:JSONObject=JSONObject(map)
        var ja:JSONArray=JSONArray()
        ja.put(mapobj)
        var httpobj:JSONObject =JSONObject()
        httpobj.put(CONTENTS,ja)

        mHttpSender.request(WpServer.URL_FORM_UPLOAD,httpobj,httpobj,mListener)
    }
    private fun pullFormList(){
       var map: HashMap<String,Any> =HashMap<String, Any>(2)
        map[HttpSender.CUR_PAGE] = 1
        map[HttpSender.PAGE_SIZE] = PAGE_SIZE
        mHttpSender.request(WpServer.URL_FORM_LIST,map,map,mListener)
    }
    private fun pullUserList(){
        var map: HashMap<String,Any> =HashMap<String, Any>(2)
        map[HttpSender.CUR_PAGE] = 1
        map[HttpSender.PAGE_SIZE] = PAGE_SIZE
        mHttpSender.request(WpServer.URL_USER_LIST,map,map,mListener)
    }

    lateinit var mNotification:Notification
    private fun createNotification(){
        var buidler:Notification.Builder= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.setLightColor(Color.RED)
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
            Notification.Builder(this,CHANNEL_ONE_ID)
        } else {
            Notification.Builder(this)
        }
        buidler.setTicker("SCP")
        buidler.setContentTitle("蓝汛维保")
        buidler.setContentText("正在同步数据...")
        mNotification=buidler.build()
        startForeground(1, mNotification) // 通知栏标识符 前台进程对象唯一ID
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        mHttpSender= HttpSender(this)
        mService=this
        registerReceiver()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 注意notification也要适配Android 8 哦
            createNotification()

        }
        pullFormList()
        pullUserList()
        pushAllForm()
        auto_sync()
    }

    var timer:Timer=Timer()
    var timerTask=object: TimerTask() {
        override fun run() {
            mHandler.sendEmptyMessage(MSG_AUTO_SYNC)
        }
    }

    fun auto_sync(){
        timer.schedule(timerTask,300*1000,300*1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


}

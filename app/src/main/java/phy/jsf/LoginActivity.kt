package phy.jsf


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.login_layout.*
import phy.jsf.DataService.Companion.ACTION_UPDATE_SERVER
import phy.jsf.DataService.Companion.EXTRA_SERVER_UPDATE_RES
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import x.datautil.L
import x.dialog.AlertDialog
import x.frame.BaseActivity


class LoginActivity : BaseActivity() {
    var loginName: String? = null
    var codeTimer: Runnable? = null
    var delayTime: Int = 0
    var progressDialog :AlertDialog?=null
   // lateinit var et_server_url:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        //et_server_url=findViewById(R.id.et_server_url)
        et_server_url.setText(Settings.server_ip)

        btn_login.setOnClickListener {
                checkLogin()
                return@setOnClickListener
        }
        btn_server_ip.setOnClickListener{
            checkServer()
        }
       val intentFilter = IntentFilter()
       intentFilter.addAction(DataService.ACTION_UPDATE_SERVER_OVER)
       LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter)
    }
    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var res=intent!!.getBooleanExtra(EXTRA_SERVER_UPDATE_RES,false)
            if (progressDialog != null){
                progressDialog!!.dismiss()
            }
            Toast.makeText(mActivityReference.get(),if(res)  R.string.sync_ok else R.string.sync_err,Toast.LENGTH_SHORT).show()
        }
    }
    fun encrypt(pwd:String):String{
        val key = 13
        // 将字符串转为字符数组
        // 将字符串转为字符数组
        val chars: CharArray = pwd.toCharArray()
        val sb = StringBuilder()
        // 遍历数组
        // 遍历数组
        for (aChar in chars) {
            // 获取字符的ASCII编码
            var asciiCode = aChar.toInt()
            // 偏移数据
            asciiCode += key
            // 将偏移后的数据转为字符
            val result = asciiCode.toChar()
            // 拼接数据
            sb.append(result)
        }
        return sb.toString()
    }

   private fun checkLogin() {
        var login_name = et_phone.text.toString()
        var pwd=et_code.text.toString()
       if(TextUtils.isEmpty(login_name)){
           Toast.makeText(this,resources.getString(R.string.hint_input_phone), Toast.LENGTH_SHORT).show()
       }else if(TextUtils.isEmpty(pwd)){
           Toast.makeText(this,resources.getString(R.string.hint_input_verification_code), Toast.LENGTH_SHORT).show()
       }else{
            pwd=encrypt(pwd)
           L.e("login_name:${login_name}encrypt pwd:${pwd}")
           var user = DbManager.getDbManager(this).login_check(login_name,pwd)
           if(user!=null){
               Settings.curUser=user

               var loginAction= Intent(this,DataService::class.java)
               loginAction.setAction(DataService.ACTION_LOGIN)
               loginAction.putExtra(DataService.EXTRA_USERNAME,login_name)
               loginAction.putExtra(DataService.EXTRA_PWD,pwd)
               LocalBroadcastManager.getInstance(this).sendBroadcast(loginAction)

               val intent= Intent(this,MainActivity::class.java)
               startActivity(intent)
           }else{
               Toast.makeText(this,resources.getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
           }
       }
    }

    fun checkServer(){
        var server_ip=et_server_url.text.toString()
        if(!TextUtils.isEmpty(server_ip)){
            Settings.server_ip=  server_ip
            Settings.apply(this)
            //update server
            var intent=Intent(ACTION_UPDATE_SERVER)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            showProgress()
        }
    }

    fun showProgress(){
        if (progressDialog == null) {
            progressDialog = AlertDialog(this, R.style.base_dialog)
            val root: View = LayoutInflater.from(this).inflate(R.layout.dialog_progress_inverse, null)
            val tv_msg = root.findViewById<View>(R.id.tv_msg) as TextView
            tv_msg.setText(R.string.please_wait)
            progressDialog!!.setView(root)
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (codeTimer != null) {
            mSecureHandler.removeCallbacks(codeTimer)
        }
    }

}
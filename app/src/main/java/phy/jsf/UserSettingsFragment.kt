package phy.jsf

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.user_settings.*
import phy.jsf.db.Settings
import x.datautil.L
import x.dialog.AlertDialog
import x.frame.BaseActivity
import x.frame.BaseFragment
import java.util.*
import kotlin.math.log

class UserSettingsFragment: BaseFragment(), BaseActivity.OnAction {

    var progressDialog :AlertDialog?=null

    override fun onGetAction(intent: Intent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.user_settings, container, false)
        setView(root)
        return root
    }
    fun setView(root:View){
        var btn_logout=root.findViewById<Button>(R.id.btn_logout)
        btn_logout.setOnClickListener(View.OnClickListener {
            var login=Intent(mActivity,LoginActivity::class.java)
            startActivity(login)
            mActivity.finish()
        })

        var btn_sync=root.findViewById<Button>(R.id.btn_sync)
        btn_sync.setOnClickListener(View.OnClickListener {
            var intent=Intent(DataService.ACTION_UPDATE_SERVER)
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent)
            showProgress()
        })
        var userInfo:String= String.format(Locale.US, getString(R.string.dis_info),
                Settings.curUser.user_name,
                if(TextUtils.isEmpty(Settings.curUser.dis_name)) "" else Settings.curUser.dis_name,
                if(TextUtils.isEmpty(Settings.curUser.job_code)) "" else Settings.curUser.job_code
        )
        var tv_user_info=root.findViewById<TextView>(R.id.tv_user_info)
        tv_user_info.text=userInfo
        val intentFilter = IntentFilter()
        intentFilter.addAction(DataService.ACTION_UPDATE_SERVER_OVER)
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mReceiver, intentFilter)

    }
    fun showProgress(){
        if (progressDialog == null) {
            progressDialog = AlertDialog(mActivity, R.style.base_dialog)
            val root: View = LayoutInflater.from(mActivity).inflate(R.layout.dialog_progress_inverse, null)
            val tv_msg = root.findViewById<View>(R.id.tv_msg) as TextView
            tv_msg.setText(R.string.please_wait)
            progressDialog!!.setView(root)
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.show()
    }
    var last_sync_time:Long=0
    var last_sync_res=false
    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var res=intent!!.getBooleanExtra(DataService.EXTRA_SERVER_UPDATE_RES,false)
            if (progressDialog != null){
                progressDialog!!.dismiss()
            }
            if(System.currentTimeMillis()-last_sync_time>1000*60*3||last_sync_res!=res){
                last_sync_time=System.currentTimeMillis()
                last_sync_res=res
                Toast.makeText(mActivity,if(res)  R.string.sync_ok else R.string.sync_err, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
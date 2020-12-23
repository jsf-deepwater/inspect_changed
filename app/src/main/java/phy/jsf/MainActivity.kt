package phy.jsf

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.RadioGroup
import com.yanzhenjie.permission.Permission
import x.datautil.L
import x.frame.BaseActivity
import x.permission.PermissionManager

class MainActivity : BaseActivity() {
    companion object {

    }

    private lateinit var mReceiver: BroadcastReceiver
    private var pd: x.dialog.AlertDialog? = null
    private lateinit var group_bar:RadioGroup
    val on_checked_changed_listener:RadioGroup.OnCheckedChangeListener= RadioGroup.OnCheckedChangeListener{ radioGroup: RadioGroup, i: Int ->
        when (i) {
            R.id.rb_main -> showFragment(TaskListFragment::class.java, null)
            R.id.rb_chart ->showFragment(PieFragment::class.java, null)
            R.id.rb_user -> {
                //requestCamera()
                showFragment(UserSettingsFragment::class.java, null)
            }


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_control_layout)
        setView()
        showFragment(TaskListFragment::class.java, null)
    }
    fun setView() {
        group_bar = findViewById<RadioGroup>(R.id.group_bar)
        group_bar.setOnCheckedChangeListener(on_checked_changed_listener)
    }
    fun requestCamera(){
        requestPermission(object : PermissionManager.PermissionRes {
            override fun onSucceed() {
                showFragment(QR_ScanFragment::class.java, null)
            }
            override fun onFailed() {
                L.e("Camera permission request failed.")
            }
        }, Permission.Group.CAMERA, Permission.Group.STORAGE)
    }
    fun setScanRes(scanTxt:String){
        L.e("MainActivity,setScanRes...")
        getBackStack(null)
        Log.e("TAG", "curFragment:333" + curFragment.javaClass.name)
        group_bar.setOnCheckedChangeListener(null)
        group_bar.check(0)
        group_bar.setOnCheckedChangeListener(on_checked_changed_listener)
        Log.e("TAG", "curFragment:555" + curFragment.javaClass.name)
//        var taskListFragment=showFragment(TaskListFragment::class.java, null, false) as TaskListFragment
        var taskListFragment=curFragment as TaskListFragment
        taskListFragment.searchTask(scanTxt)

    }


    private fun toLogin() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }
}

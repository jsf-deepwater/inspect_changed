package phy.jsf

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONArray
import org.json.JSONObject
import phy.jsf.DataService.Companion.ACTION_PUSH_TASK
import phy.jsf.DataService.Companion.EXTRA_TASK
import phy.jsf.QRScanActivity.Companion.EXTRA_SCAN_RESULT
import phy.jsf.data.Task
import phy.jsf.data.User
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import phy.jsf.util.ViewLoader
import phy.network.HttpSender.*
import x.datautil.L
import x.frame.BaseActivity
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : BaseActivity() {
    companion object{
        public const val ACTION_EDIT_FORM="ACTION_EDIT_FORM"
    }
    lateinit var ll_content:LinearLayout
    lateinit var bt_commit:Button
    lateinit var bt_err_commit:Button
    lateinit var bt_cache_commit:Button
    lateinit var sp_manager:Spinner
    lateinit var task:Task
    lateinit var spinner_manager:Spinner
    var manager_id:String? = null
    var managers:ArrayList<User> = ArrayList()
    var viewList:ArrayList<ViewLoader> = ArrayList()
    var scanQRstate=false
    val REQUEST_QR_SCAN = 1000

    var scanBtn:View?=null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_QR_SCAN&&resultCode==RESULT_OK){
            // 只需要扫码一次
            if(task.scan_time!=0L){
                scanQRstate=true
                btn_scan_res(scanBtn)
                return
            }
            if(data!=null){L.e("scan did:${data!!.getStringExtra(EXTRA_SCAN_RESULT)}")}
            if(data!=null&&task.device_id.equals(data!!.getStringExtra(EXTRA_SCAN_RESULT))){
                // 更新第一次扫码时间
                DbManager.getDbManager(this).updateScanTime(task.device_id, System.currentTimeMillis());
                scanQRstate=true
                btn_scan_res(scanBtn)
            }else{
                Toast.makeText(this,R.string.scan_did_err,Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_content)
        task=intent.getParcelableExtra<Task>(ACTION_EDIT_FORM)
        ll_content=findViewById(R.id.ll_content)
        bt_commit=findViewById(R.id.bt_commit)
        bt_err_commit=findViewById(R.id.bt_err_commit)
        bt_cache_commit=findViewById(R.id.bt_cache_commit)
        sp_manager=findViewById(R.id.sp_manager)
        bt_commit.setOnClickListener(bt_listener)
        bt_err_commit.setOnClickListener(bt_listener)
        bt_cache_commit.setOnClickListener(bt_listener)
        viewInit()
    }

    fun btn_scan_res(it:View?){
        if(it!=null){
            if(it==bt_err_commit&&TextUtils.isEmpty(manager_id)){
                showManagerSpinner()
            }else{
                var jo:JSONObject=JSONObject()
                var ja:JSONArray= JSONArray()
                for(loader in viewList){
                    if(loader.`val`!=null){
                        ja.put(loader.`val`)
                    }
                }
                jo.put(CONTENT_VAL,ja)
                L.e("FormRes:${jo.toString()}")
                task.edit_content=jo.toString()
                if(TextUtils.isEmpty(task.user_id)){
                    task.user_id=Settings.curUser.user_id
                }
                if(task.create_time==0.toLong()){
                    task.create_time=System.currentTimeMillis()
                }
                if(it==bt_err_commit){
                    task.state= Settings.TASK_ERR
                    task.check_user=manager_id
                }else if(it==bt_cache_commit){
                    task.state=Settings.TASK_CACHE
                }else if(it==bt_commit){task.state
                    task.state=Settings.TASK_COMMIT
                    if(Settings.curUser.role==Settings.PERM_ADMIN){
                        task.state=Settings.TASK_ERR_OVER
                        task.check_user=Settings.curUser.user_id
                        task.check_time=System.currentTimeMillis()
                    }
                }
                update_task(task)
                var upAction= Intent(this,DataService::class.java)
                upAction.setAction(ACTION_PUSH_TASK)
                upAction.putExtra(EXTRA_TASK,task)
                LocalBroadcastManager.getInstance(this).sendBroadcast(upAction)
                finish()
            }

        }
    }

    var bt_listener: View.OnClickListener=View.OnClickListener {
        L.e("prepare scan,target did:${task.device_id}")
        scanBtn=it
        if(!scanQRstate){
            //open scan qr page.
            var intent=Intent()
            intent.setClass(this,QRScanActivity::class.java)
            startActivityForResult(intent,REQUEST_QR_SCAN)
            return@OnClickListener
        }else{
            btn_scan_res(it)
        }
    }

    var sp_listener:AdapterView.OnItemSelectedListener=object: AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {
            manager_id=null
            spinner_manager.visibility=View.GONE
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            manager_id=managers[position].user_id
        }
    }

    fun showManagerSpinner(){
        sp_manager.visibility=View.VISIBLE
        DbManager.getDbManager(this).getManagerUser(managers)
        if(managers!=null&&managers.size!=0){
            var nameList:ArrayList<String> =ArrayList()
            for(user in managers){
                nameList.add(user.dis_name)
            }
            var adapter:ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_spinner_item,nameList)
            sp_manager.adapter=adapter
            sp_manager.onItemSelectedListener=sp_listener

        }
    }

    fun update_task(task:Task){
        task.upload_state=0
        DbManager.getDbManager(this).addTask(task,false)
    }

    fun viewInit(){
        if(task!=null){
            //create view.
            var json:JSONObject=JSONObject(task.content)
            var edit:JSONObject?=null
            if(task.edit_content!=null){
                edit = JSONObject(task.edit_content)
            }
            if(json!=null){
                L.e("Context:${json.toString()}")
                if(edit!=null)L.e("editContext:${edit.toString()}")
                //rows
                var rows=json.getJSONArray(ROWS)
                if(rows!=null&&rows.length()!=0){
                    for(i in 0 until rows.length()){
                        L.e("new rows.")
                        var columns=rows.get(i) as JSONArray
                        //columns.
                        if(columns!=null&&columns.length()!=0){
                            var cnt=columns.length()
                            var layout_content=ll_content
                            //check multiple lines.
                            var multLine=false
                            for(j in 0 until columns.length()){
                                //column.
                                L.e("new columns.")
                                var column=columns.getJSONObject(j)
                                if(column!=null){
                                    if(ViewLoader.typeTextArea.equals(column.getString(CONTENT_TYPE))){
                                        multLine=true
                                        break
                                    }
                                }
                            }
                            layout_content=ViewLoader.loadChildLayout(this,ll_content,multLine)
                            for(j in 0 until  columns.length()){
                                //column.
                                var column=columns.getJSONObject(j)
                                if(column!=null){
                                    var loader:ViewLoader= ViewLoader()
                                    loader.loadView(column,edit,this,layout_content,multLine)
                                    viewList.add(loader)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
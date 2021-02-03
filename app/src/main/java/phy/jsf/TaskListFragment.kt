package phy.jsf

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONObject
import phy.jsf.WebViewFragment.Companion.EXTRAL_TASK_ITEM
import phy.jsf.data.Task
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import phy.jsf.db.Settings.*
import x.datautil.L
import x.dialog.AlertDialog
import x.frame.BaseActivity
import x.frame.BaseFragment
import java.util.*
import kotlin.collections.ArrayList


class TaskListFragment:BaseFragment(),BaseActivity.OnAction{
    val REQUEST_QR_SCAN = 1000
    var  taskList:ArrayList<Task> = ArrayList()
    lateinit var tv_none:TextView
    lateinit var lv_tasks:ListView
    lateinit var sv_task: SearchView
    var queryStr:String?=null
    var curTask:Task?=null
//    lateinit var sv_none:ScrollView
//    lateinit var sv_listener: SearchView.OnQueryTextListener
    lateinit var taskAdapter:TaskAdapter
    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.task_list, container, false)
        setView(root)
        return root
    }

    override fun onVisible() {
        super.onVisible()
        searchTask(queryStr)
    }

    fun setView(root:View){
        L.e("TaskListFragment,setView...")
        taskAdapter=TaskAdapter(taskList)
        lv_tasks=root.findViewById<ListView>(R.id.lv_tasks)
        lv_tasks.adapter=taskAdapter
        lv_tasks.onItemClickListener = lv_listener
        sv_task=root.findViewById(R.id.sv_task)
        sv_task.setIconifiedByDefault(true)
        sv_task.isSubmitButtonEnabled=true
        sv_task.setOnQueryTextListener(sv_listener)
        //sv_task.setOnFocusChangeListener(sv_focus_listener)
        tv_none=root.findViewById<TextView>(R.id.tv_none)
        tv_none.isClickable=true
        tv_none.setOnClickListener(tv_Listener)
        var btn_search=root.findViewById<Button>(R.id.btn_search)
        btn_search.setOnClickListener(View.OnClickListener {
            show_search_dialog()
        })
    }

    var dialogFilter:AlertDialog?=null

    fun listFilter(sourceList:ArrayList<Task>,filterList:ArrayList<Task>):ArrayList<Task>{
        if(sourceList.size!=0&&filterList.size!=0){
            var lastIndex=sourceList.size-1
            for(i in lastIndex downTo 0){
                var find=false
                for(task in filterList){
                    if(sourceList[i].form_id == task.form_id){
                        find=true
                        break
                    }
                }
                if(!find){
                    sourceList.removeAt(i)
                }
            }
        }
        return sourceList
    }

    lateinit var tv_d_id_str:EditText
    lateinit var tv_d_name_str:EditText
    lateinit var tv_task_name_str:EditText
    lateinit  var tv_d_building_str:EditText
    lateinit var tv_d_floor_str:EditText
    lateinit var tv_d_room_str:EditText
    lateinit var rg_state:RadioGroup
    lateinit var rg_type:RadioGroup
    lateinit var btn_confirm:Button

    fun add_or_filter(filterEnd:Boolean,sourceList:ArrayList<Task>,filterList:ArrayList<Task>):Boolean{
        if(!filterEnd){
            if(sourceList.size==0){
                sourceList.addAll(filterList)
                return false
            }else{
                listFilter(sourceList,filterList)
                return (sourceList.size == 0)
            }
        }
        return filterEnd
    }

    fun show_search_dialog(){

        if(dialogFilter==null){
            dialogFilter = AlertDialog(mActivity, R.style.base_dialog)
            val view: View = LayoutInflater.from(mActivity).inflate(R.layout.search_dialog, null)
            dialogFilter!!.setView(view)
            tv_d_id_str=view.findViewById<EditText>(R.id.tv_d_id_str)
            tv_d_name_str=view.findViewById<EditText>(R.id.tv_d_name_str)
            tv_task_name_str=view.findViewById<EditText>(R.id.tv_task_name_str)
            tv_d_building_str=view.findViewById<EditText>(R.id.tv_d_building_str)
            tv_d_floor_str=view.findViewById<EditText>(R.id.tv_d_floor_str)
            tv_d_room_str=view.findViewById<EditText>(R.id.tv_d_room_str)
            rg_state=view.findViewById<RadioGroup>(R.id.rg_state)
            rg_type=view.findViewById<RadioGroup>(R.id.rg_type)
            btn_confirm=view.findViewById<Button>(R.id.bt_commit)
            btn_confirm.setOnClickListener {
                var device_name_str=tv_d_name_str.text.toString()
                var device_id_str=tv_d_id_str.text.toString()
                var task_name_str=tv_task_name_str.text.toString()
                var building_str=tv_d_building_str.text.toString()
                var floor_str=tv_d_floor_str.text.toString()
                var room_str=tv_d_room_str.text.toString()
                var stateId=-1
                var type=-1

                val typeCnt: Int = rg_type.getChildCount()
                for (i in 0 until typeCnt) {
                    val rb = rg_type.getChildAt(i) as RadioButton
                    if (rb.isChecked) {
                        type=i
                        break
                    }
                }
                val stateCnt: Int = rg_state.getChildCount()
                for (i in 0 until stateCnt) {
                    val rb = rg_state.getChildAt(i) as RadioButton
                    if (rb.isChecked) {
                        stateId=i
                        break
                    }
                }

                //search by type.
                taskList.clear()
                var filterEnd=false
                var searchList=ArrayList<Task>()
                if(type != -1){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByFormType(type+1, searchList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(stateId!= -1){
                    //延期
                    searchList.clear()
                    if(stateId<4){
                        DbManager.getDbManager(mActivity).getTaskByFormState(stateId, searchList)
                    }else{
                        DbManager.getDbManager(mActivity).getDelayTask(searchList)
                    }
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(device_name_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(device_name_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(device_id_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(device_id_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(task_name_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(task_name_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(building_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(building_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(floor_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(floor_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }
                if(!TextUtils.isEmpty(room_str)){
                    searchList.clear()
                    DbManager.getDbManager(mActivity).getTaskByLike(room_str, taskList)
//                    listFilter(taskList,searchList)
                    filterEnd=add_or_filter(filterEnd,taskList,searchList)
                }

                for(task in taskList){
                    L.e("task id:${task.task_id},state:${task.state},content:${task.edit_content},up_state:${task.upload_state}")
                }
                taskAdapter.notifyDataSetChanged()
                if(taskList.size==0){
                    tv_none.visibility=View.VISIBLE
                }else{
                    tv_none.visibility=View.GONE
                }
                dialogFilter!!.dismiss()
            }
        }
        tv_d_id_str.text.clear()
        tv_d_name_str.text.clear()
        tv_task_name_str.text.clear()
        tv_d_building_str.text.clear()
        tv_d_floor_str.text.clear()
        tv_d_room_str.text.clear()
        rg_state.clearCheck()
        rg_type.clearCheck()
        dialogFilter!!.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_QR_SCAN&&resultCode== BaseActivity.RESULT_OK){
            if(data!=null){
                L.e("scan did:${data!!.getStringExtra(QRScanActivity.EXTRA_SCAN_RESULT)}")
                var json= JSONObject(data!!.getStringExtra(QRScanActivity.EXTRA_SCAN_RESULT))
                if (curTask!!.device_id.equals(json.get("code"))){
                    enable_edit_page(curTask!!)
                    return ;
                }
            }
            Toast.makeText(mActivity,R.string.scan_did_err,Toast.LENGTH_SHORT).show()
        }
    }

    fun enable_edit_page(task:Task){
//                var edit:Intent=Intent(mActivity,EditActivity::class.java)
//                edit.putExtra(EditActivity.ACTION_EDIT_FORM,task)
//                mActivity.startActivity(edit)'
        var intent=Intent()
        intent.setAction(WebViewFragment.ACTION_FORM)
        intent.putExtra(EXTRAL_TASK_ITEM,task)
        showFragment(WebViewFragment::class.java,intent,false)
    }
    /*val sv_focus_listener:View.OnFocusChangeListener= View.OnFocusChangeListener{v,has_focus->
        if(has_focus){
            mActivity.imm.showSoftInput(v,InputMethodManager.SHOW_FORCED)
        }else{
            mActivity.imm.hideSoftInputFromWindow(v.windowToken,0)
        }
    }*/
    val lv_listener:AdapterView.OnItemClickListener=AdapterView.OnItemClickListener { parent, view, position, id ->
        var task= taskList[position]
        if(task.state==TASK_ERR && Settings.curUser.role==Settings.PERM_WATCH){
            Toast.makeText(mActivity,R.string.no_perm_read_err,Toast.LENGTH_SHORT).show()
        }else{
            var exec=checkExecTime(task)
            if(exec){
                curTask=task
                L.e("Device_id:${curTask!!.device_id}")
                //scan
                var intent=Intent()
                intent.setClass(mActivity,QRScanActivity::class.java)
                startActivityForResult(intent,REQUEST_QR_SCAN)
            }else{
                Toast.makeText(mActivity,R.string.time_err,Toast.LENGTH_SHORT).show()
            }
        }
    }

    val tv_Listener:View.OnClickListener= android.view.View.OnClickListener{
        L.e("textview click.")
        sv_task.setQuery("",false)
        searchTask(null)
    }

    val sv_listener: SearchView.OnQueryTextListener=object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            queryStr=query
            searchTask(queryStr)
            sv_task.clearFocus()                //可以收起键盘
            sv_task.onActionViewCollapsed()    //可以收起SearchView视图
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }
    }

    fun checkExecTime(task :Task):Boolean{
        val DAY_START=3600*9
        var NIGHT_START=3600*18
        var curTime=(System.currentTimeMillis()/1000+8*3600)%(3600*24)

//        var time=Settings.SDF_DATE_TIME.format(System.currentTimeMillis())
//        L.e("time:${time}")
        var curDate=Settings.SDF_DATE.format(System.currentTimeMillis())
        var planDate=Settings.SDF_DATE.format(task.scheduler_time)

        if(curDate >= planDate){
            if(Settings.TASK_DAY==task.form_day_night){
                if(curTime in DAY_START..NIGHT_START){
                    return true
                }
            }else if(Settings.TASK_NIGHT==task.form_day_night){
                if(curTime>=NIGHT_START||curTime<=DAY_START){
                    return true
                }
            }
        }
        return false
    }

     fun getDidFromScanRes(scanResTxt:String?):String?{
        return scanResTxt
    }

    fun searchTask(scanResTxt:String?){
        L.e("TaskListFragment,searchByQRScanRes:$scanResTxt")

        var did=getDidFromScanRes(scanResTxt)
        if(TextUtils.isEmpty(scanResTxt)){
            L.e("get all task")
            DbManager.getDbManager(mActivity).getAllTask(taskList,Settings.curUser)
        }else{
//            L.e("get  task by did:$did")
//            DbManager.getDbManager(mActivity).getTaskByDeviceId(did, taskList)
            //search by type.
            taskList.clear()
            Settings.TASK_TYPE.forEachIndexed { index, s ->
                if(scanResTxt.equals(s)){
                    DbManager.getDbManager(mActivity).getTaskByFormType(index+1, taskList)
                }
            }
            //search by state.
            var state = -1
            if(scanResTxt.equals(resources.getString(R.string.type_unstart))){
                state=Settings.TASK_UNSTART
            }else if(scanResTxt.equals(resources.getString(R.string.type_uncommit))){
                state=Settings.TASK_CACHE
            }else if(scanResTxt.equals(resources.getString(R.string.type_err))){
                state=Settings.TASK_ERR
            }else if(scanResTxt.equals(resources.getString(R.string.type_complete))){
                state= Settings.TASK_COMMIT
            }
            if(state>=0){
                DbManager.getDbManager(mActivity).getTaskByFormState(state, taskList)
            }
            if(scanResTxt.equals(resources.getString(R.string.type_delay))){
                DbManager.getDbManager(mActivity).getDelayTask(taskList)
            }
            //other.
            DbManager.getDbManager(mActivity).getTaskByLike(scanResTxt, taskList)

        }
        for(task in taskList){
            L.e("task id:${task.task_id},state:${task.state},content:${task.edit_content},up_state:${task.upload_state}")
        }

        taskAdapter.notifyDataSetChanged()
        if(taskList.size==0){
            tv_none.visibility=View.VISIBLE
        }else{
            tv_none.visibility=View.GONE
        }
    }

    override fun onGetAction(intent: Intent?) {
        TODO("Not yet implemented")
    }

    inner class TaskAdapter(var taskList: ArrayList<Task>) :BaseAdapter(){
        var today=Date().time
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var mView=convertView
            if(mView==null){
                mView = LayoutInflater.from(mActivity).inflate(R.layout.task_item, parent, false)
            }
            var tv_index=mView!!.findViewById<TextView>(R.id.tv_index)
            var tv_d_id_str=mView!!.findViewById<TextView>(R.id.tv_d_id_str)
            var tv_d_name_str=mView!!.findViewById<TextView>(R.id.tv_d_name_str)
            var tv_task_type_str=mView!!.findViewById<TextView>(R.id.tv_task_type_str)
            var tv_task_name_str=mView!!.findViewById<TextView>(R.id.tv_task_name_str)
            var tv_scheduler_time_str=mView!!.findViewById<TextView>(R.id.tv_scheduler_time_str)
            var tv_complete_time_str=mView!!.findViewById<TextView>(R.id.tv_complete_time_str)
            var tv_task_day_night=mView!!.findViewById<TextView>(R.id.tv_task_day_night)
            var tv_task_state=mView!!.findViewById<TextView>(R.id.tv_task_state)
            /*if(taskList[position].state==TASK_ERR){
                tv_task_state.visibility=View.VISIBLE
            }else{
                tv_task_state.visibility=View.GONE
            }*/
            if(taskList[position].state==TASK_ERR){
                tv_task_state.setText(R.string.type_err)
            }else if(taskList[position].state==TASK_UNSTART){
                tv_task_state.setText(R.string.type_unstart)
            }else if(taskList[position].state==TASK_CACHE){
                tv_task_state.setText(R.string.type_uncommit)
            }else{
                tv_task_state.setText(R.string.type_complete)
            }
            tv_index.text= (position+1).toString()
            tv_d_id_str.text=taskList[position].device_id
            tv_d_name_str.text=taskList[position].device_name
            tv_task_type_str.text= Settings.TASK_TYPE[taskList[position].form_type-1]
            tv_task_name_str.text=taskList[position].form_name
            tv_scheduler_time_str.text=Settings.SDF_DATE.format(taskList[position].scheduler_time)
            if(taskList[position].commit_time==0L || taskList[position].check_time==0L){
                tv_complete_time_str.text=resources.getText(R.string.type_unstart)
            }else if (taskList[position].commit_time>taskList[position].check_time){
                tv_complete_time_str.text=Settings.SDF_DATE.format(taskList[position].commit_time)
            }else{
                tv_complete_time_str.text=Settings.SDF_DATE.format(taskList[position].check_time)
            }
            /*if(taskList[position].create_time!=(0.toLong())){
                tv_complete_time_str.text=Settings.SDF_DATE.format(taskList[position].create_time)
            }else{
                tv_complete_time_str.text=resources.getText(R.string.type_unstart)
            }*/


            if(Settings.TASK_DAY==taskList[position].form_day_night){
                tv_task_day_night.text=resources.getText(R.string.day_work)
            }else{
                tv_task_day_night.text=resources.getText(R.string.night_work)
            }

            /*
            if(today<taskList[position].scheduler_time){//not start.
                tv_task_state.text=resources.getText(R.string.type_unstart)
            }else if (today.compareTo(taskList[position].scheduler_time)>0){
                tv_task_state.text=resources.getText(R.string.type_delay)
            }else{//today
                tv_task_state.text=resources.getText(R.string.type_uncomplete)
            }
            */
            return mView!!
        }


        override fun getItem(position: Int): Any {
            return taskList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return taskList.size
        }

    }
}

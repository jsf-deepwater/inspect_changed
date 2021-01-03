package phy.jsf

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import phy.jsf.WebViewFragment.Companion.EXTRAL_TASK_ITEM
import phy.jsf.data.Task
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import phy.jsf.db.Settings.TASK_ERR
import x.datautil.L
import x.frame.BaseActivity
import x.frame.BaseFragment
import java.util.*
import kotlin.collections.ArrayList

class TaskListFragment:BaseFragment(),BaseActivity.OnAction{

    var  taskList:ArrayList<Task> = ArrayList()
    lateinit var tv_none:TextView
    lateinit var lv_tasks:ListView
    lateinit var sv_task: SearchView
    var queryStr:String?=null
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
//                var edit:Intent=Intent(mActivity,EditActivity::class.java)
//                edit.putExtra(EditActivity.ACTION_EDIT_FORM,task)
//                mActivity.startActivity(edit)'
                var bundle=Bundle()
                bundle.putParcelable(EXTRAL_TASK_ITEM,task)
                showFragment(WebViewFragment::class.java,bundle,false)
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
            L.e("querry.")
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
            L.e("get  task by did:$did")
            DbManager.getDbManager(mActivity).getTaskByDeviceId(did, taskList)
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
            if(taskList[position].state==TASK_ERR){
                tv_task_state.visibility=View.VISIBLE
            }else{
                tv_task_state.visibility=View.GONE
            }
            tv_index.text= (position+1).toString()
            tv_d_id_str.text=taskList[position].device_id
            tv_d_name_str.text=taskList[position].device_name
            tv_task_type_str.text= Settings.TASK_TYPE[taskList[position].form_type-1]
            tv_task_name_str.text=taskList[position].form_name
            tv_scheduler_time_str.text=Settings.SDF_DATE.format(taskList[position].scheduler_time)
            if(taskList[position].create_time!=(0.toLong())){
                tv_complete_time_str.text=Settings.SDF_DATE.format(taskList[position].create_time)
            }else{
                tv_complete_time_str.text=resources.getText(R.string.type_unstart)
            }

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

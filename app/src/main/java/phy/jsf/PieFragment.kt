package phy.jsf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lwb.piechart.PieChartView
import phy.jsf.data.Task
import phy.jsf.db.DbManager
import phy.jsf.db.Settings
import phy.jsf.db.Settings.*
import x.frame.BaseActivity
import x.frame.BaseFragment
import java.util.*


class PieFragment: BaseFragment(), BaseActivity.OnAction  {
    override fun onGetAction(intent: Intent?) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.pie_task, container, false)
        setView(root)
        return root
    }
    fun setView(root: View){
        val pieChartView: PieChartView = root.findViewById(R.id.pie_chart_view)
        var taskList: ArrayList<Task> = ArrayList()
        DbManager.getDbManager(mActivity).getAllTask(taskList, Settings.curUser)
        var all=taskList.size
        var delay=0
        var unstart=0
        var cache=0
        var err=0
        var commit =0
        for(item in taskList){
            if(item.state==TASK_UNSTART){
                var sch=Settings.SDF_DATE.format(item.scheduler_time)
                var today=Settings.SDF_DATE.format(System.currentTimeMillis())
                if(item.create_time == (0.toLong())&&(sch<today)){
                    delay++
                }else{
                    unstart++
                }

            }
            if(item.state==TASK_COMMIT||item.state==TASK_ERR_OVER){
                commit++
            }
            if(item.state==TASK_ERR){
                err++
            }
            if(item.state==TASK_CACHE){
                cache++
            }
        }
        pieChartView.addItemType(PieChartView.ItemType(resources.getString(R.string.type_unstart), unstart, resources.getColor(R.color.yellow)))
        pieChartView.addItemType(PieChartView.ItemType(resources.getString(R.string.type_uncommit), cache, resources.getColor(R.color.blue_light)))
        pieChartView.addItemType(PieChartView.ItemType(resources.getString(R.string.type_delay), delay, resources.getColor(R.color.color14)))
        pieChartView.addItemType(PieChartView.ItemType(resources.getString(R.string.type_complete), commit, resources.getColor(R.color.color18)))
        pieChartView.addItemType(PieChartView.ItemType(resources.getString(R.string.type_err), err, resources.getColor(R.color.color15)))
        pieChartView.setBackGroundColor(R.color.color18)    //设置背景颜色
        pieChartView.setItemTextSize(30)                    //设置字体大小
        pieChartView.setTextPadding(10)                    //设置字体与横线距离

    }
}
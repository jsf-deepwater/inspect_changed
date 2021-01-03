package phy.jsf

import android.content.Intent
import android.os.Bundle
import android.util.JsonWriter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject
import phy.jsf.data.Task
import phy.jsf.util.MyWebView
import x.datautil.L
import x.frame.BaseActivity
import x.frame.BaseFragment
import java.util.*


class WebViewFragment: BaseFragment(), BaseActivity.OnAction  {
    companion object {
        public const val EXTRAL_TASK_ITEM="phy.jsf.WebViewFragment.EXTRAL_TASK_ITEM"
    }

    lateinit var my_web: MyWebView
    var task:Task?=null
    interface ChartCallBack {
        fun URLLoadFinished()
    }

    override fun onGetAction(intent: Intent?) {
        if(intent!=null){
            var bundle=intent!!.extras
            if(bundle!=null){
                task=bundle.getParcelable(EXTRAL_TASK_ITEM)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.web_content, container, false)
        setView(root)
        return root
    }
    fun setView(root: View){
        my_web= root.findViewById(R.id.web)
        val wSet: WebSettings = my_web.getSettings()
        wSet.javaScriptEnabled = true
        my_web.setBackgroundColor(0)
        my_web.setVerticalScrollBarEnabled(false)
        my_web.setHorizontalScrollBarEnabled(false)
        my_web.addJavascriptInterface(JsInteration(), "control") //传递对象进行交互

        my_web.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) {
                    //do something.
                    if(task!=null){

                        var json=JSONObject()
                        json.put("form_id",task!!.form_id)
                        //other content.
                        //...
                        var jsonStr=json.toString()

                        my_web.loadUrl(String.format(Locale.US, "javascript:edit_task(%s)", jsonStr))
                    }
                    if (callback != null) {
                        callback.URLLoadFinished()
                    }
                }
            }
        })
        my_web.loadUrl("file:///android_asset/html/test.html")
        var iv_back=root.findViewById<ImageView>(R.id.iv_back)
        var tv_title=root.findViewById<TextView>(R.id.tv_title)
        tv_title.setText(R.string.edit_text)
        }
    var callback: ChartCallBack = object : ChartCallBack {
        override fun URLLoadFinished() {
            //do something
        }
    }

    class JsInteration{
        @JavascriptInterface
        fun onTaskEdit(json: String) {
            L.e("web edit task res json:$json")
        }
    }

    }

package phy.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpsHurlStackCreator;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;

import phy.jsf.data.Task;
import phy.jsf.data.User;
import phy.jsf.db.Settings;
import x.datautil.L;

import static phy.jsf.db.DbAttrs.C_TASK_UPLOAD_STATE;
import static phy.network.WpServer.URL_FORM_LIST;
import static phy.network.WpServer.URL_USER_LIST;
import static phy.network.WpServer.URL_USER_LOGIN;
import static x.frame.BaseActivity.ACTION_LOGOUT;
import static x.frame.BaseActivity.EXTRA_IF_LOGIN_INVALID;


/**
 * Created by phy on 2017/8/17.
 */

public class HttpSender {

    public final static String TAG = "HttpSender";

    public final static SimpleDateFormat SERVER_SDF_TIME_UTC = new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.US);

    static {
        SERVER_SDF_TIME_UTC.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }

    public final static SimpleDateFormat SERVER_SDF_DATE = new SimpleDateFormat("yyyy MM dd", Locale.US);
    public final static SimpleDateFormat SERVER_SDF_TIME = new SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.US);

    public final static String SOURCE_REQ_DATA="SOURCE_REQ_DATA";

    /**
     * response field
     */
    public static final String RESULT = "result";
    public static final String MESSAGE = "message";

    public static final String MSG="msg";
    public static final String CODE="code";
    public static final String ENTITY="entity";
    public static final String ROWS="rows";
    public static final String LIST="list";
    public static final String TOTAL_PAGE="totalPage";
    public static final String TOTAL_COUNT="totalCount";
    public static final String CUR_PAGE="currentPage";
    public static final String PAGE_SIZE="pageSize";

    //user list
    public static final String ID="id";
    public static final String USERNAME="username";
    public static final String ID_CARD="idCard";
    public static final String PWD="password";
    public static final String NAME="name";
    public static final String JOBCODE="jobCode";
    public static final String ROLE="role";
    public static final String PHONE="phone";
    public static final String EMAIL="email";
    public static final String REMARK="remark";
    public static final String STATUS="status";
    public static final String USER_ID="userId";

    //form list
    public static final String DEVICE_NAME="equipmentName";
    public static final String DEVICE_ID="equipmentCode";
    public static final String FORM_NAME="formName";
    public static final String FORM_TYPE="formType";
    public static final String BUILDING="building";
    public static final String FLOOR="floor";
    public static final String ROOM="room";
    public static final String PLAN_ID="planId";
    public static final String FORM_ID="formId";
    public static final String FORM_CONTENT="formContent";
    public static final String PLAN_DATE="planDate";
    public static final String PLAN_DAY="planDay";
    public static final String CONTENT="content";
    public static final String CONTENTS="contents";
    public static final String CREATE_TIME="createTime";
    public static final String UPDATE_TIME="updateTime";
    public static final String CHECK_USER="checkUser";
    public static final String CHECK_TIME="checkTime";
    public static final String CONTENT_VAL="value";
    public static final String CONTENT_TYPE="type";
    public static final String CONTENT_TITLE="title";
    public static final String CONTENT_JSONNAME="jsonName";
    public static final String CONTENT_SELECTIONS="selectOption";


    /******
     {
     "rows": [
     [{
     "type": "text",
     "title": "单行文本",
     "jsonName": "drag_name_0",
     "selectOption": []
     }, {
     "type": "textArea",
     "title": "多行文本",
     "jsonName": "drag_name_1",
     "selectOption": []
     }],
     [{
     "type": "radio",
     "title": "单选框",
     "jsonName": "drag_name_2",
     "selectOption": ["选项一", "选项二"]
     }, {
     "type": "checkbox",
     "title": "复选框",
     "jsonName": "drag_name_3",
     "selectOption": ["选项一", "选项二"]
     }],
     [{
     "type": "radio",
     "title": "单选框",
     "jsonName": "drag_name_4",
     "selectOption": ["选项一", "选项二"]
     }, {
     "type": "checkbox",
     "title": "复选框",
     "jsonName": "drag_name_5",
     "selectOption": ["选项一", "选项二"]
     }],
     [{
     "type": "select",
     "title": "下拉框",
     "jsonName": "drag_name_6",
     "selectOption": []
     }, {
     "size": "16px",
     "type": "title",
     "title": "标题",
     "jsonName": "drag_name_7",
     "selectOption": []
     }]
     ]
     }
     ***/



    //form content


    public static final int CODE_SUCCESS = 200;
    public static final int CODE_SERVER_ERR = 500;



    public static final int ERROR_EXCEPTION_JSON = 100;
    public static final int ERROR_NO_NETWOKR = 101;
    public static final int ERROR_OTHER = 102;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_LOGIN_REQUIRE = 255;
    //腾讯短信服务返回的错误码，短信频率限制
    public static final int RESULT_LOGIN_MSG_LIMIT = 1025;


    public final static int TIME_OUT = 1000 * 60;
    static RequestQueue mRequestQueue;
    Context mContext;

    public HttpSender(Context mContext) {
//        this.mContext = mContext.getApplicationContext();
        this.mContext = mContext;
//        if (!WPApplicationConfig.isApkDebugable(mContext) || useBusinessServer) {
//            WpServer.BASE_URL = BASE_BUSINESS_URL;
//            WpServer.APP_ID_VALUE = BUSINESS_SERVER_APP_ID_VALUE;
//            WpServer.APP_SECRET_VALUE = BUSINESS_SERVER_APP_SECRET_VALUE;
//            Log.e("TAG", "connect to business url.");
//        }
        getRequestQueue(this.mContext);
    }

    String getFullUrl(String url){
        return "http://"+Settings.server_ip +"/ccWb/api/"+url;

    }

    public interface HttpUpdateListener {
        void onHttpUpdate(String http_type, HashMap<String, Object> mapOutput);
    }

    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            synchronized (HttpSender.class) {
                mRequestQueue = Volley.newRequestQueue(context, HttpsHurlStackCreator.create(context));
            }
        }
        return mRequestQueue;
    }

    RetryPolicy defRetryPolicy = new DefaultRetryPolicy(TIME_OUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public void requestPostImage(final String url, HashMap<String, String> params, String imgKey, File file, final HttpUpdateListener listener) {
//        String fullUrl = BASE_URL + url;
//        Response.Listener<JSONObject> responseHandler = new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                HashMap<String, Object> outPut = resolveResponseAttrs(url, response);
//                if (RESULT_LOGIN_REQUIRE != (int) outPut.get(RESULT)) {
//                    listener.onHttpUpdate(url, outPut);
//                } else {//重新登录
//                    Intent intent = new Intent(ACTION_LOGOUT);
//                    intent.putExtra(EXTRA_IF_LOGIN_INVALID, true);
//                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(intent));
//                }
//            }
//        };
//        MultipartRequest request = new MultipartRequest(fullUrl, params, imgKey, file, responseHandler, new ErrorListener(url, listener));
//        request.setShouldCache(false);
//        request.setRetryPolicy(defRetryPolicy);
//        mRequestQueue.add(request);
    }


    /**
     * post
     *
     * @param url
     * @param listener
     */
    public void request(final String url, HashMap<String, Object> input, final Object source_data,final HttpUpdateListener listener) {
        if (!canSendHttpRequest()) {
            HashMap<String, Object> outPut = new HashMap<>();
            outPut.put(RESULT, ERROR_NO_NETWOKR);
            listener.onHttpUpdate(url, outPut);
            Log.d("TAG", "no network,refuse request:" + url);
            return;
        }
        L.e("Request data:"+new JSONObject(input).toString());
        boolean ifGetMethod = false;
        String fullUrl = getFullUrl(url);
        if (WpServer.httpGetMethods.contains(url)) {
            ifGetMethod = true;
            StringBuilder sb = new StringBuilder(fullUrl);
            if (!input.isEmpty()) {
                sb.append("?");
                for (String key : input.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(input.get(key).toString());
                    sb.append("&");
                }
                sb.deleteCharAt(sb.lastIndexOf("&"));
                fullUrl = sb.toString();
            }
        }

        Response.Listener<JSONObject> responseHandler = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                L.e(response.toString());
                HashMap<String, Object> outPut = resolveResponseAttrs(url, response);
                outPut.put(SOURCE_REQ_DATA,source_data);
                if(outPut.containsKey(CODE)&&RESULT_LOGIN_REQUIRE == (int) outPut.get(CODE)){
                    Intent intent = new Intent(ACTION_LOGOUT);
                    intent.putExtra(EXTRA_IF_LOGIN_INVALID, true);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(intent));
                }else{
                    listener.onHttpUpdate(url, outPut);
                }
            }
        };
        PostSessionRequest request = new PostSessionRequest(fullUrl, ifGetMethod ? null : new JSONObject(input), responseHandler, new ErrorListener(url, listener));
        request.setShouldCache(false);
        request.setRetryPolicy(defRetryPolicy);
        mRequestQueue.add(request);
    }
    public void request(final String url,final JSONObject obj,final Object source_data, final HttpUpdateListener listener) {
        if (!canSendHttpRequest()) {
            HashMap<String, Object> outPut = new HashMap<>();
            outPut.put(RESULT, ERROR_NO_NETWOKR);
            listener.onHttpUpdate(url, outPut);
            Log.d("TAG", "no network,refuse request:" + url);
            return;
        }
        L.e("Request data:"+obj.toString());
        boolean ifGetMethod = false;
        String fullUrl = getFullUrl(url);
        if (WpServer.httpGetMethods.contains(url)) {
            ifGetMethod = true;
            StringBuilder sb = new StringBuilder(fullUrl);
//            if (!input.isEmpty()) {
//                sb.append("?");
//                for (String key : input.keySet()) {
//                    sb.append(key);
//                    sb.append("=");
//                    sb.append(input.get(key).toString());
//                    sb.append("&");
//                }
//                sb.deleteCharAt(sb.lastIndexOf("&"));
//                fullUrl = sb.toString();
//            }
        }

        Response.Listener<JSONObject> responseHandler = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                L.e(response.toString());
                HashMap<String, Object> outPut = resolveResponseAttrs(url, response);
                outPut.put(SOURCE_REQ_DATA,source_data);
                if(outPut.containsKey(CODE)&&RESULT_LOGIN_REQUIRE == (int) outPut.get(CODE)){
                    Intent intent = new Intent(ACTION_LOGOUT);
                    intent.putExtra(EXTRA_IF_LOGIN_INVALID, true);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(intent));
                }else{
                    listener.onHttpUpdate(url, outPut);
                }
            }
        };
        PostSessionRequest request = new PostSessionRequest(fullUrl, ifGetMethod ? null : obj, responseHandler, new ErrorListener(url, listener));
        request.setShouldCache(false);
        request.setRetryPolicy(defRetryPolicy);
        mRequestQueue.add(request);
    }
    /**
     * 根据url类型将response结果解析成 hashmap
     *
     * @param url
     * @param response
     * @return
     */
    HashMap<String, Object> resolveResponseAttrs(String url, JSONObject response) {
        HashMap<String, Object> outPut = new HashMap<>();

        try {
            int code = response.getInt(CODE);
            outPut.put(CODE, code);
            if (response.has(MSG)) {
                outPut.put(MSG, response.getString(MSG));
            }
            if (code != CODE_SUCCESS) {
                L.e("TAG", "http response error_res:" + response.toString());
            }
            if(URL_FORM_LIST.equals(url)){
                if(response.has(ENTITY)){
                    JSONObject jo2=response.getJSONObject(ENTITY);
                    outPut.put(TOTAL_PAGE,jo2.getInt(TOTAL_PAGE));
                    outPut.put(TOTAL_COUNT,jo2.getInt(TOTAL_COUNT));
                    if(jo2.has(LIST)){
                        ArrayList mList=new ArrayList<Task>();
                        outPut.put(LIST,mList);
                        JSONArray mArray=jo2.getJSONArray(LIST);
                        for(int i=0;i<mArray.length();i++){
                            JSONObject jo3=mArray.getJSONObject(i);
                            Task task=new Task();
                            task.task_id=jo3.getString(PLAN_ID);
                            task.form_id=jo3.getString(FORM_ID);
                            task.device_id=jo3.getString(DEVICE_ID);
                            task.device_name=jo3.getString(DEVICE_NAME);
                            task.form_name=jo3.getString(FORM_NAME);
                            String formContent=jo3.getString(FORM_CONTENT);
                            formContent=formContent.replace("\\","");
                            task.content=formContent;
                            task.form_type=jo3.getInt(FORM_TYPE);
                            task.scheduler_time=jo3.getLong(PLAN_DATE);
                            task.form_day_night=jo3.getInt(PLAN_DAY);
                            task.building=jo3.getString(BUILDING);
                            task.device_floor=jo3.getString(FLOOR);
                            task.device_room=jo3.getString(ROOM);
                            if(!jo3.has(STATUS)){
                                task.state= Settings.TASK_UNSTART;
                            }
                            if(!jo3.has(C_TASK_UPLOAD_STATE)){
                                task.upload_state=0;
                            }
                            mList.add(task);
                        }
                    }
                }
            }else if(URL_USER_LIST.equals(url)){
                if(response.has(ENTITY)){
                    JSONObject jo2=response.getJSONObject(ENTITY);
                    outPut.put(TOTAL_PAGE,jo2.getInt(TOTAL_PAGE));
                    outPut.put(TOTAL_COUNT,jo2.getInt(TOTAL_COUNT));
                    if(jo2.has(LIST)){
                        ArrayList mList=new ArrayList<User>();
                        outPut.put(LIST,mList);
                        JSONArray mArray=jo2.getJSONArray(LIST);
                        for(int i=0;i<mArray.length();i++){
                            JSONObject jo3=mArray.getJSONObject(i);
                            User user=new User();
                            user.user_id=jo3.getString(ID);
                            user.user_name=jo3.getString(USERNAME);
                            user.id_card=jo3.getString(ID_CARD);
                            user.dis_name=jo3.getString(NAME);
                            user.pwd=jo3.getString(PWD);
                            user.job_code=jo3.getString(JOBCODE);
                            user.phone=jo3.getString(PHONE);
                            user.email=jo3.getString(EMAIL);
                            if(jo3.has(REMARK))user.remark=jo3.getString(REMARK);
                            user.status=jo3.getInt(STATUS);
                            user.role=jo3.getInt(ROLE);
                            mList.add(user);
                        }
                    }
                }
            }else if(URL_USER_LOGIN.equals(url)){
                //
            }
        } catch (Exception e) {
            outPut.put(CODE, ERROR_EXCEPTION_JSON);
            outPut.put(MSG, e.getMessage());
            e.printStackTrace();
        }
        return outPut;
    }

    /**
     * @param maxWidth  Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight Maximum height to decode this bitmap to, or zero for
     *                  none
     * @param mListener
     * @param fullUrl
     * @param scaleType
     */
    public void requestImage(final ImageResListener mListener, String fullUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        ImageRequest imageRequest = new ImageRequest(fullUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                mListener.onResponse(response);
            }
        }, maxWidth, maxHeight, scaleType, Bitmap.Config.ARGB_8888, null);
        mRequestQueue.add(imageRequest);
    }

    public static class ErrorListener implements Response.ErrorListener {
        String mHttpEvent;
        HttpUpdateListener listener;

        public ErrorListener(String http_event, HttpUpdateListener listener) {
            mHttpEvent = http_event;
            this.listener = listener;
        }

        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (error instanceof NoConnectionError) {
                map.put(RESULT, ERROR_NO_NETWOKR);
            } else {
                map.put(RESULT, ERROR_OTHER);
            }
            map.put(MESSAGE, error.toString());
            if (listener != null) {
                listener.onHttpUpdate(mHttpEvent, map);
            }
        }
    }

    public boolean canSendHttpRequest() {
        boolean result = false;
        NetworkInfo mNetInfo = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (mNetInfo != null && mNetInfo.isConnected()) {
            int activeConnectType = mNetInfo.getType();
            int activeSubType = mNetInfo.getSubtype();

            if (activeConnectType == ConnectivityManager.TYPE_WIFI
                    || activeConnectType == ConnectivityManager.TYPE_ETHERNET
                    || activeConnectType == ConnectivityManager.TYPE_MOBILE
                    ) {
                //type_ehternet: no wifi and mobile data, phone connected with desktop
                result = true;
            }
        }
        return result;
    }

    public interface ImageResListener {
        void onResponse(Bitmap response);
    }

}

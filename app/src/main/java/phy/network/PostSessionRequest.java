package phy.network;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//import static com.miaomiaoce.mmc.wp.network.WpServer.APP_ID_KEY;
//import static com.miaomiaoce.mmc.wp.network.WpServer.APP_ID_VALUE;
//import static com.miaomiaoce.mmc.wp.network.WpServer.APP_SECRET_KEY;
//import static com.miaomiaoce.mmc.wp.network.WpServer.APP_SECRET_VALUE;

/**
 * Created by Administrator on 2016/9/5.
 */
public class PostSessionRequest extends JsonObjectRequest {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";

    public PostSessionRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public PostSessionRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }
        addSessionCookie(headers);
        return headers;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        checkSessionCookie(response.headers);
        return super.parseNetworkResponse(response);
    }

    /**
     * 检查返回的Response header中有没有session
     *
     * @param responseHeaders Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> responseHeaders) {
        if (responseHeaders.containsKey(SET_COOKIE_KEY)) {
            String cookie = responseHeaders.get(SET_COOKIE_KEY);
            if (!TextUtils.isEmpty(cookie)) {
//                Settings.httpSession = cookie;
            }
        }
    }

    /**
     * 添加session到Request header中
     */
    public final void addSessionCookie(Map<String, String> requestHeaders) {
//        if (!TextUtils.isEmpty(Settings.httpSession)) {
//            requestHeaders.put(COOKIE_KEY, Settings.httpSession);
//        }
//        {
//            requestHeaders.put(APP_ID_KEY, APP_ID_VALUE);
//            requestHeaders.put(APP_SECRET_KEY, APP_SECRET_VALUE);
//        }
    }
}
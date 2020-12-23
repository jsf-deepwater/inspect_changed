package phy.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by phy on 2017/11/6.
 * <p>
 * Header = {"Content-type" : "multipart/form-data, boundary=AaB03x"}
 * Data =  "--AaB03x\r\n" +
 * "content-disposition: form-data; name=\"field1\"\r\n" +
 * "\r\n" +
 * "Joe Blow\r\n" +
 * "--AaB03x\r\n" +
 * "content-disposition: form-data; name="pics"; filename=\"file1.txt\"\r\n" +
 * "Content-Type: text/plain\r\n" +
 * "\r\n" +
 * "...binary contents of file1.txt ...\r\n" +
 * "--AaB03x--\r\n"
 */
public class MultipartRequest<T> extends Request<T> {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";


    protected static final String PROTOCOL_CHARSET = "utf-8";
    private final String contentKey = "content-disposition";

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private Response.Listener<T> mListener;
    private byte[] mRequestBody;

    private File file;
    private String imgKey;
    HashMap<String, String> params;
    //    HttpEntity entity;
    MultipartEntity entity = new MultipartEntity();

    public MultipartRequest(String url, HashMap<String, String> params, String imgKey, File file, Response.Listener<T> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        this.params = params;
        this.imgKey = imgKey;
        this.file = file;
        buildMultipartEntity();
    }

    void buildMultipartEntity() {
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (String key : params.keySet()) {
//            builder.addTextBody(key, params.get(key));
            try {
                entity.addPart(key, new StringBody(params.get(key), Charset.forName("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        entity.addPart(imgKey, new FileBody(file));
        /*try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            int size = 1024;
            byte[] buffer = new byte[size];
            int len;
            while ((len = bis.read(buffer, 0, size)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] date = baos.toByteArray();
            builder.addBinaryBody(imgKey, date, ContentType.DEFAULT_BINARY, file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            entity.writeTo(baos2);
            mRequestBody = baos2.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBodyContentType() {
//        return mimeType;
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() {
        return mRequestBody;
    }


    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
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

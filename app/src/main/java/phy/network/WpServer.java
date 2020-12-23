package phy.network;

import java.util.HashSet;

/**
 * Created by phy on 2017/9/18.
 */

public class WpServer {
    public static final String DEF_SERVER_IP="39.107.107.131";
    //redirect url
    public static final String LOGIN_REDIRECT_URL = "http://mmc.mi-ae.cn/mmc/api/user/login/";
    //download url
    public static final String APK_DOWNLOAD_URL = "http://a.app.qq.com/o/simple.jsp?pkgname=com.miaomiaoce.mmc.wp";
    public static final String BASE_TEST_URL = "http://39.107.107.131/ccWb/api/";//wp.mmc-data.com/mmcpecker/api
    public static String BASE_URL = BASE_TEST_URL;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public final static String URL_USER_LIST="user/list";    //post
    public final static String URL_FORM_LIST="form/list";        //post
    public final static String URL_FORM_UPLOAD="form/batchAddOrUpdate";//post
    public final static String URL_USER_LOGIN="user/login"; //post
    public final static String URL_USER_LOGOUT="user/logout";//post

    ////////////////////////////////////////////////////////////////////////////////////////////////


//    public final static String URL_USER_REGISTER = "user/register/";
//    public final static String URL_USER_LOGIN = "user/login/";
//    public static final String URL_USER_SEND_VCODE_SMS = "user/send_vcode_sms/";
//    public final static String URL_USER_UPDATE = "user/update/";
//    public final static String URL_USER_ACTIVE = "user/is_user_active/";
//    public final static String URL_DEVICE_CREATE = "device/create/";
//    public final static String URL_DEVICE_REGISTER = "device/register/";
//    public final static String URL_DEVICE_GET = "device/get/";
//    public final static String URL_PROFILE_REGISTER = "profile/register/";
//    public final static String URL_PROFILE_UPDATE = "profile/update/";
//    public final static String URL_PROFILE_GET = "profile/get/";
//    public final static String URL_BINDING_BIND = "binding/bind/";
//    public final static String URL_BINDING_UNBIND = "binding/unbind/";
//    public final static String URL_BINDING_GET = "binding/get/";
//    public final static String URL_TEMP_POST = "temperature/post/";
//    public final static String URL_TEMP_GET = "temperature/get/";
//    public final static String URL_TEMP_UPDATE = "temperature/update/";
//    public final static String URL_TEMP_DELETE = "temperature/delete/";
//    public final static String URL_EVENT_POST = "event/post/";
//    public final static String URL_EVENT_UPDATE = "event/update/";
//    public final static String URL_EVENT_DELETE = "event/delete/";
//    public final static String URL_EVENT_GET = "event/get/";
//    public final static String URL_SERVICE_TIME = "sync/get_utc_time/";
//    public final static String URL_SESSION_START = "session/start/";
//    public final static String URL_SESSION_UPDATE = "session/update/";
//    public final static String URL_SESSION_GET = "session/get/";
//    public final static String URL_SESSION_CONNECT_START = "session/start_connect/";
//    public final static String URL_SESSION_CONNECT_UPDATE = "session/update_connect/";
//
//    public final static String URL_PERIOD_POST = "period/post/";
//    public final static String URL_PERIOD_UPDATE = "period/update/";
//    public final static String URL_PERIOD_DELETE = "period/delete/";
//    public final static String URL_PERIOD_GET = "period/get/";
//
//    public final static String URL_AVATAR_POST = "avatar/post/";
//    public final static String URL_AVATAR_DELETE = "avatar/delete/";
//    public final static String URL_VERSION_GET = "version/get/";
//    public final static String URL_ADVERTISE_GET = "advertise/get/";

    static HashSet<String> httpGetMethods = new HashSet<>(9);

    static {
//        httpGetMethods.add(URL_USER_LOGIN);
//        httpGetMethods.add(URL_USER_ACTIVE);
//        httpGetMethods.add(URL_DEVICE_GET);
//        httpGetMethods.add(URL_PROFILE_GET);
//        httpGetMethods.add(URL_BINDING_GET);
//        httpGetMethods.add(URL_SERVICE_TIME);
//        httpGetMethods.add(URL_SESSION_GET);
//        httpGetMethods.add(URL_PERIOD_DELETE);
//        httpGetMethods.add(URL_VERSION_GET);
//        httpGetMethods.add(URL_ADVERTISE_GET);
    }

    //http header key
    public static final String APP_ID_KEY = "APP_ID";
    public static final String APP_SECRET_KEY = "APP_SECRET";

    public static final String TEST_SERVER_APP_ID_VALUE = "mmc_wp";
    public static final String TEST_SERVER_APP_SECRET_VALUE = "mmc";

    public static final String BUSINESS_SERVER_APP_ID_VALUE = "mmc_wp_prod";
    public static final String BUSINESS_SERVER_APP_SECRET_VALUE = "2I4eOXyMf5izGtuUIexu5bin1tq6ZG";

    public static String APP_ID_VALUE = TEST_SERVER_APP_ID_VALUE;
    public static String APP_SECRET_VALUE = TEST_SERVER_APP_SECRET_VALUE;

    //appid of xiaomi
    public static final Long XIAOMI_APP_ID = 2882303761517613552l;
    public static final String XIAOMI_APP_KEY = "5881761361552";
    public final static int[] XIAOMI_OAUTH_SCOPE = {1, 3};//获取小米用户个人资料,获取小米用户 open_id
}

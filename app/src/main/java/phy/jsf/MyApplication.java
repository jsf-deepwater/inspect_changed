package phy.jsf;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import phy.jsf.db.Settings;
import x.datautil.L;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        L.initConsoleLog("PHY");
        Settings.init(this);
        Intent service=new Intent(this,DataService.class);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            startForegroundService(service);
        }else{
            startService(service);
        }
    }
}

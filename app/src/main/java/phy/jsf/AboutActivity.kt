package phy.jsf

import android.os.Bundle
import x.frame.BaseActivity

class AboutActivity:BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frame_empty_layout)
//        showFragment(AboutBluetoothFragment::class.java, intent.extras)
    }

}
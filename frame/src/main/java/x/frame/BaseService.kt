package x.frame

import android.app.Service
import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

open abstract class BaseService : Service() {
    /**
     * for handler msg.
     */
    protected open fun handleMessage(msg: Message){}

    open class ServiceSecureHandler(service: BaseService) : Handler() {
        protected var mReference: WeakReference<BaseService>? = null

        init {
            mReference = WeakReference(service)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mReference != null) {
                val service = mReference!!.get()
                if (service != null) {
                    service.handleMessage(msg)
                }
            }
        }
    }

}

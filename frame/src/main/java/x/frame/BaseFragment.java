package x.frame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

import x.datautil.L;

/**
 * Created by phy on 2017/8/17.
 */

public abstract class BaseFragment extends Fragment implements  BaseActivity.OnAction {
    protected BaseActivity mActivity;
    FragmentSecureHandler mSecureHandler;

    protected BaseFragment showFragment(Class<? extends BaseFragment> clazz, Bundle bundle) {
        return mActivity.showFragment(clazz, bundle, true);
    }
    protected BaseFragment showFragment(Class<? extends BaseFragment> clazz, Bundle bundle,boolean allowBackStock) {
        return mActivity.showFragment(clazz, bundle, allowBackStock);
    }
    @Override
    @CallSuper
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mActivity == null) {
            mActivity = (BaseActivity) getActivity();
        }
        mSecureHandler = new FragmentSecureHandler(this);
    }

    @Override
    @CallSuper
    public final void onResume() {
        super.onResume();
        if (!isHidden()) {//发现fragment被 hide 的时候，onResume依然会被调用，这里过滤一ide 状态
            L.e("onresume,");
            onVisible();
        }
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        if (!isHidden()) {//发现fragment被 hide 的时候，onResume依然会被调用，这里过滤一下 hide 状态
            L.e("onpause,");
            onHide();
        }

    }

    @Override
    public final void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            L.e("onHiddenChanged visible,");
            onVisible();
        }else{
            L.e("onHiddenChanged hide,");
            onHide();
        }
    }

    /**
     * 需要在fragment可见的时候刷新状态的操作都可以放在这里
     */
    @CallSuper
    public void onVisible() {
        try {
        //    MobclickAgent.onPageStart(getClass().getSimpleName());
        } catch (Exception e) {
        }
    }
    @CallSuper
    public void onHide(){
        try {
        //    MobclickAgent.onPageEnd(getClass().getSimpleName());
        } catch (Exception e) {
        }
    }

    /**
     * 可能不被调用
     *
     * @param context
     */
    @Override
    @CallSuper
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mActivity == null) {
            mActivity = (BaseActivity) getActivity();
        }
    }

    @Override
    @CallSuper
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (BaseActivity) getActivity();
        }
    }

    /**
     * 返回键,这里在 top_navigation_bar 中调用此方法,不要删除
     *
     * @param back
     */
    public void onBackPressed(View back) {
        mActivity.onBackPressed(back);
    }

    /**
     * handler msg.
     *
     * @param msg
     */
    protected void handleMessage(Message msg) {
    }

    final static class FragmentSecureHandler extends Handler {
        protected WeakReference<BaseFragment> mReference;

        public FragmentSecureHandler(BaseFragment fragment) {
            mReference = new WeakReference(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference != null) {
                BaseFragment fragment = mReference.get();
                if (fragment != null && fragment.isAdded() && !fragment.isDetached()) {
                    fragment.handleMessage(msg);
                }
            }
        }
    }
}

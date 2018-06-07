package com.contentsapp.sancsvision.vivekbindra;

import android.app.Application;
import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by abhinandan on 01/06/18.
 */

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    private static MainApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        AndroidNetworking.initialize(getApplicationContext());
    }

    public static void configureDefaultImageLoader(Context context){
        ImageLoaderConfiguration defaultConfig = new ImageLoaderConfiguration.Builder(context).
                threadPriority(Thread.MAX_PRIORITY).
                denyCacheImageMultipleSizesInMemory().
                tasksProcessingOrder(QueueProcessingType.LIFO).
                diskCacheFileNameGenerator(new Md5FileNameGenerator()).
                build();

        ImageLoader.getInstance().init(defaultConfig);
    }


    public static synchronized MainApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(Connectivity.ConnectivityReceiverListener listener) {
        Connectivity.connectivityReceiverListener = listener;
    }
}

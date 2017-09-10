package com.server.service.binder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

/**
 * Created by dawish on 2017/8/24.
 * 自己实现Binder实现进程通信
 * http://www.cnblogs.com/punkisnotdead/p/5163464.html
 */

public class BinderService extends Service {

    public BinderService (){
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //将实际的Binder对象返回给服务端
        Logger.i("danxx", "BinderService onBind");
        return new BookManager().asBinder();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        Logger.i("danxx", "BinderService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("danxx", "BinderService onCreate");
    }
}

package com.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.DisplayUtil;
import com.server.service.aidl.MyAIDLService;
import com.server.service.data.ServiceData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityAidlService extends AppCompatActivity {

    @BindView(R.id.bind_service)
    public Button bindService;

    @BindView(R.id.unbind_service)
    public Button unbindService;

    @BindView(R.id.addData)
    public Button addData;

    @BindView(R.id.getData)
    public Button getData;

    @BindView(R.id.txt)
    TextView tvInfo;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    /**服务是否连接*/
    public boolean isBound = false;
    /**远程service使用*/
    public MyAIDLService myAIDLService;
    private SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
    /**
     *  ServiceConnection is Interface for monitoring the state of an application service
     *  ServiceConnection是一个观察程序service的回调接口
     */
    ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * service连接成功后的回调
         * @param name
         * @param service  通讯接口
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            showMessage("-----远程服务链接成功-----", R.color.praised_num);
            isBound = true;
            myAIDLService = MyAIDLService.Stub.asInterface(service); //远程service写法
            try {
                //设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            myAIDLService = null;
            showMessage("-----远程服务已断开连接-----", R.color.red_deep);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl_client);
        ButterKnife.bind(ActivityAidlService.this);
        showMessage("-----远程服务等待连接-----", R.color.green_pure);
    }

    /**
     * Service 的两种启动方法和区别
     Service的生命周期方法onCreate, onStart, onDestroy
     有两种方式启动一个Service,他们对Service生命周期的影响是不一样的。
     1 通过startService
     　　Service会经历 onCreate -> onStart
     　stopService的时候直接onDestroy
     如果是调用者自己直接退出而没有调用stopService的话，Service会一直在后台运行。下次调用者再起来可以stopService。
     2 通过bindService
     　　Service只会运行onCreate， 这个时候服务的调用者和服务绑定在一起
     调用者退出了，Srevice就会调用onUnbind->onDestroyed所谓绑定在一起就共存亡了。并且这种方式还可以使得
     *
     */

    @OnClick({R.id.bind_service, R.id.unbind_service,R.id.addData,R.id.getData})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind_service:
                if (!isBound) {
                    //用Intent匹配的方式绑定service
                    doBindService();
                }
                break;
            case R.id.unbind_service:
                if(isBound && myAIDLService != null && serviceConnection!=null) {
                    unbindService(serviceConnection);
                    isBound = false;
                    showMessage("-----主动断开远程连接-----", R.color.red_deep);
                }
                break;
            case R.id.addData:
                addData();
                break;
            case R.id.getData:
                getData();
                break;
            default:
                break;
        }
    }

    private void addData(){
        if(isBound){
            ServiceData serviceData = new ServiceData();
            try {
                int size = myAIDLService.getDataList().size();
                serviceData.setName("no-"+size);
                serviceData.setId(String.valueOf(size));
                serviceData.setPrice(String.valueOf(size+2));
                serviceData.setType(String.valueOf(size%8));

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Name:"+serviceData.getName());
                stringBuffer.append("  Id:"+serviceData.getId());
                stringBuffer.append("  Type:"+serviceData.getType());
                stringBuffer.append("  Price:"+serviceData.getPrice());
                showMessage(stringBuffer.toString(), R.color.blue_color);
                myAIDLService.addData(serviceData);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this,"服务还未连接！", Toast.LENGTH_SHORT).show();
        }

    }

    private void getData(){
        if(isBound){
            try {
                int size = myAIDLService.getDataList().size();
//                tvInfo.setText("Data size: "+ size);
                showMessage("Data size: "+ size, R.color.blue_color);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this,"服务还未连接！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示文字
     *
     * @param info 提示信息
     */
    private void showMessage(final String info, final int color) {
        tvInfo.post(new Runnable() {
            @Override
            public void run() {
                int startPos = stringBuilder.length();
                stringBuilder.append("\n"+info);
                tvInfo.setText(DisplayUtil.changeTextColor(ActivityAidlService.this, stringBuilder, color, startPos));
            }
        });
        tvInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != scrollView) scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },100);
    }

    protected void showToast(String msg){
        Toast.makeText(ActivityAidlService.this, msg,Toast.LENGTH_SHORT).show();
    }

    //客户端使用死亡代理，可以重启service
    //http://blog.csdn.net/liuyi1207164339/article/details/51706585
    //服务端使用死亡回调回收数据
    //http://www.cnblogs.com/punkisnotdead/p/5158016.html
    //死亡通知原理分析
    //http://light3moon.com/2015/01/28/Android%20Binder%20%E5%88%86%E6%9E%90%E2%80%94%E2%80%94%E6%AD%BB%E4%BA%A1%E9%80%9A%E7%9F%A5[DeathRecipient]/
    /**
     * 监听Binder是否死亡
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (myAIDLService == null) {
                return;
            }
            //死亡后解除绑定
            myAIDLService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            myAIDLService = null;
            //重新绑定
            doBindService();
            showMessage("--DeathRecipient后重链远程服务--", R.color.red_wine);
        }
    };

    private void doBindService(){
        showMessage("-----开始链接远程服务-----", R.color.light_yellow);
        Intent intent = new Intent();
        intent.setAction("com.danxx.aidlService");
        intent.setPackage("com.server");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

}

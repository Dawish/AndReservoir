// MyAIDLService.aidl
//生产的MyAIDLService.java类会使用这个包名
package com.server.service.aidl;
//导入数据javabean类，注意包名不要错
import com.server.service.data.ServiceData;
// Declare any non-default types here with import statements
interface MyAIDLService {
    /**
     * 除了基本数据类型，其他类型的参数都需要标上方向类型：in(输入), out(输出), inout(输入输出)
     */
    void addData(in ServiceData data);

    List<ServiceData> getDataList();
}

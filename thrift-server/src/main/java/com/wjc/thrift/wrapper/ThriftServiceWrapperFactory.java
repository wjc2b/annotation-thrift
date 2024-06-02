package com.wjc.thrift.wrapper;

import com.wjc.thrift.exception.ThriftServerException;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author wjc
 * @date 2024-04-06 18:52
 * @desription
 */
public class ThriftServiceWrapperFactory {

    public static ThriftServiceWrapper wrapper(final String thriftServiceId,String thriftServiceName,Object thriftService,double version){
        ThriftServiceWrapper thriftServiceWrapper;
        if(version<=-1){
            thriftServiceWrapper = new ThriftServiceWrapper(thriftServiceName,thriftService.getClass(),thriftService);
        }else{
            thriftServiceWrapper = new ThriftServiceWrapper(thriftServiceName,thriftService.getClass(),thriftService,version);
        }
        // 获得以”$Iface“为结束的接口
        Class<?> thriftServiceIface = Arrays.stream(thriftService.getClass().getInterfaces())
                .filter(iface -> iface.getName().endsWith("$Iface"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No thrift Iface found on service"));

        thriftServiceWrapper.setType(thriftService.getClass());
        thriftServiceWrapper.setIfaceType(thriftServiceIface);
        // 生成签名，格式: serviceId $ 接口名称 $ 版本
        final String signature = String.join("$",new String[]{
                thriftServiceId,thriftServiceIface.getDeclaringClass().getName(),String.valueOf(version)
        });
        thriftServiceWrapper.setThriftServiceSignature(signature);
        return thriftServiceWrapper;
    }

    /**
     * 从传入的type进行解析
     * @param thriftServiceId
     * @param thriftServiceName
     * @param type
     * @param thriftService
     * @param version
     * @return
     */
    public static ThriftServiceWrapper wrapper(final String thriftServiceId, String thriftServiceName, Class<?> type, Object thriftService, double version) {
        if (Objects.isNull(thriftService) || !Objects.equals(type, thriftService.getClass())) {
            throw new ThriftServerException("Thrift service initializing in wrong way");
        }

        ThriftServiceWrapper thriftServiceWrapper;
        if (version <= -1) {
            thriftServiceWrapper = new ThriftServiceWrapper(thriftServiceName, thriftService.getClass(), thriftService);
        } else {
            thriftServiceWrapper = new ThriftServiceWrapper(thriftServiceName, thriftService.getClass(), thriftService, version);
        }

        Class<?> thriftServiceIface = Arrays.stream(type.getInterfaces())
                .filter(iface -> iface.getName().endsWith("$Iface"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No thrift IFace found on service"));

        thriftServiceWrapper.setIfaceType(thriftServiceIface);

        final String signature = String.join("$", new String[]{
                thriftServiceId, thriftServiceIface.getDeclaringClass().getName(),
                String.valueOf(version)
        });

        thriftServiceWrapper.setThriftServiceSignature(signature);

        return thriftServiceWrapper;
    }
}

package com.wjc.thrift.client.service;

import com.wjc.iface.thrift.CalculatorService;
import com.wjc.thrift.client.annotation.ThriftClient;
import com.wjc.thrift.client.common.ThriftClientAware;

/**
 * @author wjc
 * @date 2024-04-18 14:29
 * @desription
 */
@ThriftClient(serviceId = "thrift-calculator-rpc-server",refer = CalculatorService.class,version = 2.0)
public interface CalculatorThriftClient extends ThriftClientAware<CalculatorService.Client> {
}

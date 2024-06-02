package com.wjc.thrift.client.discovery;

import com.wjc.thrift.client.common.ThriftServerNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author wjc
 * @date 2024-04-17 15:30
 * @desription
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ThriftConsulServerNode extends ThriftServerNode {
    private String node;

    private String serviceId;

    private List<String> tags;

    private String host;

    private int port;

    private String address;

    private boolean isHealth;
}

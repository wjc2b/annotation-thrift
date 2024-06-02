package com.wjc.thrift.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author wjc
 * @date 2024-04-16 15:46
 * @desription
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThriftServerNode {
    private String host;

    private int port;

    private int timeout;
}

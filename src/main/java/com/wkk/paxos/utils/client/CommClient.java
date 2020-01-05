package com.wkk.paxos.utils.client;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @Time: 19-12-6下午12:28
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public interface CommClient {
    public void sendTo(String ip, int port, byte[] msg) throws UnknownHostException, IOException;

}

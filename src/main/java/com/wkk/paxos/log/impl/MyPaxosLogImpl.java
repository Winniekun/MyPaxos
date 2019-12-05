package com.wkk.paxos.log.impl;

import com.wkk.paxos.log.MyPaxosLog;

import java.io.IOException;

/**
 * @Time: 19-12-5下午9:21
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public class MyPaxosLogImpl implements MyPaxosLog {

    /**
     * 每个log的结构
     */
    static class log{
        // 类型字段0 -> ballot, 1 -> AcceptBallot, 2 -> value
        int type;
        // value 的长度
        int len;
        // instance的ID
        int instanceId;
        // 值
        Object value;

        public log(int type, int len, int instanceId, Object value) {
            this.type = type;
            this.len = len;
            this.instanceId = instanceId;
            this.value = value;
        }
    }

    private final static byte MAGIC_NUMBER = (byte) 0xd4;


    public String getLogFileAddr() {
        return null;
    }

    public void recoverFromLog() throws IOException {

    }

    public void setInstanceBallot(int instanceId, int ballot) throws IOException {

    }

    public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot) throws IOException {

    }

    public void setInstanceValue(int instanceId, Object value) throws IOException {

    }

    public void clearLog() throws IOException {

    }
}

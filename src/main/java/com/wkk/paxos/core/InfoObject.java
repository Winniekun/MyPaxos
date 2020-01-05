package com.wkk.paxos.core;

/**
 * @Time: 19-12-5下午9:59
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public class InfoObject {
    /**
     * 相关的配置信息
     */
    private int id;
    private String host;
    private int port;

    public InfoObject(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

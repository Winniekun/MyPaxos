package com.wkk.paxos.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @Time: 19-12-5下午10:00
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public class ConfObject {
    private List<InfoObject> nodes = new ArrayList<InfoObject>();
    private int myid;
    private int timeout;
    private int learningInterval;
    private String dataDir;
    private boolean enableDatePersistence;

    public ConfObject(){

    }

    public List<InfoObject> getNodes() {
        return nodes;
    }

    public void setNodes(List<InfoObject> nodes) {
        this.nodes = nodes;
    }

    public int getMyid() {
        return myid;
    }

    public void setMyid(int myid) {
        this.myid = myid;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getLearningInterval() {
        return learningInterval;
    }

    public void setLearningInterval(int learningInterval) {
        this.learningInterval = learningInterval;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public boolean isEnableDatePersistence() {
        return enableDatePersistence;
    }

    public void setEnableDatePersistence(boolean enableDatePersistence) {
        this.enableDatePersistence = enableDatePersistence;
    }
}

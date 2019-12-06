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


}

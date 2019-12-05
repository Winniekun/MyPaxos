package com.wkk.paxos.log;


import java.io.IOException;

/**
 * @Time: 19-12-5下午9:20
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public interface MyPaxosLog {
    /**
     * 获取log文件位置
     */
    public String getLogFileAddr();


    /**
     * 从log文件中恢复
     * @throws IOException
     */
    public void recoverFromLog() throws IOException;


    /**
     * 每次新增, 修改ballot都会调用此函数, 将记录加入log
     * @param instanceId
     * @param ballot
     * @throws IOException
     */
    public void setInstanceBallot(int instanceId, int ballot) throws IOException;


    /**
     * 每次新增, 修改acceptedBallot都会调用此函数, 将记录加入log
     * @param instanceId
     * @param acceptedBallot
     * @throws IOException
     */
    public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot) throws IOException;


    /**
     * 每次新增, 修改value都会调用此函数, 将记录加入log
     * @param instanceId
     * @param value
     * @throws IOException
     */
    public void setInstanceValue(int instanceId, Object value) throws  IOException;


    /**
     * 清空日志
     * @throws IOException
     */
    public void clearLog() throws IOException;


}

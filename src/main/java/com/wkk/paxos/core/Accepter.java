package com.wkk.paxos.core;

import com.wkk.paxos.packet.Value;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.HashMap;
import java.util.Map;

/**
 * @Time: 19-12-5下午9:19
 * @Author: kongwiki
 * @Email: kongwiki@163.com
 */
public class Accepter {
    static class Instance {
        //当前的选票的数量
        private int ballot;
        // 提案内容
        private Value value;
        // 接受到提案的数量
        private int acceptedBallot;


        public Instance(int ballot, Value value, int acceptedBallot) {
            super();
            this.ballot = ballot;
            this.value = value;
            this.acceptedBallot = acceptedBallot;
        }

        public void setValue(Value value) {
            this.value = value;
        }

    }

    private Map<Integer, Instance> instanceState = new HashMap<Integer, Instance>();
    private Map<Integer, Value> acceptedValue = new HashMap<Integer, Value>();
    private transient int id;
}


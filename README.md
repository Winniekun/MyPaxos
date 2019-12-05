## Paxos理解以及实现

### 简介

**Paxos算法**是[莱斯利·兰伯特](https://zh.wikipedia.org/wiki/莱斯利·兰伯特)（英语：Leslie Lamport，[LaTeX](https://zh.wikipedia.org/wiki/LaTeX)中的“La”）于1990年提出的一种基于消息传递且具有高度容错特性的一致性算法。

#### Basic Paxos

###### 角色介绍:

1. Client: 系统外部角色,  请求发起者,  像民众
2. Propser: 接受Client的请求, 向集群提出提议(propose). 并在冲突发生时, 起到冲突调节的作用, 像议员, 替民众提出议案
3. Accpetor(不关乎内容, 只确定编号是否冲突): 对提议进行投票, 只有在形成法定人数(Quorum, 一般即为majorit多数派)时, 提议才会最终被接受. 类似国会
4. Learner: 提议的接受者,  backup , 对集群一致性没有什么影响, 像记录员.

###### 步骤, 阶段(phases)

1. Phase 1a: Prepare(议员提出讨论编号为N的提案)
   1. proposer提出一个议案, 编号为N, 此N大于这个proposer之前提出的提案编号, 请求accepoters quorum接受.
2. Phase 1b: Promise(国会内的人, 确定之前提案的均小于N, 则该提案可以进行讨论, 否者pass)
   1. 如果N大于此acceptor之前接受的任何提案编号则接受, 否者拒绝
3. Phase 2a: Accept(通过Phase1b确定该提案是否值得去讨论, 之后)
   1. 如果达到了多数派,  proposer会发出accept请求, 此请求包含提案编号N, 以及提案内容
4. Phase 2b: Accepted
   1. 若果此acceptor在此期间没有收到任何编号大于N的提案, 则接受次提案内容, 否者忽略

###### 图形化流程(维基百科) --- 正常情况

```
Client   Proposer      Acceptor     Learner
   |         |          |  |  |       |  | --- First Request ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Prepare(N)
   |         |<---------X--X--X       |  |  Promise(N,I,{Va,Vb,Vc})
   |         X--------->|->|->|       |  |  Accept!(N,I,V)
   |         |<---------X--X--X------>|->|  Accepted(N,I,V)
   |<---------------------------------X--X  Response
   |         |          |  |  |       |  |
```

###### 图形化流程(维基百科) --- 部分节点失败

```
Client   Proposer      Acceptor     Learner
   |         |          |  |  |       |  | --- First Request ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Prepare(1)
   |         |          |  |  |       |  |  !!FAIL!!        
   |         |<---------X--X          |  |  Promise(1,{null,null})
   |         X--------->|->|          |  |  Accept!(1, V)
   |         |<---------X--X--X------>|->|  Accepted(1, V)
   |<---------------------------------X--X  Response
   |         |          |  |  |       |  |
```

###### 图形化流程(维基百科) --- Proposer失败

```
Client   Proposer      Acceptor     Learner
   |         |          |  |  |       |  | --- First Request ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Prepare(1)
   |         |<---------X--X--X       |  |  Promise(1,{null, null, null})
   |         |          |  |  |       |  |
   |         |          |  |  |       |  |  !! Leader fails during broadcast !!
   |         X--------->|  |  |       |  |  Accept!(1, Va)
   |         |          |  |  |       |  |  
   |           |        |  |  |       |  |  !! New LEADER !! 
   |           X------->|->|--|       |  |  Prepare(2)
   |           |<-------X--X--X       |  |  Promise(2, {null, null , null})
   |           X------->|->|->|       |  |  Accepted(2, V)
   |           |<-------X--X--X------>|->|  Accepted(2,V)
   |<---------------------------------X--X  Response
   |         |          |  |  |       |  |
```

###### 潜在问题, 活锁(liveness)或dueling

ProposerA, ProposerB轮流提出提案

```
Client   Proposer      Acceptor     Learner
   |         |          |  |  |       |  | --- First Request ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Prepare(1)
   |         |<---------X--X--X       |  |  Promise(1, {null, null, null})
   |         |          |  |  |       |  |  !! LEADER FAILS !!
   |           |        |  |  |       |  |  !! NEW LEADER (it knows last numner was 1)
   |           X------->|->|->|       |  |  Prepare(2)
   |           |<-------X--X--X       |  |  Promise(2, {null, null, null})
   |         | |        |  |  |       |  |  !! OLD LEADER RECOVER
   |         | |        |  |  |       |  |  !! OLD LEADER tries 2 , denied!!
   |         X--------->|->|->|       |  |  Prepare(2)
   |         |<---------X--X--X       |  |  Nack(2)
   |         | |        |  |  |       |  |  !! tires 3
   |         X--------->|->|->|       |  |  Prepare(3)
   |         |<---------X--X--X       |  |  Promise(3, {null, null, null})
   |         | |        |  |  |       |  |  !! NEW LEADER propose, denied!!
   |         | X------->|->|->|       |  |  Accept(2, Va)
   |         | |<-------X--X--X       |  |  Nack(3)
   |         | |        |  |  |       |  |  !! tires 4
   |         | X------->|->|->|       |  |  Prepare(4)
   |         | |<-------X--X--X       |  |  Promise(4, {null, null, null})
   |         | |        |  |  |       |  |  !! OLD LEADER propose, denied!!
   |         X -------->|->|->|       |  |  Accept(3, Vb)
   |         | <--------X--X--X       |  |  Nack(4)
   |         | |        |  |  |       |  |  !! tires 5
   |         X--------->|->|->|       |  |  Prepare(5)
   |         | |<-------X--X--X       |  |  Promise(5, {null, null, null})
   
   
```

**其中一个Proposer等待一个random时间即可解决**

###### Basic Paxos的问题

1. 难实现
2. 效率低(2轮RPC调用)
3. 活锁

.....

#### Multi Paxos

###### 角色介绍

新概念: Leader: **唯一一个propser**, 所有的请求都需经过此Leader, 可以解决活锁问题

###### 基础介绍

Prepare() 可以理解为竞选Leader

```
Client   Proposer      Acceptor     Learner
   |         |          |  |  |       |  | --- First Request ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Prepare(N)
   |         |<---------X--X--X       |  |  Promise(N,I,{Va,Vb,Vc})
   |         X--------->|->|->|       |  |  Accept!(N,I,V)
   |         |<---------X--X--X------>|->|  Accepted(N,I,V)
   |<---------------------------------X--X  Response
   |         |          |  |  |       |  |
   
   
------------------------------------------------------------------------------------


Client   Proposer       Acceptor     Learner
   |         |          |  |  |       |  |  --- Following Requests ---
   X-------->|          |  |  |       |  |  Request
   |         X--------->|->|->|       |  |  Accept!(N,I+1,W)
   |         |<---------X--X--X------>|->|  Accepted(N,I+1,W)
   |<---------------------------------X--X  Response
   |         |          |  |  |       |  |
```

###### 减少角色, 进一步优化

```
Client     Acceptor   
   |        |  |  |     --- First Request
   X------->|  |  |     Request
   |        X->|->|     Prepare(N)
   |        |<-X--X     Promise(N, I, {Va, Vb})
   |        X->|->|     Accept!(N, I, Vn)
   |        |<-X--X     Accepted(N, I)
   |<-------X  |  |     Response
   |        |  |  | 
```

#### Raft

共划分为三个子问题

* Leader Election
* Log Replication
* Safety

重新定义角色(状态):

* Leader
* Follower
* Candidate

[动画展示](http://thesecretlivesofdata.com/raft/)







## reference

* [维基百科]([https://zh.wikipedia.org/wiki/Paxos%E7%AE%97%E6%B3%95](https://zh.wikipedia.org/wiki/Paxos算法))
* [The Paxos Algorithm](https://www.youtube.com/watch?v=d7nAGI_NZPk)
* [可靠分布式系统基础Paxos的直观解释](https://drmingdrmer.github.io/tech/distributed/2015/11/11/paxos-slide.html)
* paxos-simple.pdf


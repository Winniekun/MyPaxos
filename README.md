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



#### 方案1： 确定一个不可变变量的取值

**先考虑系统由单个Acceptor组成，通过列斯互斥锁机制，来管理并发的proposer运行**

1. Proposer首先向acceptor申请acceptor的互斥访问权限，然后才能请求Acceptor接受自己的取值
2. 让proposer按照获取互斥访问权的顺序，依次访问acceptor。
3. 一旦Acceptor接受了某个Proposer的取值，则认为var值被确定 ， 其他的Proposer不再更改

**基于互斥访问权的Acceptor的实现**

* Acceptor保存变量var和一个互斥锁lock
* Acceptor::prepare():
  * 加互斥锁，给予var的互斥访问权，并返回var当前的取值f。
* Acceptor::release()
  * 解互斥锁，收回var的互斥访问权
* Acceptor::accept(var, V):
  * 如果已经加锁，并且var没有取值，则 设置var为V。并且释放锁。

**propose(var, V)的两阶段实现**

* 第一阶段： 通过 Acceptor::prepare获取互斥访问权和当前的var的取值
  * 如果不能， 返回\<error>(锁被别人占用)
* 第二阶段： 根据当前var的取值f，选择执行：
  * 如果f为Null，则通过Acceptor::accept(var, V)提交数据V
  * 如果f不为空， 则通过Acceptor::release()释放访问权， 返回\<ok, f>
* 



## reference

* [维基百科]([https://zh.wikipedia.org/wiki/Paxos%E7%AE%97%E6%B3%95](https://zh.wikipedia.org/wiki/Paxos算法))
* [The Paxos Algorithm](https://www.youtube.com/watch?v=d7nAGI_NZPk)
* [可靠分布式系统基础Paxos的直观解释](https://drmingdrmer.github.io/tech/distributed/2015/11/11/paxos-slide.html)
* paxos-simple.pdf
* [微信自研生产级paxos类库PhxPaxos实现原理介绍](https://mp.weixin.qq.com/s?__biz=MzI4NDMyNTU2Mw==&mid=2247483695&idx=1&sn=91ea422913fc62579e020e941d1d059e#rd)
* [paxos和分布式系统](https://www.bilibili.com/video/av61253978/?spm_id_from=333.788.videocard.6)


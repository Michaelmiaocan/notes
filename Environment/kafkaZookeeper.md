# kafka与zookeeper单机集群搭建

* [Zookeeper环境搭建](#一zookeeper环境搭建)
* [Kafka环境搭建](#二Kafka环境搭建)
* [Kafka常用命令](#三Kafka常用命令)
* [Kafka监控软件](#四Kafka监控软件)
* [测试](#五测试)

## 一、Zookeeper环境搭建
> Zookeeper是一种在分布式系统中被广泛用来作为分布式状态管理、分布式协调管理、
分布式配置管理、和分布式锁服务的集群。kafka增加和减少服务器都会在Zookeeper节点上触发相应的事件kafka系统会捕获这些事件，
进行新一轮的负载均衡，客户端也会捕获这些事件来进行新一轮的处理，所以启动Kafka之前需要安装并启动zookeeper。

Zookeeper的安装和配置十分简单, 既可以配置成单机模式, 也可以配置成集群模式. 下面将分别进行介绍.
### 1. 单机模式
[下载](https://zookeeper.apache.org/releases.html)zookeeper的安装包之后, 解压到合适目录. 进入zookeeper目录下的conf子目录, 创建zoo.cfg进行如下配置:
```
tickTime=2000
dataDir=/opt/zookeeper/data
dataLogDir=/opt/zookeeper/logs
clientPort=2181
```
参数说明:
* tickTime: zookeeper中使用的基本时间单位, 毫秒值.
* dataDir: 数据目录. 可以是任意目录.
* dataLogDir: log目录, 同样可以是任意目录. 如果没有设置该参数, 将使用和dataDir相同的设置.
* clientPort: 监听client连接的端口号.

到现在为止，zookeeper的单机模式已经配置好了. 启动server只需运行以下脚本:
```
bin/zkServer.sh start conf/zoo.cfg
```
查看服务状态:
```
bin/zkServer.sh status conf/zoo.cfg
```

Server启动之后, 就可以启动client连接server了, 启动客户端可查看具体信息:
```
bin/zkCli.sh -server localhost:2181
```

### 2. 伪集群模式
所谓伪集群, 是指在单台机器中启动多个zookeeper进程, 并组成一个集群. 以启动3个zookeeper进程为例.

将conf/目录下的zoo.cfg复制两份，如下所示:
```
|--zoo.cfg
|--zoo1.cfg
|--zoo2.cfg
```
更改conf/zoo.cfg文件如下:
```
tickTime=2000
initLimit=5
syncLimit=2
dataDir=/opt/zookeeper/data
dataLogDir=/opt/zookeeper/logs
clientPort=2181
server.0=127.0.0.1:2888:3888
server.1=127.0.0.1:2889:3889
server.2=127.0.0.1:2890:3890
```
更改conf/zoo1.cfg文件如下:
```
tickTime=2000
initLimit=5
syncLimit=2
dataDir=/opt/zookeeper/data1
dataLogDir=/opt/zookeeper/logs1
clientPort=2182
server.0=127.0.0.1:2888:3888
server.1=127.0.0.1:2889:3889
server.2=127.0.0.1:2890:3890
```
更改conf/zoo2.cfg文件如下:
```
tickTime=2000
initLimit=5
syncLimit=2
dataDir=/opt/zookeeper/data2
dataLogDir=/opt/zookeeper/logs2
clientPort=2183
server.0=127.0.0.1:2888:3888
server.1=127.0.0.1:2889:3889
server.2=127.0.0.1:2890:3890
```
新增了几个参数, 其含义如下:
* initLimit:  zookeeper集群中的包含多台server, 其中一台为leader, 集群中其余的server为follower. initLimit参数配置初始化连接时, follower和leader之间的最长心跳时间. 此时该参数设置为5, 说明时间限制为5倍tickTime, 即5*2000=10000ms=10s.
* syncLimit:  该参数配置leader和follower之间发送消息, 请求和应答的最大时间长度. 此时该参数设置为2, 说明时间限制为2倍tickTime, 即4000ms.
* server.X=A:B:C 其中X是一个数字, 表示这是第几号server. A是该server所在的IP地址. B配置该server和集群中的leader交换消息所使用的端口. C配置选举leader时所使用的端口. 由于配置的是伪集群模式, 所以各个server的B, C参数必须不同.

在之前设置的dataDir中新建myid文件, 写入一个数字, 该数字表示这是第几号server. 该数字必须和zoo.cfg文件中的server.X中的X一 一对应,如下所示:
```
|--data  >> 0
|--data1 >> 1
|--data2 >> 2
```
分别启动三个server:
```
bin/zkServer.sh start conf/zoo.cfg
bin/zkServer.sh start conf/zoo1.cfg
bin/zkServer.sh start conf/zoo2.cfg
```
任意选择一个server目录, 启动客户端可查看具体信息:
```
bin/zkCli.sh -server localhost:2181
```

### 集群模式
集群模式的配置和伪集群基本一致
由于集群模式下, 各server部署在不同的机器上, 因此各server的conf/zoo.cfg文件可以完全一样,如下所示:
```
tickTime=2000
initLimit=5
syncLimit=2
dataDir=/opt/zookeeper/data
dataLogDir=/opt/zookeeper/logs
clientPort=2181
server.0=10.181.65.180:2888:3888
server.1=10.181.65.180:2888:3888
server.2=10.181.65.180:2888:3888
```
> 注意事项: 无论是集群还是伪集群模式都要配置相应的myid文件

## 二、Kafka环境搭建
> Kafka是一个分布式消息系统，由linkedin使用scala编写，用作LinkedIn的活动流（Activity Stream）和运营数据处理管道（Pipeline）的基础,具有高水平扩展和高吞吐量

## 三、Kafka常用命令
## 四、Kafka监控软件
## 五、测试

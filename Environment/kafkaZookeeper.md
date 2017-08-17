# kafka与zookeeper单机集群搭建

* [Zookeeper环境搭建](#一zookeeper环境搭建)
* [Kafka环境搭建](#二kafka环境搭建)
* [Kafka常用命令](#三kafka常用命令)
* [Kafka监控软件](#四kafka监控软件)
* [测试](#五测试)
* [参考文档](#六参考文档)

## 一、Zookeeper环境搭建
> Zookeeper是一种在分布式系统中被广泛用来作为分布式状态管理、分布式协调管理、
分布式配置管理、和分布式锁服务的集群。kafka增加和减少服务器都会在Zookeeper节点上触发相应的事件kafka系统会捕获这些事件，
进行新一轮的负载均衡，客户端也会捕获这些事件来进行新一轮的处理，所以启动Kafka之前需要安装并启动zookeeper。

Zookeeper的安装和配置十分简单, 既可以配置成单机模式, 也可以配置成集群模式. 下面将分别进行介绍.
### 1. 单机模式
[下载](https://zookeeper.apache.org/releases.html) zookeeper的安装包之后, 解压到合适目录. 进入zookeeper目录下的conf子目录, 创建zoo.cfg进行如下配置:
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

[下载](http://kafka.apache.org/) Kafka的安装包之后, 解压到合适目录，Kafka和zookeeper一样可以分为单机模式、
伪集群模式和集群模式，因为都大同小异，这里只介绍一下集群模式的搭建.

将下载的Kafka软件解压进入config目录下，主要关注server.properties 这个文件即可，修改配置文件:
```
broker.id=0     #当前机器在集群中的唯一标识，和zookeeper的myid性质一样
port=9092      #当前kafka对外提供服务的端口默认是9092
host.name=10.181.65.180       #这个参数默认是关闭的，在0.8.1有个bug，DNS解析问题，失败率的问题。
num.network.threads=3         #这个是borker进行网络处理的线程数
num.io.threads=8              #这个是borker进行I/O处理的线程数
log.dirs=/opt/kafka/kafkalogs/       #消息存放的目录，这个目录可以配置为“，”逗号分割的表达式，上面的num.io.threads要大于这个目录的个数这个目录，如果配置多个目录，新创建的topic他把消息持久化的地方是，当前以逗号分割的目录中，那个分区数最少就放那一个
socket.send.buffer.bytes=102400      #发送缓冲区buffer大小，数据不是一下子就发送的，先回存储到缓冲区了到达一定的大小后在发送，能提高性能
socket.receive.buffer.bytes=102400   #kafka接收缓冲区大小，当数据到达一定大小后在序列化到磁盘
socket.request.max.bytes=104857600   #这个参数是向kafka请求消息或者向kafka发送消息的请请求的最大数，这个值不能超过java的堆栈大小
num.partitions=1                     #默认的分区数，一个topic默认1个分区数
log.retention.hours=168              #默认消息的最大持久化时间，168小时，7天
message.max.byte=5242880             #消息保存的最大值5M
default.replication.factor=2         #kafka保存消息的副本数，如果一个副本失效了，另一个还可以继续提供服务
replica.fetch.max.bytes=5242880      #取消息的最大直接数
log.segment.bytes=1073741824         #这个参数是：因为kafka的消息是以追加的形式落地到文件，当超过这个值的时候，kafka会新起一个文件
log.retention.check.interval.ms=300000         #每隔300000毫秒去检查上面配置的log失效时间（log.retention.hours=168 ），到目录查看是否有过期的消息如果有，删除
log.cleaner.enable=false                       #是否启用log压缩，一般不用启用，启用的话可以提高性能
zookeeper.connect=10.181.65.180:2181,0.181.65.181:2181,0.181.65.182:2181             #设置zookeeper的连接端口
```
具体的参数详细信息可以参考[Kafka Broker配置](http://orchome.com/12)

这只是其中一个broker的配置，其余broker配置一样只需要修改相应的broker.id和host.name就行啦


## 三、Kafka常用命令
启动服务
```
bin/kafka-server-start.sh config/server.properties
bin/kafka-server-start.sh -daemon config/server.properties #从后台启动Kafka集群（3台都需要启动）
```


## 四、Kafka监控软件

## 五、测试

## 六、参考文档
* [kafka官方文档](http://kafka.apache.org/quickstart)
* [OrcHome](http://orchome.com/)
* [kafka源码](https://github.com/apache/kafka/)
* [胡夕博客](http://www.cnblogs.com/huxi2b/tag/Kafka/)
* [阿里中间件团队博客](http://jm.taobao.org/)
* [confluent](https://www.confluent.io/)

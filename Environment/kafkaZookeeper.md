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
* 启动服务
```
bin/kafka-server-start.sh config/server.properties  #普通启动
bin/kafka-server-start.sh -daemon config/server.properties #从后台启动Kafka集群（3台都需要启动）
```

* 新建主题
```
bin/kafka-topics.sh --create --zookeeper 10.181.65.180:2181 --replication-factor 3 --partitions 3 --topic my-replicated-topic
```

* 查看主题信息
```
bin/kafka-topics.sh --describe --zookeeper 10.181.65.180:2181 --topic my-replicated-topic
```

* 查看主题列表
```
bin/kafka-console-producer.sh --broker-list 10.181.65.180:9092 --topic my-replicated-topic
```

* 启动生产者
```
 bin/kafka-console-producer.sh --broker-list 10.181.65.180:9092 --topic my-replicated-topic
```

* 启动消费者
```
bin/kafka-console-consumer.sh --bootstrap-server 10.181.65.180:9092 --from-beginning --topic my-replicated-topic
```

## 四、Kafka监控软件

### 1.KafkaMonitor
> KafkaOffsetMonitor是一个可以用于监控Kafka的Topic及Consumer消费状况的工具，其配置和使用特别的方便,[源项目Github地址](https://github.com/quantifind/KafkaOffsetMonitor).

最简单的使用方式是从Github上下载一个最新的[KafkaOffsetMonitor-assembly-0.2.1.jar](https://github.com/quantifind/KafkaOffsetMonitor/releases/download/v0.2.1/KafkaOffsetMonitor-assembly-0.2.1.jar)，
上传到某服务器上，然后执行一句命令就可以运行起来。

KafkaOffsetMonitor的使用
因为完全没有安装配置的过程，所以直接从KafkaOffsetMonitor的使用开始。
将KafkaOffsetMonitor-assembly-0.2.0.jar上传到服务器后，可以新建一个脚本用于启动该应用。脚本内容如下:
```
java -cp KafkaOffsetMonitor-assembly-0.2.0.jar \
    com.quantifind.kafka.offsetapp.OffsetGetterWeb \
    --zk 10.181.65.180:2181,10.181.65.181:2181,10.181.65.182:2181 \
    --port 8080 \
    --refresh 10.seconds \
    --retain 2.days
```

各参数的作用可以参考一下Github上的描述：
* offsetStorage:  valid options are ”zookeeper”, ”kafka” or ”storm”. Anything else falls back to ”zookeeper”
* zk:  the ZooKeeper hosts
* port: on what port will the app be available
* refresh:  how often should the app refresh and store a point in the DB
* retain:  how long should points be kept in the DB
* dbName:  where to store the history (default ‘offsetapp’)
* kafkaOffsetForceFromStart:  only applies to ”kafka” format. Force KafkaOffsetMonitor to scan the commit messages from start (see notes below)
* stormZKOffsetBase:  only applies to ”storm” format. Change the offset storage base in zookeeper, default to ”/stormconsumers” (see notes below)
* pluginsArgs:  additional arguments used by extensions (see below)

然后访问8080端口即可

### 2.kafka Manager
#### 运行时环境要求
1. Kafka 0.8.1.1+
2. Java 7+
#### 功能
为了简化开发者和服务工程师维护Kafka集群的工作，yahoo构建了一个叫做Kafka管理器的基于Web工具，
叫做 Kafka Manager（[GitHub源码地址](https://github.com/yahoo/kafka-manager)）。这个管理工具可以很容易地发现分布在集群中的哪些topic分布不均匀，
或者是分区在整个集群分布不均匀的的情况。它支持管理多个集群、选择副本、副本重新分配以及创建Topic。
同时，这个管理工具也是一个非常好的可以快速浏览这个集群的工具，有如下功能：
1. 管理多个kafka集群
2. 便捷的检查kafka集群状态(topics,brokers,备份分布情况,分区分布情况)
3. 选择你要运行的副本
4. 基于当前分区状况进行
5. 可以选择topic配置并创建topic(0.8.1.1和0.8.2的配置不同)
6. 删除topic(只支持0.8.2以上的版本并且要在broker配置中设置delete.topic.enable=true)
7. Topic list会指明哪些topic被删除（在0.8.2以上版本适用）
8. 为已存在的topic增加分区
9. 为已存在的topic更新配置
10. 在多个topic上批量重分区
11. 在多个topic上批量重分区(可选partition broker位置)

#### 安装,配置,启动
[下载zip](https://github.com/yahoo/kafka-manager/releases/tag/1.3.3.13)文件，解压后在conf/application.conf中将
kafka-manager.zkhosts的值设置为你的zk地址
```
kafka-manager.zkhosts 10.181.65.180:2181,10.181.65.181:2181,10.181.65.182:2181
```
启动,指定配置文件位置和启动端口号，默认为9000
```
nohup bin/kafka-manager
-Dconfig.file=conf/application.conf
 -Dhttp.port=8081 &
```
然后访问8081端口即可

## 五、测试



## 六、参考文档
* [kafka官方文档](http://kafka.apache.org/quickstart)
* [OrcHome](http://orchome.com/)
* [kafka源码](https://github.com/apache/kafka/)
* [胡夕博客](http://www.cnblogs.com/huxi2b/tag/Kafka/)
* [阿里中间件团队博客](http://jm.taobao.org/)
* [confluent](https://www.confluent.io/)

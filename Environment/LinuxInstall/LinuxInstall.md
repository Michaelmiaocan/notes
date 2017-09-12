# Ubuntu虚拟机安装

## 一、VM下安装ubuntu16.04

打开VMware Workstation，点击创建新的虚拟机

![创建新的虚拟机](1.png)

选择自定义，点下一步，如下图所示：

![自定义](2.png)

点下一步，如下图所示,这一步可以设置虚拟机的兼容性可以很好的设置向下兼容：

![](3.png)

选择**安装程序光盘文件**，如下图所示：

![安装程序光盘文件](4.png)

新建虚拟机向导并设置用户名密码：

![](5.png)

命名虚拟机：

![](6.png)

配置处理器：

![](7.png)

配置内存:

![](8.png)

设置网络为NAT模式（经过测试直连账号NAT和桥接模式虚拟机都可以联网）:

![](9.png)

选择I/O控制器类型：

![](10.png)

选择磁盘类型：

![](11.png)

创建新磁盘：

![](12.png)

制定磁盘容量（这里最好设置的大一点，以后扩容比较麻烦）:

![](13.png)

创建虚拟机:

![](14.png)

之后就会自动安装了，安装过程中可能遇到一些包下载过慢，如果实在等不得就点击skip跳过，等装好进虚拟机里面更新：

![](15.png)

### 优化：
虚拟机用了一段时间以后会变得比较卡这时候可以i通过磁盘清理一下，如下图所示：

![](磁盘整理.png)

## 二、安装后基本配置

### 1.更换国内源

选择软件更新：

![](更换源.png)

这里进去测速后选择最快的源：

![](chooseServer.png)

### 2. 更新系统
```
sudo apt-get update

sudo apt-get upgrade

# 安装一些通用开发包
sudo apt-get install -y python build-essential curl automake autoconf libtool

# 安装git
sudo apt-get install -y git

# 安装svn
sudo apt-get install subvison
```

### 3.多安装些浏览器
```
sudo apt-get install chromium-browser   ##Chronium浏览器

# 下载最新稳定版的Google
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
```


### 4.nodejs环境安装
[下载](https://nodejs.org/dist/v6.11.3/node-v6.11.3-linux-x64.tar.xz)nodejs二进制安装文件，执行以下命令：
``` lua
cd /home/linkage/Documents   #进入二进制文件目录

xz -d node-v6.11.3-linux-x64.tar.xz     #解压 tra.xz文件

tar -xvf node-v6.11.3-linux-x64.tar -C ../software/   #解压tar文件到指定目录

vim ~/.bashrc       #设置环境变量

#将下面两个配置粘贴在文档最下面，NODE路径根据自己解压的nodejs文件目录为准#
export NODE=/home/linkage/software/node-v6.11.3-linux-x64
export PATH=$NODE/bin:$PATH

执行  source ~/.bashrc 让刚才的配置生效

#执行下面命令验证：
node -v
npm -v

#nodejs配置：
1.npm 淘宝镜像配置
    npm config set registry http://registry.npm.taobao.org
    npm info underscore
2.全局安装需要的模块
    npm install -g npm
    npm install -g node-gyp
    npm install -g pm2
    npm install -g gulp
    npm install -g bower
    npm install -g forever
    npm install -g grunt-cli
```

### 5.go环境安装
[下载](https://studygolang.com/dl/golang/go1.9.linux-amd64.tar.gz) go语言安装压缩文件
执行以下命令：
```
cd /home/linkage/Documents   #进入压缩文件目录

tar -zxvf go1.8.3.linux-amd64.tar.gz -C ../software/  #解压文件到指定目录

vim ~/.bashrc       #设置环境变量

#将下面配置粘贴在文档最下面，GOROOT指的是解压后的安装文件路径，GOPATH指的是go的工作目录#
export GOROOT=/home/linkage/software/go
export GOPATH=/home/linkage/software/gopath
export PATH=$GOROOT/bin:$PATH

执行  source ~/.bashrc 让刚才的配置生效

#执行下面命令验证：
go version
go env
```

### 6.IPFS 源码环境安装
```
go get -u -d github.com/ipfs/go-ipfs   #下载go-ipfs

cd $GOPATH/src/github.com/ipfs/go-ipfs

make install # 安装依赖并编译

cd $GOPATH/bin    #会自动生成可执行文件ipfs,修改代码后再执行make install生成新的ipfs执行文件
```

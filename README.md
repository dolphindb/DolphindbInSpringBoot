# 基于DolphinDB搭建微服务的SpringBoot项目

SpringBoot 是一个基于 Spring 的快速开发框架，也是 SpringCloud 构建微服务分布式系统的基础设施。本文主要介绍如何通过 SpringBoot 快速搭建 DolphinDB 微服务，并且基于 Mybatis 操作 DolphinDB 数据库。

本项目实现物联网中的一个典型场景：

1.  终端设备通过 MQTT 协议发送数据到后端服务器；

2.  服务器接收到数据后写入内存中的实时数据表；

3.  实时数据表将数据写入到磁盘中的历史数据表；

4.  用户查询近期的实时数据；

5.  用户查询一年的历史数据。

**环境配置：**

+ JDK 1.8

+ Sprinboot 2.7.3

+ DolphinDB 2.00.7(Linux)

+ DolphinDBJDBC 130.19.1: 数据库连接工具

+ DolphinDBGUI 1.30.19.2: DolphinDB 客户端工具

+ EMQX 5.0.6: MQTT 消息服务器

+ MQTTX 1.8.2: MQTT 客户端

+ Postman 9.12.2 测试工具

+ 项目的源码:[DolphinDBJDBCDemo](https://gitee.com/dolphindb/DolphindbInSpringBoot)

## 1. 创建 SpringBoot 项目

### 1.1 新建项目

首先我们通过 IDEA 创建 Springboot 项目，根据下图依次检查选项内容。

[下载 IDEA Ultimate 版本](https://www.jetbrains.com/idea/)

![img](image/1_1_01.png?inline=false)

### 1.2 添加项目依赖

如下图所示，在左侧搜索框中搜索并添加项目需要的依赖（红色方框中的内容）。之后点击 Create 创建项目。

![img](image/1_2_01.png?inline=false)

### 1.3 确认 pom 文件

等待项目加载完成，确认 pom 文件中的依赖和版本号是否正确：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.dolphindb</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>demo</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.2.2</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
### 1.4 添加 fastjson 和 mqtt 模块

在 pom 文件中，参照如下代码添加 fastjson 和 mqtt 依赖。添加依赖后可点击右上角刷新按钮，下载相应的依赖包。

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.2</version>
</dependency>
```
### 1.5 新建目录

如下图所示，在项目中创建需要的目录：

![img](image/1_5_01.png?inline=false)

项目整体框架搭建好后，下面进行数据库的准备工作。

## 2. 创建 DolphinDB 数据库

### 2.1 下载 DolphinDB

打开 DolphinDB 官网 ([DolphinDB 丨浙江智臾科技有限公司](https://www.dolphindb.cn/)) 下载 DolphinDB server (Linux)。下载后将其上传到 Linux 服务器，解压并运行 dolphindb/server/dolphindb，开启服务。关于 DolphinDB 数据库部署的详细介绍请参考[部署教程](https://gitee.com/dolphindb/Tutorials_CN/blob/master/standalone_server.md)。

若结果如下图所示，则表示成功开启 DolphinDB 服务。

![img](image/2_1_01.png?inline=false)

### 2.2 创建数据库，建立实时数据表和历史数据表

在 DolphinDB 官网下载并运行 DolphinDB GUI。关于 GUI 的下载及使用方法请参考 [部署教程](https://gitee.com/dolphindb/Tutorials_CN/blob/master/standalone_server.md)。

下载 [建库建表脚本](script)，并将脚本拷贝到 GUI 中运行。参考 [编程语言介绍](https://gitee.com/dolphindb/Tutorials_CN/blob/master/hybrid_programming_paradigms.md) 了解更多 DolphinDB 编程语言。

在执行脚本之前，需配置流表数据持久化的目录路径（在 DolphinDB/server/dolphindb.cfg 文件中添加 `persistenceDir=/home/Software/DolphinDB/data`，根据实际情况调整目录即可）。当内存中的流数据表的行数超过持久化流表（[enableTableShareAndPersistence](https://www.dolphindb.cn/cn/help/FunctionsandCommands/CommandsReferences/e/enableTableShareAndPersistence.html)）设置值时，系统会将内存中的部分数据持久化到配置的目录中。

如下图所示，完成数据库的创建。

![img](image/2_2_01.png?inline=false)

DolphinDB 还提供其他形式的客户端，详情参考 [DolphinDB 客户端软件教程](https://gitee.com/dolphindb/Tutorials_CN/blob/master/client_tool_tutorial.md)。

建立两个表：

1. 内存中的实时数据表（也就是 DolphinDB 中的流表），用于快速接收实时数据。建表语句中设置内存中最多保留的实时数据的大小不超过 10 万条；

2. 磁盘上的历史数据表（也就是 DolphinDB 中的分布式表），实时数据表新增的数据会自动、定期写入历史数据表中。历史数据表的大小没有限制。

这两个表的结构一致，一共 12 个字段，time 表示时间戳，id 为设备唯一编码，其他字段可以是一些采集的物理量（如温度、湿度等数据)。

![img](image/2_2_02.png?inline=false)

创建两个表的方案，既能满足快速写入和实时查询的需要，也能满足大批数据查询的需要，适用于大部分应用场景。

### 2.3 添加 DolphinDB JDBC 依赖

pom 文件中添加 JDBC 依赖并且刷新 Maven。

```xml
<dependency>
    <groupId>com.dolphindb</groupId>
    <artifactId>jdbc</artifactId>
    <version>1.30.19.1</version>
</dependency>
```
### 2.4 项目配置

在 application.properties 中进行数据库相关属性的配置。本文只配了几个必要的配置，其他参数可自行配置，参数说明详见 [官方配置文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)。请注意，以下参数中 `spring.datasource.url` 请调整为用户实际使用的地址。

```properties
server.port=8888
spring.datasource.url=jdbc:dolphindb://192.168.1.182:8848?databasePath=dfs://DemoDB
spring.datasource.username=admin
spring.datasource.password=123456
spring.datasource.driver-class-name=com.dolphindb.jdbc.Driver
mybatis.mapper-locations=classpath:/mapper/*.xml
mybatis.configuration.auto-mapping-behavior=full
```

运行项目，启动后控制台输出如下：

![img](image/2_4_01.png?inline=false)

完成前面的步骤后，就可以编写具体代码。

## 3. 在 IDEA 中编写代码

本节主要是代码的编写工作。需要在相应的目录下编写代码。

### 3.1 entity 目录

DemoEntity 对应数据库中 Demo 表的实体类，即为本案例中创建的流表和分区表所对应的类。

```java
@Data
public class DemoEntity {
    @JSONField(name="time", format = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    @JSONField(name="id")
    private long id;
    @JSONField(name="f0")
    private float f0;
    @JSONField(name="f1")
    private float f1;
    @JSONField(name="f2")
    private float f2;
    @JSONField(name="f3")
    private float f3;
    @JSONField(name="f4")
    private float f4;
    @JSONField(name="f5")
    private float f5;
    @JSONField(name="f6")
    private float f6;
    @JSONField(name="f7")
    private float f7;
    @JSONField(name="f8")
    private float f8;
    @JSONField(name="f9")
    private float f9;
}
```
### 3.2 dao 目录

定义分区表的数据查询接口：DemoDfsDao

```java
@Mapper
public interface DemoDfsDao {
    List<DemoEntity> queryByTimespan(@Param("start") Date start, @Param("end") Date end);
}
```

定义流表的数据写入和查询接口：DemoStreamDao

```java
@Mapper
public interface DemoStreamDao {
    void insert(DemoEntity demoEntity);
    List<DemoEntity> queryByTimespan(@Param("start") Date start, @Param("end") Date end);
}
```
### 3.3 service 目录

定义分区表的业务层查询接口：DemoDfsService

```java
public interface DemoDfsService {
    List<DemoEntity> queryByTimespan(Date start, Date end);
}
```

定义流表的业务层写入和查询接口：DemoStreamService

```java
public interface DemoStreamService {
    void insert(DemoEntity demoEntity);
    List<DemoEntity> queryByTimespan(Date start, Date end);
}
```
定义分区表的业务层实现：DemoDfsServiceImpl

```java
@Service
public class DemoDfsServiceImpl implements DemoDfsService {
    @Autowired
    DemoDfsDao demoDfsDao;

    @Override
    public List<DemoEntity> queryByTimespan(Date start, Date end) {
        List<DemoEntity> demoEntities = demoDfsDao.queryByTimespan(start, end);
        return demoEntities;
    }
}
```

定义流表的业务层实现类：DemoStreamServiceImpl

```java
@Service
public class DemoStreamServiceImpl implements DemoStreamService {
    @Autowired
    DemoStreamDao demoStreamDao;

    @Override
    public void insert(DemoEntity demoEntity) {
        demoStreamDao.insert(demoEntity);
    }

    @Override
    public List<DemoEntity> queryByTimespan(Date start, Date end) {
        List<DemoEntity> demoEntities = demoStreamDao.queryByTimespan(start, end);
        return demoEntities;
    }
}
```

### 3.4 dto 目录

定义对外数据传输类：QueryArg

```java
@Data
public class QueryArg {
    private Date startTime,endTime;
}
```

### 3.5 controller 目录

展现层类的定义

```java
@RestController
public class DemoController {

    @Autowired
    DemoDfsServiceImpl demoDfsService;

    @Autowired
    DemoStreamServiceImpl demoStreamService;

    @PostMapping("/queryStream")
    public List<DemoEntity> queryStream(@RequestBody() QueryArg arg) {
        return demoStreamService.queryByTimespan(arg.getStartTime(), arg.getEndTime());
    }

    @PostMapping("/queryDfs")
    public List<DemoEntity> queryDfs(@RequestBody() QueryArg arg) {
        return demoDfsService.queryByTimespan(arg.getStartTime(), arg.getEndTime());
    }
}
```

### 3.6 mqtt 目录

Mqtt 客户端的定义，用于连接 EMQX，接收 MQTTX 发过来的消息并插入到数据库中。

```java
@Component
public class DemoMqttClient {
    Logger logger = LoggerFactory.getLogger(DemoMqttClient.class);
    @Autowired
    private DemoStreamService mDemoStreamService;

    private MqttCallback onMessageCallback=new MqttCallback() {
        public void connectionLost(Throwable cause) {
            // 连接丢失后，进行重连
            System.out.println("连接断开，可以做重连");
            try {
                reConnect();
            }catch (Exception e){
                System.out.println("重连失败，错误："+e.toString());
            }
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try{
                // 将接收到的信息转换为实体类对象并插入流表
                List<DemoEntity> demoEntities = JSON.parseArray(new String(message.getPayload()), DemoEntity.class);
                for(DemoEntity entity : demoEntities){
                    mDemoStreamService.insert(entity);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    public final String HOST = "tcp://127.0.0.1:1883"; //ip 地址
    public final String TOPIC = "test"; // 订阅主题
    public final String clientId = "test";  // 客户端 id，不能重复
    private MqttClient client;
    private MqttConnectOptions connOpts;
    private String userName = "admin";// 登录名
    private String passWord = "123456";// 密码

    @PostConstruct
    public void init() {
        start();
    }

    public void start() {
        try {
            client = new MqttClient(HOST, clientId, new MemoryPersistence());
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // 清空 session
            connOpts.setUserName(userName);
            connOpts.setPassword(passWord.toCharArray());
            connOpts.setConnectionTimeout(10);// 设置超时时间
            connOpts.setKeepAliveInterval(20);// 设置会话心跳时间
            client.setCallback(onMessageCallback); // 设置回调函数
            System.out.println("Connecting to broker:" + HOST);
            client.connect(connOpts); // 创立连接
            client.subscribe(TOPIC); // 订阅
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void reConnect() throws Exception {
        if (this.client != null) {
            this.logger.info("开始重连");
            this.client.connect(this.connOpts);
            this.client.subscribe(TOPIC);
        }
    }
}
```

### 3.7 mapper 目录

DemoDfs.xml 描述分区表的查询 SQL 实现。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.dolphindb.demo.dao.DemoDfsDao">

    <select id="queryByTimespan" resultType="com.dolphindb.demo.entity.DemoEntity">
        <![CDATA[
        select time,id,f0,f1,f2,f3,f4,f5,f6,f7,f8,f9
        from DemoDfs
        where time >=#{start} and time <= #{end}
        ]]>
    </select>
</mapper>
```

DemoStream.xml 描述流表的查询和写入 SQL 实现。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.dolphindb.demo.dao.DemoStreamDao">

    <select id="queryByTimespan" resultType="com.dolphindb.demo.entity.DemoEntity">
        <![CDATA[
        select time,id,f0,f1,f2,f3,f4,f5,f6,f7,f8,f9
        from DemoStream
        where time >=#{start} and time <= #{end}
        ]]>
    </select>
    <insert id="insert" parameterType="com.dolphindb.demo.entity.DemoEntity">
        insert into DemoStream values(#{time},#{id},#{f0},#{f1},#{f2},#{f3},#{f4},#{f5},#{f6},#{f7},#{f8},#{f9})

    </insert>
</mapper>
```

全部代码文件编写完成后，显示如下目录结构：

![img](image/3_7_01.png?inline=false)

## 4.MQTT 数据连接

### 4.1 下载 EMQX

官网下载 EMQX [EMQX: 大规模分布式物联网 MQTT 消息服务器](https://www.emqx.io/zh)。

下载 EMQX 完成后解压，在文件夹的 bin 目录处输入 cmd，打开命令窗口：

![img](image/4_1_01.png?inline=false)

在命令窗口中执行 emqx start，开启 EMQX：

![img](image/4_1_02.png?inline=false)

打开浏览器，输入 localhost:18083 登 EMQX Dashboard 界面，输入用户名密码登录（默认用户名 admin，密码 public）。

![img](image/4_1_03.png?inline=false)

### 4.2 下载 MQTTX

打开 [下载的 MQTTX 客户端](https://mqttx.app/zh)，新建连接，配置连接信息，点击 connect 按钮进行连接。连接后在 EMQX Dashboard 的连接管理页面查看连接成功的客户端。

![img](image/4_2_01.png?inline=false)

至此，基于 DolphinDB 的 Springboot 项目已经搭建完成，接下来我们将使用一个简单的例子进行测试。

## 5. 测试

通过 MQTTX 客户端模拟物联网设备，发送实时数据至服务器。

通过 Postman 模拟前端网页调用后端接口，实现以下查询：

* 查询 1 小时内收到的实时数据；
* 查询 1 年内的历史数据。

### 5.1 数据说明

本教程只是简单模拟了物联网设备的数据集结构，模拟的数据规模也十分小，仅供参考。

<table>
   <tr>
      <td><b>序号</b></td>
      <td><b>字段名</b></td>
      <td><b>字段类型</b></td>
      <td><b>字段说明</b></td>
   </tr>
   <tr>
      <td>1</td>
      <td>time</td>
      <td>DATE</td>
      <td>数据采集时间戳</td>
   </tr>
   <tr>
      <td>2</td>
      <td>id</td>
      <td>LONG</td>
      <td>设备唯一标识</td>
   </tr>
   <tr>
      <td>3</td>
      <td>f0</td>
      <td>FLOAT</td>
      <td rowspan="10">这些字段可以是一些采集的物理量（如温度、湿度等数据)。</td>
   </tr>
   <tr>
      <td>4</td>
      <td>f1</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>5</td>
      <td>f2</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>6</td>
      <td>f3</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>7</td>
      <td>f4</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>8</td>
      <td>f5</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>9</td>
      <td>f6</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>10</td>
      <td>f7</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>11</td>
      <td>f8</td>
      <td>FLOAT</td>
   </tr>
   <tr>
      <td>12</td>
      <td>f9</td>
      <td>FLOAT</td>
   </tr>
</table>

准备好测试工具，下载 [Postman](https://www.postman.com/downloads/)。

### 5.2 启动项目

首先启动 Springboot 项目。

### 5.3 发送消息

使用 MQTTX 客户端向 EMQX 发送信息，发送消息的 topic 为 test。

发送的消息如下，time 为数据采集的时间，id 为设备编码，f0-f9 为设备的各个参数。

```json
[
  {
    "time": "2022-09-15 20:01:54",
    "id": 7,
    "f0": 51.31,
    "f1": 5.62,
    "f2": 32.64,
    "f3": 1.33,
    "f4": 15.11,
    "f5": 366.45,
    "f6": 5.76,
    "f7": 2.53,
    "f8": 2.14,
    "f9": 203.5
  },
  {
    "time": "2022-09-15 20:01:45",
    "id": 8,
    "f0": 51.31,
    "f1": 54.62,
    "f2": 3.64,
    "f3": 11.83,
    "f4": 15.81,
    "f5": 66.45,
    "f6": 55.56,
    "f7": 52.53,
    "f8": 6.12,
    "f9": 43.51
  }
]
```

### 5.4 验证消息发送

发送成功后可在 GUI 中运行 select * from DemoStream order by time desc，查看插入的数据：

![img](image/5_4_01.png?inline=false)

### 5.5 查询实时数据

打开 postman，以 post 方式发送请求 `http://localhost:8888/queryStream`，查询一小时内的实时数据，下面是请求的格式：

```json
{
    "startTime":"2022-09-15T12:00:00.955+00:00",
    "endTime":"2022-09-15T13:00:00.955+00:00"
}
```

![img](image/5_5_01.png?inline=false)

### 5.6 查询历史数据

以 post 方式发送请求 `http://localhost:8888/queryDfs`，查询一年内的历史数据，下面是请求的格式：

```json
{
    "startTime":"2022-09-15T21:00:00.955+00:00",
    "endTime":"2021-09-15T21:00:00.955+00:00"
}
```

![img](image/5_6_01.png?inline=false)

## 6. 总结

+ DolphinDB 可以很好的支持微服务的应用开发。
+ DolphinDB SQL 可以兼容 MySQL, PostgreSQL 等常见标准 SQL 语句，减少了代码迁移成本。后续 DolphinDB 将支持更多标准 SQL 用法。
+ DolphinDB Java API 性能比 MyBatis 更好。如果用户对性能有要求，建议使用 Java API。
# DolphinDBJDBCDemo
## 介绍
这个Demo主要是搭建一个基于DolphinDB数据库的SpringBoot小项目，通过MQTT发送消息并将数据存储到DolphinDB实时数据表(流表)中，实时数据表将数据写入到磁盘中的历史数据表(分布式表)中，用户可根据实际情况查询实时数据或历史数据。

## 环境以及工具
+ JDK 1.8

+ Sprinboot 2.7.3

+ DolphinDB 2.00.7(Linux)

+ DolphinDBJDBC 130.19.1: 数据库连接工具

+ DolphinDBGUI 1.30.19.2: DolphinDB 客户端工具

+ EMQX 5.0.6: MQTT 消息服务器

+ MQTTX 1.8.2: MQTT客户端

+ Postman 9.12.2 测试工具

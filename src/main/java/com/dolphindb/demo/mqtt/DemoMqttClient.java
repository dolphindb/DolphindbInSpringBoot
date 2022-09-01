package com.dolphindb.demo.mqtt;

import com.alibaba.fastjson.JSON;
import com.dolphindb.demo.entity.DemoEntity;
import com.dolphindb.demo.service.DemoStreamService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class DemoMqttClient {
    Logger logger = LoggerFactory.getLogger(DemoMqttClient.class);
    @Autowired
    private DemoStreamService mDemoStreamService;

    private MqttCallback onMessageCallback=new MqttCallback() {
        public void connectionLost(Throwable cause) {
            // 连接丢失后，一般在这里面进行重连
            System.out.println("连接断开，可以做重连");
            try {
                reConnect();
            }catch (Exception e){
                System.out.println("重连失败，错误："+e.toString());
            }
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try{
                //订阅后得到的消息会执行到这里面
                DemoEntity entity = JSON.parseObject(new String(message.getPayload()),DemoEntity.class);
                entity.setTime(new Date());
                mDemoStreamService.insert(entity);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    public final String HOST = "tcp://127.0.0.1:1883"; //连接ip
    public final String TOPIC = "test"; //订阅队列
    public final String clientId = "test";  //连接id，不能和存在的客户端ID重复
    private MqttClient client;
    private MqttConnectOptions connOpts;
    private String userName = "admin";
    private String passWord = "123456";

    @PostConstruct
    public void init() {
        start();
    }

    public void start() {
        try {
            client = new MqttClient(HOST, clientId, new MemoryPersistence());
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // 清空session
            connOpts.setUserName(userName);
            connOpts.setPassword(passWord.toCharArray());
            connOpts.setConnectionTimeout(10);// 设置超时时间
            connOpts.setKeepAliveInterval(20);// 设置会话心跳时间
            client.setCallback(onMessageCallback); // 设置回调函数
            System.out.println("Connecting to broker: " + HOST);
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

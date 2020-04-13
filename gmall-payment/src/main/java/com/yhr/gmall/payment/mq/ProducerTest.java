package com.yhr.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerTest {

    public static void main(String[] args) throws JMSException {

        /*
        * 1.创建连接工厂
        * 2.创建连接
        * 3.打开连接
        * 4.创建session
        * 5.创建队列
        * 6.创建消息提供者
        * 7.创建消息对象
        * 8.发送消息
        * 9.关闭
        * */

        ActiveMQConnectionFactory activeMQConnectionFactory=new
                ActiveMQConnectionFactory("tcp://192.168.126.11:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();

        /*
        * 第一个参数表示是否开启事务，如果开启事务，则必须提交 session.commit
        * 第二个参数表示事务的相应的配置参数
        * */

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue atguigu = session.createQueue("atguigu");

        MessageProducer producer = session.createProducer(atguigu);

        ActiveMQTextMessage activeMQTextMessage=new ActiveMQTextMessage();
        activeMQTextMessage.setText("好好学习");

        producer.send(activeMQTextMessage);

        producer.close();
        session.close();
        connection.close();
    }
}

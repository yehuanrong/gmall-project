package com.yhr.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTest {

    public static void main(String[] args) throws JMSException {

          /*
        * 1.创建连接工厂
        * 2.创建连接
        * 3.打开连接
        * 4.创建session
        * 5.创建队列
        * 6.创建消息消费者
        * 7.消费消息
        * */

        ActiveMQConnectionFactory activeMQConnectionFactory=new
                ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://192.168.126.11:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();

         /*
        * 第一个参数表示是否开启事务，如果开启事务，则必须提交 session.commit
        * 第二个参数表示事务的相应的配置参数
        * */

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue atguigu = session.createQueue("atguigu");

        MessageConsumer consumer = session.createConsumer(atguigu);

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {

                //如何将消息获取到
                if(message instanceof TextMessage){

                    try {
                        String text = ((TextMessage) message).getText();
                        System.out.println("获取的消息:"+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

package com.yhr.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.OrderDetail;
import com.yhr.bean.OrderInfo;
import com.yhr.bean.enums.OrderStatus;
import com.yhr.bean.enums.ProcessStatus;
import com.yhr.gmall.config.ActiveMQUtil;
import com.yhr.gmall.config.RedisUtil;
import com.yhr.gmall.order.mapper.OrderDetailMapper;
import com.yhr.gmall.order.mapper.OrderInfoMapper;
import com.yhr.gmall.util.HttpClientUtil;
import com.yhr.service.OrderService;
import com.yhr.service.PaymentService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {

        //数据结构不完整
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());

        orderInfo.setOrderStatus(OrderStatus.UNPAID);

        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfo.sumTotalAmount();

        orderInfoMapper.insertSelective(orderInfo);

        //订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.set(tradeNoKey,tradeCode);

        jedis.close();

        return tradeCode;

    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis=redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        String tradeNo = jedis.get(tradeNoKey);

        jedis.close();

        return tradeCodeNo.equals(tradeNo);
    }

    @Override
    public void delTradeCode(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey =  "user:"+userId+":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);

        if ("1".equals(result)){

            return  true;
        }else {

            return  false;
        }

    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail=new OrderDetail();

        orderDetail.setOrderId(orderId);

        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);

        orderInfo.setOrderStatus(processStatus.getOrderStatus());

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {

        Connection connection = activeMQUtil.getConnection();
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(orderJson);

            producer.send(textMessage);

            session.commit();
            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {

        Example example = new Example(OrderInfo.class);

        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);

        return orderInfoMapper.selectByExample(example);

    }

    @Override
    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 订单信息
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);

        // 付款信息
        paymentService.closePayment(orderInfo.getId());

    }

    /**
     * 根据orderId将orderInfo变为json字符串
     * @param orderId
     * @return
     */
    public String initWareOrder(String orderId) {

        OrderInfo orderInfo = getOrderInfo(orderId);
        
        Map map = initWareOrder(orderInfo);
        
        return JSON.toJSONString(map);

    }

    public Map initWareOrder(OrderInfo orderInfo) {

        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
       // map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;

    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {

        /**
         * 1.获取原始订单
         * 2.将wareSkuMap转换成我们能操作的对象
         * 3.创建新的订单
         * 4.给子订单赋值
         * 5.将子订单添加到集合中
         * 6.更新订单状态
         */

        List<OrderInfo> subOrderInfoList = new ArrayList<>();

        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);

        if(maps!=null){

            for (Map map : maps) {

                String wareId = (String) map.get("wareId");

                List<String> skuIds = (List<String>) map.get("skuIds");

                OrderInfo subOrderInfo = new OrderInfo();
                //属性拷贝
                BeanUtils.copyProperties(subOrderInfo,orderInfoOrigin);

                subOrderInfo.setId(null);
                // 5 原来主订单，订单主表中的订单状态标志为拆单
                subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
                subOrderInfo.setWareId(wareId);

                // 6 明细表 根据拆单方案中的skuids进行匹配，得到那个的子订单
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
                // 创建一个新的订单集合
                List<OrderDetail> subOrderDetailList = new ArrayList<>();

                for (OrderDetail orderDetail : orderDetailList) {
                    for (String skuId : skuIds) {
                        if (skuId.equals(orderDetail.getSkuId())){
                            orderDetail.setId(null);
                            subOrderDetailList.add(orderDetail);
                        }
                    }
                }

                subOrderInfo.setOrderDetailList(subOrderDetailList);
                subOrderInfo.sumTotalAmount();
                // 7 保存到数据库中
                saveOrder(subOrderInfo);
                subOrderInfoList.add(subOrderInfo);

            }
        }

        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        // 8 返回一个新生成的子订单列表
        return subOrderInfoList;
    }
}

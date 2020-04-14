package com.yhr.service;

import com.yhr.bean.OrderInfo;
import com.yhr.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void  delTradeCode(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 根据orderId查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 发送消息给库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 查询过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理过期订单（关闭订单）
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    /**
     * 将OrderInfo转换为map
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}

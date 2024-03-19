package com.drake.shortlink.project.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.drake.shortlink.project.common.convention.exception.ServiceException;
import com.drake.shortlink.project.dao.entity.LinkStatsMessage;
import com.drake.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.drake.shortlink.project.service.ShortLinkService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ消费者
 */
@Slf4j
@RocketMQMessageListener(topic = "short-link-stats", consumerGroup = "short-link-stats-consumer-group")
@Component
public class ShortLinkStatsListener implements RocketMQListener<MessageExt> {

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private MessageQueueIdempotentHandler handler;

    /**
     * 消费者读取消息
     *
     * @param messageExt
     */
    @SneakyThrows
    @Override
    public void onMessage(MessageExt messageExt) {
        String jsonStr = new String(messageExt.getBody());
        LinkStatsMessage linkStatsMessage = JSONUtil.toBean(jsonStr, LinkStatsMessage.class);
        String msgId = messageExt.getMsgId();
        try {
            // 消息幂等处理，防止消息重复消费
            boolean isMessageProcessed = handler.isMessageProcessed(msgId);
            // 如果拿到了锁，说明当前没有其他线程在处理同样的业务，则可执行业务
            if(isMessageProcessed){
                shortLinkService.saveShortLinkStats(linkStatsMessage);
                // 处理完业务需将锁的状态标识为已完成
                handler.setAccomplish(msgId);
            }
            else{
                // 若未拿到锁，需先判断锁的状态
                boolean isAccomplish = handler.isAccomplish(msgId);
                // 若锁的状态为消费中，说明之前消费的线程出现异常导致消息没有被成功消费，这时需要通过抛出异常，将锁释放，并重试消费消息
                if(!isAccomplish){
                    throw new ServiceException("消息未成功消费！");
                }
            }
        } catch (Throwable e) {
            log.error("Consume Message Error!", e);
            // 若捕获到异常，则删除锁并重试消费消息
            handler.delMessageProcessed(msgId);
        }
    }
}

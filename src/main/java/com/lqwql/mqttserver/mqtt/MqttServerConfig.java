package com.lqwql.mqttserver.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqwql.mqttserver.Constants;
import com.lqwql.mqttserver.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;


/**
 * mqtt服务类
 * 一种基于发布/订阅（publish/subscribe）模式的轻量级通讯协议，通过订阅相应的主题来获取消息，
 * 是物联网（Internet of Thing）中的一个标准传输协议
 * ClientId是MQTT客户端的标识。MQTT服务端用该标识来识别客户端。因此ClientId必须是独立的。
 * clientID需为全局唯一。如果不同的设备使用相同的clientID同时连接物联网平台，那么先连接的那个设备会被强制断开。
 */
@Configuration
@IntegrationComponentScan
@Slf4j
public class MqttServerConfig {

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.hostUrl}")
    private String hostUrl;

    @Value("${mqtt.pubClientId}")
    private String pubClientId;

    @Value("${mqtt.subClientId}")
    private String subClientId;

    @Value("${mqtt.pubTopic}")
    private String pubTopic;

    @Value("${mqtt.subTopic}")
    private String subTopic;

    @Value("${mqtt.completionTimeout}")
    private int completionTimeout;

    @Autowired
    private DeviceService ds;
    /*========================================factory=================================*/
    /**
     * mqtt客户工厂
     * @return
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
        // 把配置里的 cleanSession 设为false，客户端掉线后 服务器端不会清除session，
        // 当重连后可以接收之前订阅主题的消息。当客户端上线后会接受到它离线的这段时间的消息
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setServerURIs(hostUrl.split(","));
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        mqttConnectOptions.setKeepAliveInterval(20);
        mqttConnectOptions.setMaxInflight(1000);
        factory.setConnectionOptions(mqttConnectOptions);
        return factory;
    }

    /*========================================sent=================================*/
    /**
     * mqtt出站通道
     * @return
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * mqtt出站handler
     *
     * @return {@link MessageHandler}
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler() {
        //MqttPahoMessageHandler初始化
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(pubClientId+"_send_handler_", mqttClientFactory());
        //设置默认的qos级别
        handler.setDefaultQos(1);
        //保留标志的默认值。如果没有mqtt_retained找到标题，则使用它。如果提供了自定义，则不使用它converter。这里不启用
        handler.setDefaultRetained(false);
        //设置发布的主题
        handler.setDefaultTopic(pubTopic);
        //当 时true，调用者不会阻塞。相反，它在发送消息时等待传递确认。默认值为false（在确认交付之前发送阻止）。
        handler.setAsync(false);
        //当 async 和 async-events 都为 true 时，会发出 MqttMessageSentEvent（请参阅事件）。它包含消息、主题、客户端库生成的messageId、clientId和clientInstance（每次连接客户端时递增）。当客户端库确认交付时，会发出 MqttMessageDeliveredEvent。它包含 messageId、clientId 和 clientInstance，使传递与发送相关联。任何 ApplicationListener 或事件入站通道适配器都可以接收这些事件。请注意，有可能在 MqttMessageSentEvent 之前接收到 MqttMessageDeliveredEvent。默认值为false。
        handler.setAsyncEvents(false);
        return handler;
    }


    /*========================================receive=================================*/

    /**
     * mqtt输入通道
     * @return
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 入站
     * @return
     */
    @Bean
    public MessageProducer inbound() {
        //配置订阅端MqttPahoMessageDrivenChannelAdapter
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(subClientId+"_receive_inbound_", mqttClientFactory(), subTopic.split(","));
        //设置超时时间
        adapter.setCompletionTimeout(completionTimeout);
        //设置默认的消息转换类
        adapter.setConverter(new DefaultPahoMessageConverter());
        //设置qos级别
        adapter.setQos(1);
        //设置入站管道
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setTaskScheduler(new ConcurrentTaskScheduler());
        return adapter;
    }

    /**
     * 消息处理程序
     * @return
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                MessageHeaders headers = message.getHeaders();
                log.info("headers: {}", headers);
                String topic = headers.get(MqttHeaders.RECEIVED_TOPIC).toString();
//                 订阅空气数据
                if (topic.equals(Constants.TOPIC_AIR_DATA_RECEIVE)) {
//                    log.info("订阅主题为:{};接收到该主题消息为:{}",topic,message.getPayload().toString());
                    try {
                        Map<String, Object> data = new ObjectMapper().readValue(message.getPayload().toString(), new TypeReference<Map<String, Object>>() {});
                        if (data.get("valid").equals(true)) {
                            ds.addAirData(UUID.randomUUID().toString(),
                                    (Double) data.get("air_temp"),
                                    (Double) data.get("air_humi"),
                                    (Double) data.get("air_pres"),
                                    (String) data.get("client_id"),
                                    new Timestamp(new java.util.Date().getTime()));
                        }

                    } catch (JsonProcessingException jpe) {
                        throw new RuntimeException(jpe);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(topic.equals(Constants.TOPIC_DEVICE_LOGIN)) {
                    try {
                        Map<String, Object> data = new ObjectMapper().readValue(message.getPayload().toString(), new TypeReference<Map<String, Object>>() {});
                        String cidM = (String) data.get("client_id");
                        String uidM = (String) data.get("user_id");
                        String uidF = "";
                        for (int i = 0; i < uidM.length(); i ++){
                            Character t = uidM.charAt(i);;
                            if (Character.isDigit(t) || Character.isLetter(t) || t.equals('-')) {
                                uidF += t;
                            }
                        }
                        ds.addDevice(cidM, uidF, null);
                    } catch (JsonProcessingException jpe) {
                        throw new RuntimeException(jpe);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                log.info("订阅主题为: {}", topic);
//                String[] topics = subTopic.split(",");
//                for (String t : topics) {
//                    if (t.equals(topic)) {
//                        log.info("订阅主题为:{};接收到该主题消息为:{}",topic,message.getPayload().toString());
//                    }
//                }
            }
        };
    }
}

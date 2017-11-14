package com.huashi.sms.config.rabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.huashi.common.util.IdGenerator;
import com.huashi.sms.config.FastJsonMessageConverter;
import com.huashi.sms.task.context.MQConstant;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * 
 * TODO 短信消息队列配置信息
 * template必须为原型模式：如果需要在生产者需要消息发送后的回调，需要对rabbitTemplate设置ConfirmCallback对象，
 * 由于不同的生产者需要对应不同的ConfirmCallback，如果rabbitTemplate设置为单例bean，则所有的rabbitTemplate
 * 
 *
 * @author zhengying
 * @version V1.0.0
 * @date 2016年10月3日 下午11:56:40
 */
@Configuration
@EnableRabbit
public class RabbitMqConfiguration {

	@Value("${mq.rabbit.host}")
	private String mqHost;
	@Value("${mq.rabbit.port}")
	private Integer mqPort;
	@Value("${mq.rabbit.username}")
	private String mqUsername;
	@Value("${mq.rabbit.password}")
	private String mqPassword;
	@Value("${mq.rabbit.vhost}")
	private String mqVhost;
	
	@Value("${mq.rabbit.consumers}")
	private int concurrentConsumers;
	@Value("${mq.rabbit.maxconsumers}")
	private int maxConcurrentConsumers;
	
	@Value("${mq.rabbit.prefetch:1}")
	private int rabbitPrefetchCount;

	@Bean
	public IdGenerator idGenerator() {
		return new IdGenerator(1);
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public FastJsonMessageConverter fastjsonMessageConverter() {
		return new FastJsonMessageConverter();
	}

	@Bean(name = "rabbitConnectionFactory")
	public ConnectionFactory rabbitConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(mqHost);
		connectionFactory.setPort(mqPort);
		connectionFactory.setUsername(mqUsername);
		connectionFactory.setPassword(mqPassword);
		connectionFactory.setVirtualHost(mqVhost);
		
		// 显性设置后才能进行回调函数设置
		connectionFactory.setPublisherReturns(true); // enable confirm mode
		connectionFactory.setPublisherConfirms(true);
		
		// 默认 connectionFactory.setCacheMode(CacheMode.CHANNEL), ConnectionCacheSize无法设置
		connectionFactory.setChannelCacheSize(100);
//		connectionFactory.setConnectionCacheSize(5);

		// 关键所在，指定线程池
//		ExecutorService service = Executors.newFixedThreadPool(500);
//		connectionFactory.setExecutor(service);

		connectionFactory.setRequestedHeartBeat(60);
		connectionFactory.setConnectionTimeout(15000);// 15秒

		// 断开重连接，保证数据无丢失
//		connectionFactory.setAutomaticRecoveryEnabled(true);
		
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory rabbitConnectionFactory) {
		AmqpAdmin amqpAdmin = new RabbitAdmin(rabbitConnectionFactory);
		DirectExchange exchange = new DirectExchange(MQConstant.EXCHANGE_SMS, true, false);
		amqpAdmin.declareExchange(exchange);
		
		// 待分包处理队列
		Queue smsWaitProcessQueue = new Queue(MQConstant.MQ_SMS_MT_WAIT_PROCESS, true, false, false, setQueueFeatures());
		Binding smsWaitProcessBinding = BindingBuilder.bind(smsWaitProcessQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MT_WAIT_PROCESS);
		amqpAdmin.declareQueue(smsWaitProcessQueue);
		amqpAdmin.declareBinding(smsWaitProcessBinding);
		
		// 待点对点短信分包处理队列
		Queue smsP2PWaitProcessQueue = new Queue(MQConstant.MQ_SMS_MT_P2P_WAIT_PROCESS, true, false, false, setQueueFeatures());
		Binding smsP2PWaitProcessBinding = BindingBuilder.bind(smsP2PWaitProcessQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MT_P2P_WAIT_PROCESS);
		amqpAdmin.declareQueue(smsP2PWaitProcessQueue);
		amqpAdmin.declareBinding(smsP2PWaitProcessBinding);
		
//		// 分包处理完成，待提交网关（上家通道）队列
//		Queue smsWaitSubmitQueue = new Queue(MQConstant.MQ_SMS_MT_WAIT_SUBMIT, true, false, false, setQueueFeatures());
//		Binding smsWaitSubmitBinding = BindingBuilder.bind(smsWaitSubmitQueue).to(exchange)
//				.with(MQConstant.MQ_SMS_MT_WAIT_SUBMIT);
//		amqpAdmin.declareQueue(smsWaitSubmitQueue);
//		amqpAdmin.declareBinding(smsWaitSubmitBinding);

		// 网关回执数据，带解析回执数据并推送
		Queue smsWaitReceiptQueue = new Queue(MQConstant.MQ_SMS_MT_WAIT_RECEIPT, true, false, false, setQueueFeatures());
		Binding smsWaitReceiptBinding = BindingBuilder.bind(smsWaitReceiptQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MT_WAIT_RECEIPT);
		amqpAdmin.declareQueue(smsWaitReceiptQueue);
		amqpAdmin.declareBinding(smsWaitReceiptBinding);

		// 用户上行回复记录
		Queue moReceiveQueue = new Queue(MQConstant.MQ_SMS_MO_RECEIVE, true, false, false, setQueueFeatures());
		Binding moReceiveBinding = BindingBuilder.bind(moReceiveQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MO_RECEIVE);
		amqpAdmin.declareQueue(moReceiveQueue);
		amqpAdmin.declareBinding(moReceiveBinding);
		
		// 用户推送数据，带解析推送数据并推送(上行)
		Queue moWaitPushQueue = new Queue(MQConstant.MQ_SMS_MO_WAIT_PUSH, true, false, false, setQueueFeatures());
		Binding moWaitPushBinding = BindingBuilder.bind(moWaitPushQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MO_WAIT_PUSH);
		amqpAdmin.declareQueue(moWaitPushQueue);
		amqpAdmin.declareBinding(moWaitPushBinding);
		
		// 下行分包异常，如黑名单数据
		Queue packetsExceptionQueue = new Queue(MQConstant.MQ_SMS_MT_PACKETS_EXCEPTION, true, false, false, setQueueFeatures());
		Binding packetsExceptionBinding = BindingBuilder.bind(packetsExceptionQueue).to(exchange)
				.with(MQConstant.MQ_SMS_MT_PACKETS_EXCEPTION);
		amqpAdmin.declareQueue(packetsExceptionQueue);
		amqpAdmin.declareBinding(packetsExceptionBinding);

		return amqpAdmin;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	RabbitTemplate rabbitTemplate(@Qualifier("rabbitConnectionFactory") ConnectionFactory rabbitConnectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
		// rabbitTemplate.setQueue(SmsQueueConstant.MQ_SMS_MT_WAIT_PROCESS);
		// rabbitTemplate.setRoutingKey(SmsQueueConstant.MQ_SMS_MT_WAIT_PROCESS);

		rabbitTemplate.setMessageConverter(messageConverter());
		rabbitTemplate.setRetryTemplate(retryTemplate());
		rabbitTemplate.setMandatory(true);

		return rabbitTemplate;
	}

	/**
	 * 
	 * TODO 设置重试模板
	 * 
	 * @param rabbitTemplate
	 */
	@Bean
	public RetryTemplate retryTemplate() {
		// 重试模板
		RetryTemplate retryTemplate = new RetryTemplate();
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(500);
		backOffPolicy.setMultiplier(10.0);
		backOffPolicy.setMaxInterval(10000);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		// 尝试最大重试次数
		retryPolicy.setMaxAttempts(3);
		retryTemplate.setRetryPolicy(retryPolicy);
		
		return retryTemplate;
	}
	
	/**
	 * 
	   * TODO 获取队列中消息总量
	   * 
	   * @param queue
	   * @param rabbitConnectionFactory
	   * @return
	   * @throws IOException
	 */
	public int getMessageCount(final String queue, @Qualifier("rabbitConnectionFactory") ConnectionFactory rabbitConnectionFactory) 
			throws IOException {
        Connection connection = rabbitConnectionFactory.createConnection();
        final Channel channel = connection.createChannel(false);
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);

        return declareOk.getMessageCount();
    }

	// @Bean
	// public StatefulRetryOperationsInterceptor interceptor() {
	// return RetryInterceptorBuilder.stateful().maxAttempts(5)
	// .backOffOptions(1000, 2.0, 10000) // initialInterval, multiplier,
	// maxInterval
	// .build();
	// }

	/**
	 * 
	   * TODO 设置队列属性
	   * @return
	 */
	private Map<String, Object> setQueueFeatures() {
		Map<String, Object> args  = new HashMap<>();
		// 最大优先级定义
		args .put("x-max-priority", 10);
		// 过期时间，单位毫秒
//		args .put("x-message-ttl", 60000);
		// 30分钟，单位毫秒
//		args.put("x-expires", 1800000); 
		// 集群高可用，数据复制
		args.put("x-ha-policy", "all");
		return args;
	}

	// @Bean
	// MessageListenerAdapter listenerAdapter(SmsQueueService smsQueueService) {
	// return new MessageListenerAdapter(smsQueueService, "receiveMessage");

	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(@Qualifier("rabbitConnectionFactory") ConnectionFactory rabbitConnectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(rabbitConnectionFactory);

		factory.setConcurrentConsumers(concurrentConsumers);
		factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
		// 消息失败重试后 设置 RabbitMQ把消息分发给有空的cumsuer，同 channel.basicQos(1);
		factory.setPrefetchCount(rabbitPrefetchCount);
//		factory.setConsumerTagStrategy(consumerTagStrategy);
		
		
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(messageConverter());
		
//		factory.setMissingQueuesFatal(missingQueuesFatal);
		
//		ExecutorService service = Executors.newFixedThreadPool(500);
//		factory.setTaskExecutor(service);
		
		
		return factory;
	}

//	@Bean
//	public SimpleMessageListenerContainer simpleMessageListenerContainer(
//			@Qualifier("rabbitConnectionFactory") ConnectionFactory rabbitConnectionFactory) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitConnectionFactory);
//		
//		container.setConcurrentConsumers(concurrentConsumers);
//		container.setMaxConcurrentConsumers(maxConcurrentConsumers);
//		
//		// 设置公平分发，同 channel.basicQos(1);
//		container.setPrefetchCount(rabbitPrefetchCount);
//		container.setMessageConverter(messageConverter());
//		
//		container.setQueueNames(MQConstant.MQ_SMS_MT_WAIT_PROCESS, MQConstant.MQ_SMS_MT_WAIT_RECEIPT, MQConstant.MQ_SMS_MO_RECEIVE, 
//				MQConstant.MQ_SMS_MO_WAIT_PUSH, MQConstant.MQ_SMS_MT_PACKETS_EXCEPTION);
//
//		
//		// 设置是否自动启动
//		container.setAutoStartup(true);
//		// 设置拦截器信息
//		// container.setAdviceChain(adviceChain);
//
//		// 设置事务
//		// container.setChannelTransacted(true);
//
//		// 设置优先级
////		container.setConsumerArguments(Collections.<String, Object> singletonMap("x-priority", Integer.valueOf(10)));
//		container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置确认模式手工确认
//		
//		return container;
//	}
}

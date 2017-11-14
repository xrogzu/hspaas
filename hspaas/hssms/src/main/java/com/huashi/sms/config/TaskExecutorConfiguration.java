package com.huashi.sms.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 
  * TODO 线程池配置
  * 
  * @author zhengying
  * @version V1.0   
  * @date 2017年9月20日 下午5:58:30
 */
@Configuration
public class TaskExecutorConfiguration {


    /**
     * 配置线程池
     * 
     * @return
     */
    @Bean(initMethod = "initialize", destroyMethod = "shutdown")
    ThreadPoolTaskExecutor poolTaskExecutor() {

        ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();

        // 线程池所使用的缓冲队列
        poolTaskExecutor.setQueueCapacity(200); // 队列大一点，以应对峰值压力
        // 线程池维护线程的最少数量
        poolTaskExecutor.setCorePoolSize(10); // 处理签名合成，并处理邮件和短信发送,pdf图片转换
        // 线程池维护线程的最大数量
        poolTaskExecutor.setMaxPoolSize(200); // 该值不能太大，tomcat 默认最大线程为200.
        // 线程池维护线程所允许的空闲时间
        poolTaskExecutor.setKeepAliveSeconds(10000); // 10分

        return poolTaskExecutor;
    }
    
    
    public static void main(String[] args) {

        ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();

        // 线程池所使用的缓冲队列
        poolTaskExecutor.setQueueCapacity(200); // 队列大一点，以应对峰值压力
        // 线程池维护线程的最少数量
        poolTaskExecutor.setCorePoolSize(10); // 处理签名合成，并处理邮件和短信发送,pdf图片转换
        // 线程池维护线程的最大数量
        poolTaskExecutor.setMaxPoolSize(200); // 该值不能太大，tomcat 默认最大线程为200.
        // 线程池维护线程所允许的空闲时间
        poolTaskExecutor.setKeepAliveSeconds(10000);
        
        poolTaskExecutor.initialize();
        
        List<String> data = new ArrayList<String>();
        for(int i= 0;i<10;i++) {
        	data.add(i + "");
        }
        
        System.out.println("开始");
        Future<Integer> future = poolTaskExecutor.submit(() -> getCount(data));
        
        
        try {
			System.out.println("总数量为：" + future.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        
        data.forEach(n -> {System.out.println(n);});
	}
    
    public static int getCount(List<String> data) {
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return data.size() + 5;
    }

}

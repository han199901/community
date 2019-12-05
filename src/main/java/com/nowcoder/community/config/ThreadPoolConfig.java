package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author qhhu
 * @date 2019/12/5 - 23:47
 */
// 需要配置这个类, spring定时线程池默认不启动
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {



}

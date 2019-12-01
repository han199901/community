package com.nowcoder.community.config;

import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author qhhu
 * @date 2019/12/1 - 21:57
 */
// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程
    // 1. 通过FactoryBean封装Bean的实例化过程
    // 2. 将FactoryBean装配到Spring容器里
    // 3. 将FactoryBean注入其他的Bean
    // 4. 该Bean定得到的是FactoryBean所管理的对象实例

    // JobDetail
    // 刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(PostScoreRefreshJob.class);
        jobDetailFactoryBean.setName("postScoreRefreshJob");
        jobDetailFactoryBean.setGroup("communityJobGroup");
        jobDetailFactoryBean.setDurability(true); // 声明任务是长久保存的
        jobDetailFactoryBean.setRequestsRecovery(true); // 任务是否是可恢复的
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        simpleTriggerFactoryBean.setName("postScoreRefreshTrigger");
        simpleTriggerFactoryBean.setGroup("communityTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(1000 * 60 * 5); // 多长时间执行一次任务
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap()); // 指定trigger底层存储job状态的数据结构
        return simpleTriggerFactoryBean;
    }
}

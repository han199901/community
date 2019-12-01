package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qhhu
 * @date 2019/12/1 - 21:31
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    // 我的纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-10-16 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化我的纪元失败:" + e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数, 共需刷新帖子数为: " + operations.size());
        while (operations.size() > 0) {
            this.refresh((int) operations.pop());
        }
        logger.info("[任务完成] 帖子分数刷新完毕");
    }

    private void refresh(int discussPostId) {
        DiscussPost discussPost = discussPostService.getDiscussPostById(discussPostId);

        if (discussPost == null) {
            logger.error("该帖子不存在: id = " + discussPostId);
            return;
        }

        // 是否精华
        boolean good = discussPost.getStatus() == 1;
        // 评论数量
        int commentCount = discussPost.getCommentCount();
        // 点赞数量
        long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, discussPostId);

        // 计算权重
        double w = (good ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1)) + (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(discussPostId, score);
        // 同步更新搜素数据
        discussPost.setScore(score);
        elasticSearchService.saveDiscussPost(discussPost);
    }
}

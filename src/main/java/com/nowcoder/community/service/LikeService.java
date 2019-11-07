package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author qhhu
 * @date 2019/11/7 - 20:09
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isLike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        // 重复点赞 == 取消点赞
        if (isLike) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    // 查询某实体点赞的数量
    public long getEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int getEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 返回整数, 1: 代表已赞, 0: 代表未赞(使用boolean只能表示两种状态, 整数可以用-1代表点踩)
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

}

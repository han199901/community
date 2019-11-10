package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author qhhu
 * @date 2019/11/8 - 22:35
 */
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                // 当前用户对该类实体的关注列表中加入该实体
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // 该实体的粉丝列表中加入当前用户
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                // 当前用户对该类实体的关注列表中移除该实体
                redisOperations.opsForZSet().remove(followeeKey, entityId, System.currentTimeMillis());
                // 该实体的粉丝列表中移除当前用户
                redisOperations.opsForZSet().remove(followerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    // 查询用户关注某类实体的数量
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询某实体的粉丝数量
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().score(followerKey, userId) != null;
    }

    // 查询某个用户的关注的人
    public List<Map<String, Object>> getFolloweeUsers(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 调用redis的range和revrange方法返回的集合是有序的, [offset, offset + limit - 1]
        Set<Integer> followeeUserIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (followeeUserIds == null) {
            return null;
        }

        List<Map<String, Object>> followeeUserVoList = new ArrayList<>();
        for (Integer followeeUserId : followeeUserIds) {
            Map<String, Object> followeeUserVo = new HashMap<>();
            User followeeUser = userService.getUserById(followeeUserId);
            followeeUserVo.put("followeeUser", followeeUser);
            Double score = redisTemplate.opsForZSet().score(followeeKey, followeeUserId);
            followeeUserVo.put("followTime", new Date(score.longValue()));
            // 判断登录用户是否关注该用户
            boolean hasFollowed = false;
            if (hostHolder.getUser() != null) {
                hasFollowed = hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, followeeUserId);
            }
            followeeUserVo.put("hasFollowed", hasFollowed);
            followeeUserVoList.add(followeeUserVo);
        }

        return followeeUserVoList;
    }

    // 查询某个用户的粉丝
    public List<Map<String, Object>> getFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // 调用redis的range和revrange方法返回的集合是有序的, [offset, offset + limit - 1]
        Set<Integer> followersIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (followersIds == null) {
            return null;
        }

        List<Map<String, Object>> followerVoList = new ArrayList<>();
        for (Integer followerId : followersIds) {
            Map<String, Object> followerVo = new HashMap<>();
            User follower = userService.getUserById(followerId);
            followerVo.put("follower", follower);
            Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
            followerVo.put("followTime", new Date(score.longValue()));
            // 判断登录用户是否关注该用户
            boolean hasFollowed = false;
            if (hostHolder.getUser() != null) {
                hasFollowed = hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, followerId);
            }
            followerVo.put("hasFollowed", hasFollowed);
            followerVoList.add(followerVo);
        }

        return followerVoList;
    }

}

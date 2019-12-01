package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author qhhu
 * @date 2019/10/19 - 22:39
 */
@Mapper
@Repository
public interface DiscussPostMapper {

    // 增加userId参数，使用动态SQL语句
    // userId == 0时，查找所有用户的帖子，用于首页显示
    // userId != 0时，查找该用户的帖子，用于用户主页显示
    // 排序模式同上
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // @Param注解用于给参数取别名
    // 如果只有一个参数并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int discussPostId, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}

package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author qhhu
 * @date 2019/11/3 - 14:36
 */
@Mapper
@Repository
public interface MessageMapper {

    // 查询当前用户的会话列表, 针对每个会话只返回一个最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询当前会话所包含的所有私信
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询当前会话所包含的所有私信数量
    int selectLetterCount(String conversationId);

    // 查询未读的私信数量
    // 动态sql, 不加conversationId则查询当前用户的所有未读的私信数量;
    //          加conversationId则查询当前会话中, 当前用户所有未读的私信数量
    int selectLetterUnreadCount(int userId, String conversationId);

}

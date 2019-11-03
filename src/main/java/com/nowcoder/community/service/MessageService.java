package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qhhu
 * @date 2019/11/3 - 15:09
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public List<Message> getConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int getConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> getLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int getLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int getLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

}

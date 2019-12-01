package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.util.CommunityConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeaderUrl(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for (DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertDiscussPost() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(11111);
        discussPost.setTitle("huqihh");
        discussPost.setContent("nihk");
        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Test
    public void testSelectDiscussPostById() {
        System.out.println(discussPostMapper.selectDiscussPostById(285));
    }

    @Test
    public void testUpdateCommentCount() {
        discussPostMapper.updateCommentCount(109, 200);
        System.out.println(discussPostMapper.selectDiscussPostById(109));
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }

    @Test
    public void testUpdateLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        loginTicketMapper.updateStatus(loginTicket.getTicket(), 1);

        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }

    @Test
    public void testSelectCommentsByEntity() {
        List<Comment> list = commentMapper.selectCommentsByEntity(1, 280, 0, Integer.MAX_VALUE);
        for (Comment comment : list) {
            System.out.println(comment);
        }
    }

    @Test
    public void testSelectCountByEntity() {
        System.out.println(commentMapper.selectCountByEntity(1, 280));
    }

    @Test
    public void testInsertComment() {
        Comment comment = new Comment();
        comment.setUserId(1);
        comment.setEntityType(1);
        comment.setEntityId(1);
        comment.setTargetId(1);
        comment.setContent("nihk");
        comment.setStatus(1);
        comment.setCreateTime(new Date());
        commentMapper.insertComment(comment);
        System.out.println(comment);
    }

    @Test
    public void testMessageMapper() {
        List<Message> list =  messageMapper.selectConversations(111, 0, 5);
        for (Message message : list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        list = messageMapper.selectLetters("111_138", 0, 5);
        for (Message message : list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectLetterCount("111_138"));

        System.out.println(messageMapper.selectLetterUnreadCount(111, null));
    }

    @Test
    public void testInsertMessage() {
        Message message = new Message();
        message.setContent("你好啊, 古久寒");
        message.setFromId(1);
        message.setToId(2);
        message.setConversationId("1_2");
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageMapper.insertMessage(message);
    }

    @Test
    public void testUpdateMessageStatus() {
        messageMapper.updateStatus(Arrays.asList(355), 2);
    }

    @Test
    public void testSelectLatestNotice() {
        Message notice = messageMapper.selectLatestNotice(111, TOPIC_FOLLOW);
        System.out.println(notice);
    }

}

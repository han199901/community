package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author qhhu
 * @date 2019/12/6 - 20:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setUserId(111);
            discussPost.setTitle("test缓存数据");
            discussPost.setContent("看看缓存在压力测试的用处");
            discussPost.setCreateTime(new Date());
            discussPost.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(discussPost);
        }
    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 0));
    }
}

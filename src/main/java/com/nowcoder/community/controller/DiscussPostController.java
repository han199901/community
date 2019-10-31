package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author qhhu
 * @date 2019/10/29 - 22:56
 */
@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    // 异步操作: 异步请求, 在网页不刷新的情况下, 访问服务器, 服务器返回非网页结果
    // 通过对结果中的数据的提炼, 来局部刷新网页, 通常是给一个提示或更改一个样式等
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            // 403代表没有权限
            return CommunityUtil.getJSONString(403, "你还没有登录");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 报错情况, 将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String discussPostDetail(@PathVariable("discussPostId") int discussPostId, Model model) {
        DiscussPost discussPost = discussPostService.getDiscussPostById(discussPostId);
        model.addAttribute("discussPost", discussPost);
        User user = userService.getUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        return "/site/discuss-detail";
    }

}

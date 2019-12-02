package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author qhhu
 * @date 2019/10/27 - 13:45
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    // Spring MVC：通过 MultipartFile 处理上传文件
    // MultipartFile是Spring MVC的对象, 在表现层处理文件存储, 防止表现层与业务层耦合
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")); // 文件后缀名
        if (suffix.equals(".txt")) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名, 防止文件互相覆盖
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件的存放位置
        File dest = new File(uploadPath + "/" + fileName); // 在目标位置创建一个空文件
        try {
            headerImage.transferTo(dest); // io操作需要捕获异常
        } catch (IOException e) {
            logger.error("文件上传失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败, 服务器发成异常", e);
        }

        // 跟新当前用户的头像的路径(web访问路径)
        // http://127.0.0.1:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // 返回值void, 向浏览器响应的既不是网页也不是字符串, 而是图片(二进制的数据), 需要通过流, 手动向浏览器输出
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            // 建立缓冲区, 一批一批输出, 提高效率
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/modifyPassword", method = RequestMethod.POST)
    public String modifyPassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(hostHolder.getUser().getId(), oldPassword, newPassword);
        if (map.containsKey("passwordMsg")) {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/setting";
        }

        return "redirect:/index";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 主页所属用户
        model.addAttribute("user", user);
        // 及其所获得的赞的数量
        long likeCount = likeService.getUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 及其关注用户的数量
        long followeeCount = followService.getFolloweeCount(user.getId(), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 及其粉丝的数量
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount", followerCount);
        // 当前登录用户是否关注主页所属用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}

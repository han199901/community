package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author qhhu
 * @date 2019/11/3 - 15:14
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterListPage(Model model, Page page) {
        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.getConversationCount(user.getId()));

        // 会话列表
        // 每条会话需要信息: 会话中最近一条私信内容, 当前会话未读私信数量, 当前会话私信总数量, 当前会话中的另一个用户
        List<Message> conversationList = messageService.getConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationList != null) {
            for (Message conversation : conversationList) {
                Map<String, Object> conversationVo = new HashMap<>();
                conversationVo.put("conversation", conversation);
                conversationVo.put("unreadCount", messageService.getLetterUnreadCount(user.getId(), conversation.getConversationId()));
                conversationVo.put("letterCount", messageService.getLetterCount(conversation.getConversationId()));
                int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                conversationVo.put("target", userService.getUserById(targetId));

                conversationVoList.add(conversationVo);
            }
        }
        model.addAttribute("conversationVoList", conversationVoList);

        // 查询所有未读的私信数量
        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    // 私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetailPage(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.getLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.getLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> letterVo = new HashMap<>();
                letterVo.put("letter", letter);
                letterVo.put("fromUser", userService.getUserById(letter.getFromId()));
                letterVoList.add(letterVo);
            }
        }
        model.addAttribute("letterVoList", letterVoList);

        // 私信的目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置私信已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id1 = Integer.parseInt(ids[0]);
        int id2 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id1) {
            return userService.getUserById(id2);
        } else {
            return userService.getUserById(id1);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message letter : letterList) {
                // 私信的发送者默认已读
                // 私信的接受者未读时, 将当前私信id加入ids列表中(status字段的0和1是标识私信接受者的读取情况)
                if (hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.getUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        String conversationId = makeConversationId(message.getFromId(), message.getToId());
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    private String makeConversationId(int  id1, int id2) {
        if (id1 <= id2) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeListPage(Model model) {
        User user = hostHolder.getUser();


        // 没有过被评论点赞关注, 会出错. 模板解析时无数据, 报错???
        // 查询评论类最新通知
        Message notice = messageService.getLatestNotice(user.getId(), TOPIC_COMMENT);
        if (notice != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("notice", notice); // 主要使用出content之外的其他信息(通知的对象, 时间)

            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            noticeVo.put("user", userService.getUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("discussPostId", data.get("discussPostId"));

            int count = messageService.getNoticeCount(user.getId(), TOPIC_COMMENT);
            noticeVo.put("count", count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            noticeVo.put("unreadCount", unreadCount);
            model.addAttribute("commentNotice", noticeVo);
        }

        // 查询点赞类最新通知
        notice = messageService.getLatestNotice(user.getId(), TOPIC_LIKE);
        if (notice != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("notice", notice); // 主要使用出content之外的其他信息(通知的对象, 时间)

            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            noticeVo.put("user", userService.getUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));
            noticeVo.put("discussPostId", data.get("discussPostId"));

            int count = messageService.getNoticeCount(user.getId(), TOPIC_LIKE);
            noticeVo.put("count", count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            noticeVo.put("unreadCount", unreadCount);
            model.addAttribute("likeNotice", noticeVo);
        }

        // 查询关注类最新通知
        notice = messageService.getLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (notice != null) {
            Map<String, Object> noticeVo = new HashMap<>();
            noticeVo.put("notice", notice); // 主要使用出content之外的其他信息(通知的对象, 时间)

            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            noticeVo.put("user", userService.getUserById((Integer) data.get("userId")));
            noticeVo.put("entityType", data.get("entityType"));
            noticeVo.put("entityId", data.get("entityId"));

            int count = messageService.getNoticeCount(user.getId(), TOPIC_FOLLOW);
            noticeVo.put("count", count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            noticeVo.put("unreadCount", unreadCount);
            model.addAttribute("followNotice", noticeVo);
        }

        // 查询未读消息数量
        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetailPage(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.getNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.getNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> noticeVo = new HashMap<>();
                // 通知
                noticeVo.put("notice", notice);
                // 内容(discussPostId关注用不到, 放着不用即可)
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                noticeVo.put("user", userService.getUserById((Integer) data.get("userId")));
                noticeVo.put("entityType", data.get("entityType"));
                noticeVo.put("entityId", data.get("entityId"));
                noticeVo.put("discussPostId", data.get("discussPostId"));
                // 通知作者(系统用户)
                noticeVo.put("fromUser", userService.getUserById(notice.getFromId()));

                noticeVoList.add(noticeVo);
            }
        }
        model.addAttribute("noticeVoList", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

}

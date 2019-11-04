package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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

import java.util.*;

/**
 * @author qhhu
 * @date 2019/11/3 - 15:14
 */
@Controller
@RequestMapping("/letter")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
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

        return "/site/letter";
    }

    @RequestMapping(path = "/detail/{conversationId}", method = RequestMethod.GET)
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

    @RequestMapping(path = "/send", method = RequestMethod.POST)
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

}

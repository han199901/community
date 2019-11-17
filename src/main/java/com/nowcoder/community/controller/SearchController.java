package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qhhu
 * @date 2019/11/17 - 14:24
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx(使用get请求时获取参数的方法)
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> discussPostVoList = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost discussPost : searchResult) {
                Map<String, Object> discussPostVo = new HashMap<>();
                // 帖子
                discussPostVo.put("discussPost", discussPost);
                // 作者
                discussPostVo.put("user", userService.getUserById(discussPost.getUserId()));
                // 点赞数量
                discussPostVo.put("likeCount", likeService.getEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));

                discussPostVoList.add(discussPostVo);
            }
        }
        model.addAttribute("discussPostVoList", discussPostVoList);
        model.addAttribute("keyword", keyword); // 用于在搜索到帖子时, 搜索框中仍有keyword

        // 分页信息
        page.setLimit(5);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }

}

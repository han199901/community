package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author qhhu
 * @date 2019/11/29 - 15:23
 */
@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 统计页面
    //                                        使用两种请求方式, 为后面的方法的转发
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    //                  使用注解将传过来的字符串转为date
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                        Model model) {
        // 我感觉页面传过来的日期是字符串, 可以直接用来统计数据, 不用转来转去
        long uv = dataService.calculateUV(startDate, endDate);
        model.addAttribute("uvResult", uv);
        // 用于刷新页面显示查询结果时, 依然显示设置的日期
        model.addAttribute("uvStartDate", startDate);
        model.addAttribute("uvEndDate", endDate);
        // 转发, 声明当前方法只能将请求处理一半, 需要另外一个方法继续做请求
        return "forward:/data";
    }

    // 统计活跃用户
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    //                  使用注解将传过来的字符串转为date
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                        Model model) {
        // 我感觉页面传过来的日期是字符串, 可以直接用来统计数据, 不用转来转去
        long dau = dataService.calculateDAU(startDate, endDate);
        model.addAttribute("dauResult", dau);
        // 用于刷新页面显示查询结果时, 依然显示设置的日期
        model.addAttribute("dauStartDate", startDate);
        model.addAttribute("dauEndDate", endDate);
        // 转发, 声明当前方法只能将请求处理一半, 需要另外一个方法继续做请求
        return "forward:/data";
    }

}

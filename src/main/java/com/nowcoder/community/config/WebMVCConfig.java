package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author qhhu
 * @date 2019/10/27 - 13:25
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                // .excludePathPatterns()排除不用拦截路径, 浏览器访问静态资源时, 不用拦截器
                // 访问静态资源: http://127.0.0.1:8080/community/css/xxx.css
                // "/**/*.css": static路径下的任意文件夹下的任意css文件
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
                // .addPathPatterns()增加需要拦截的路径
    }
}

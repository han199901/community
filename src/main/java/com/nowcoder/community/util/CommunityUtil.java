package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author qhhu
 * @date 2019/10/22 - 15:53
 */
public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        // 随机生成的uuid, 包含字母数字和'-'
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}

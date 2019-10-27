package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author qhhu
 * @date 2019/10/27 - 13:16
 *
 * 持有用户信息, 用于代替session对象
 */
@Component
public class HostHolder {

    // 线程隔离
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}

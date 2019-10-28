package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qhhu
 * @date 2019/10/27 - 22:42
 */

// 使用元注解对当前注解进行描述
// target声明注解用于描述方法
// retention声明注解在程序运行时有效
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {



}

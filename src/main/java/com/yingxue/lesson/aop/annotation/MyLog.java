package com.yingxue.lesson.aop.annotation;

import java.lang.annotation.*;

/**
 * 自定义MyLog注解
 */
@Target(ElementType.METHOD)//注解作用的位置，ElementType.METHOD表示该注解仅能作用于方法上
@Retention(RetentionPolicy.RUNTIME)//注解的生命周期，表示注解会被保留到什么阶段，可以选择编译阶段、类加载阶段，或运行阶段
@Documented//注解信息会被添加到Java文档中
public @interface MyLog {
    /**
     * 用户操作哪个模块
     */
    String title() default "";

    /**
     * 记录用户操作的动作
     */
    String action() default "";
}

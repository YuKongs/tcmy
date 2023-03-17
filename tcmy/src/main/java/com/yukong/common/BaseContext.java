package com.yukong.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 * ThreadLocal 为每一个单独的线程开辟一个独立的空间
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置id值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取id值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}

package com.yingxue.lesson.utils;

import com.github.pagehelper.Page;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtil {
    //构造对象私有
    private PageUtil(){}

    /**
     * 将传入的List封装为VO
     * @param list
     * @param <T>
     * @return
     */
    public static <T> PageVO<T> getPageVO(List<T> list){
        PageVO<T> result=new PageVO<>();
        //判断前面的对象是否是后面的类，或者其子类、实现类的实例。如果是返回true，否则返回false。
        if(list instanceof Page){
            //Page<E> extends ArrayList<E>。强转
            Page<T> page= (Page<T>) list;
            //总条数
            result.setTotalRows(page.getTotal());
            //总页数
            result.setTotalPages(page.getPages());
            //当前页数
            result.setPageNum(page.getPageNum());
            //当前页记录条数
            result.setCurPageSize(page.getPageSize());
            //每页的记录条数
            result.setPageSize(page.size());
            //具体数据
            result.setList(page.getResult());
        }
        return result;
    }
}

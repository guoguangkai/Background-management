package com.yingxue.lesson.service;

import com.yingxue.lesson.vo.resp.HomeRespVO;

/**
 * @ClassName: HomeService
 */
public interface HomeService {
    HomeRespVO getHome(String userId);
}

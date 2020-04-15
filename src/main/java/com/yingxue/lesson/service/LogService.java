package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.vo.req.SysLogPageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;

/**
 * @ClassName: LogService
 */
public interface LogService {

    PageVO<SysLog> pageInfo(SysLogPageReqVO vo);

    void deletedLog(List<String> logIds);
}

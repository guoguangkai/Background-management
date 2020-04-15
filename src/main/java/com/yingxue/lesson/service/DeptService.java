package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.vo.req.DeptAddReqVO;
import com.yingxue.lesson.vo.req.DeptUpdateReqVO;
import com.yingxue.lesson.vo.resp.DeptRespNodeVO;

import java.util.List;

/**
 * @ClassName: DeptService
 */
public interface DeptService {

    List<SysDept> selectAll();

    List<DeptRespNodeVO> deptTreeList(String deptId);

    SysDept addDept(DeptAddReqVO vo);

    void updateDept(DeptUpdateReqVO vo);

    void deletedDept(String id);

}

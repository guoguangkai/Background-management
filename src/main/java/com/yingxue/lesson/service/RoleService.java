package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.vo.req.AddRoleReqVO;
import com.yingxue.lesson.vo.req.RolePageReqVO;
import com.yingxue.lesson.vo.req.RoleUpdateReqVO;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;

/**
 * @ClassName: RoleService
 */
public interface RoleService {
    PageVO<SysRole> pageInfo(RolePageReqVO vo);
    SysRole addRole(AddRoleReqVO vo);
    List<SysRole> selectAll();
    SysRole detailInfo(String id);
    void updateRole(RoleUpdateReqVO vo);
    void deletedRole(String roleId);
    List<String> getRoleNames(String userId);
    List<SysRole> getRoleInfoByUserId(String userId);
}

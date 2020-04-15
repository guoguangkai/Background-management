package com.yingxue.lesson.service;

import com.yingxue.lesson.vo.req.UserOwnRoleReqVO;

import java.util.List;

/**
 * @ClassName: UserRoleService
 */
public interface UserRoleService {

    List<String> getRoleIdsByUserId(String userId);
    void addUserRoleInfo(UserOwnRoleReqVO vo);
    List<String> getUserIdsByRoleIds(List<String> roleIds);
    List<String> getUserIdsBtRoleId(String roleId);
    int removeUserRoleId(String roleId);
}

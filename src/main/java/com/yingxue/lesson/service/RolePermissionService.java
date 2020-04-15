package com.yingxue.lesson.service;

import com.yingxue.lesson.vo.req.RolePermissionOperationReqVO;

import java.util.List;


/**
 * @ClassName: RolePermissionService
 */
public interface RolePermissionService {
    void addRolePermission(RolePermissionOperationReqVO vo);
    List<String> getRoleIdsByPermissionId(String permissionId);
    int removeRoleByPermissionId(String permissionId);
    List<String> getPermissionIdsByRoleId(String roleId);
    int removeByRoleId(String roleId);
    List<String> getPermissionIdsByRoles(List<String> roleIds);

}

package com.demo.mapper;

import com.demo.entity.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysRoleMapper {

    @Select("<script>" +
            "SELECT r.*, (SELECT COUNT(1) FROM sys_user_role ur JOIN sys_user u ON ur.user_id=u.id AND u.deleted=0 WHERE ur.role_id=r.id) AS user_count " +
            "FROM sys_role r WHERE r.deleted = 0 " +
            "<if test='roleName != null and roleName != \"\"'> AND r.role_name LIKE CONCAT('%',#{roleName},'%') </if>" +
            "<if test='roleCode != null and roleCode != \"\"'> AND r.role_code LIKE CONCAT('%',#{roleCode},'%') </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            " ORDER BY r.id" +
            "</script>")
    List<Map<String, Object>> selectList(Map<String, Object> params);

    @Select("SELECT * FROM sys_role WHERE id = #{id} AND deleted = 0")
    SysRole selectById(@Param("id") Long id);

    @Select("SELECT role_code FROM sys_role WHERE deleted = 0")
    List<String> existsCodes();

    @Insert("INSERT INTO sys_role(role_name,role_code,description,status,create_by,update_by) " +
            "VALUES(#{roleName},#{roleCode},#{description},#{status},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    @Update("<script>UPDATE sys_role SET update_by=#{updateBy}" +
            "<if test='roleName != null'>, role_name=#{roleName}</if>" +
            "<if test='description != null'>, description=#{description}</if>" +
            "<if test='status != null'>, status=#{status}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(SysRole role);

    @Update("UPDATE sys_role SET deleted=1, update_by=#{operator} WHERE id=#{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM sys_user_role ur JOIN sys_user u ON ur.user_id=u.id AND u.deleted=0 WHERE ur.role_id=#{id}")
    int countUsers(@Param("id") Long id);

    // 角色-权限
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id=#{id}")
    List<Long> selectPermissionIds(@Param("id") Long id);

    @Delete("DELETE FROM sys_role_permission WHERE role_id=#{id}")
    int clearPermissions(@Param("id") Long id);

    @Insert("<script>INSERT INTO sys_role_permission(role_id,permission_id) VALUES " +
            "<foreach collection='permIds' item='pid' separator=','>(#{id},#{pid})</foreach></script>")
    int insertPermissions(@Param("id") Long id, @Param("permIds") List<Long> permIds);
}

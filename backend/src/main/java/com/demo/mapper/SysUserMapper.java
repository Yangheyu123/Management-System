package com.demo.mapper;

import com.demo.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND deleted = 0")
    SysUser selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    @Select("<script>" +
            "SELECT u.*, c.name AS community_name " +
            "FROM sys_user u LEFT JOIN community c ON u.community_id = c.id AND c.deleted = 0 " +
            "WHERE u.deleted = 0 " +
            "<if test='username != null and username != \"\"'> AND u.username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='realName != null and realName != \"\"'> AND u.real_name LIKE CONCAT('%',#{realName},'%') </if>" +
            "<if test='phone != null and phone != \"\"'> AND u.phone LIKE CONCAT('%',#{phone},'%') </if>" +
            "<if test='userType != null'> AND u.user_type = #{userType} </if>" +
            "<if test='communityId != null'> AND u.community_id = #{communityId} </if>" +
            "<if test='status != null'> AND u.status = #{status} </if>" +
            "ORDER BY u.id" +
            "</script>")
    List<Map<String, Object>> selectList(Map<String, Object> params);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_user u WHERE u.deleted = 0 " +
            "<if test='username != null and username != \"\"'> AND u.username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='realName != null and realName != \"\"'> AND u.real_name LIKE CONCAT('%',#{realName},'%') </if>" +
            "<if test='phone != null and phone != \"\"'> AND u.phone LIKE CONCAT('%',#{phone},'%') </if>" +
            "<if test='userType != null'> AND u.user_type = #{userType} </if>" +
            "<if test='communityId != null'> AND u.community_id = #{communityId} </if>" +
            "<if test='status != null'> AND u.status = #{status} </if>" +
            "</script>")
    long countList(Map<String, Object> params);

    @Select("SELECT r.role_code FROM sys_role r " +
            "JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT r.id, r.role_name AS roleName, r.role_code AS roleCode FROM sys_role r " +
            "JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Map<String, Object>> selectRolesOfUser(@Param("userId") Long userId);

    @Select("SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT p.perm_code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user(username,password,real_name,phone,email,gender,avatar,user_type,community_id,status,create_by,update_by) " +
            "VALUES(#{username},#{password},#{realName},#{phone},#{email},#{gender},#{avatar},#{userType},#{communityId},#{status},#{createBy},#{updateBy})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("<script>" +
            "UPDATE sys_user SET update_by = #{updateBy}" +
            "<if test='realName != null'>, real_name = #{realName}</if>" +
            "<if test='phone != null'>, phone = #{phone}</if>" +
            "<if test='email != null'>, email = #{email}</if>" +
            "<if test='gender != null'>, gender = #{gender}</if>" +
            "<if test='userType != null'>, user_type = #{userType}</if>" +
            "<if test='communityId != null'>, community_id = #{communityId}</if>" +
            "<if test='status != null'>, status = #{status}</if>" +
            " WHERE id = #{id} AND deleted = 0" +
            "</script>")
    int updateById(SysUser user);

    @Update("UPDATE sys_user SET deleted = 1, update_by = #{operator} WHERE id = #{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Update("UPDATE sys_user SET status = #{status}, update_by = #{operator} WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") int status, @Param("operator") String operator);

    @Update("UPDATE sys_user SET password = #{password}, update_by = #{operator} WHERE id = #{id} AND deleted = 0")
    int updatePassword(@Param("id") Long id, @Param("password") String password, @Param("operator") String operator);

    @Update("UPDATE sys_user SET last_login_time = #{time} WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id, @Param("time") LocalDateTime time);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int clearUserRoles(@Param("userId") Long userId);

    @Insert("<script>INSERT INTO sys_user_role(user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='rid' separator=','>(#{userId}, #{rid})</foreach></script>")
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}

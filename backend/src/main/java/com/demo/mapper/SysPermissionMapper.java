package com.demo.mapper;

import com.demo.entity.SysPermission;
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
public interface SysPermissionMapper {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY sort, id")
    List<SysPermission> selectAll();

    @Select("SELECT * FROM sys_permission WHERE id=#{id} AND deleted=0")
    SysPermission selectById(@Param("id") Long id);

    @Select("SELECT perm_code FROM sys_permission WHERE deleted=0")
    List<String> selectAllCodes();

    @Select("<script>SELECT * FROM sys_permission WHERE deleted=0 " +
            "<if test='permName != null and permName != \"\"'> AND perm_name LIKE CONCAT('%',#{permName},'%') </if>" +
            "<if test='permCode != null and permCode != \"\"'> AND perm_code LIKE CONCAT('%',#{permCode},'%') </if>" +
            "<if test='type != null'> AND type=#{type} </if>" +
            " ORDER BY sort, id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM sys_permission WHERE deleted=0 " +
            "<if test='permName != null and permName != \"\"'> AND perm_name LIKE CONCAT('%',#{permName},'%') </if>" +
            "<if test='permCode != null and permCode != \"\"'> AND perm_code LIKE CONCAT('%',#{permCode},'%') </if>" +
            "<if test='type != null'> AND type=#{type} </if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT COUNT(1) FROM sys_permission WHERE parent_id=#{id} AND deleted=0")
    int countChildren(@Param("id") Long id);

    @Insert("INSERT INTO sys_permission(parent_id,perm_name,perm_code,type,path,icon,sort) " +
            "VALUES(#{parentId},#{permName},#{permCode},#{type},#{path},#{icon},#{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysPermission perm);

    @Update("<script>UPDATE sys_permission SET " +
            "<if test='parentId != null'>parent_id=#{parentId},</if>" +
            "<if test='permName != null'>perm_name=#{permName},</if>" +
            "<if test='type != null'>type=#{type},</if>" +
            "<if test='path != null'>path=#{path},</if>" +
            "<if test='icon != null'>icon=#{icon},</if>" +
            "<if test='sort != null'>sort=#{sort},</if>" +
            "perm_code=#{permCode} WHERE id=#{id} AND deleted=0</script>")
    int updateById(SysPermission perm);

    @Update("UPDATE sys_permission SET deleted=1 WHERE id=#{id}")
    int logicDelete(@Param("id") Long id);

    @Delete("DELETE FROM sys_role_permission WHERE permission_id=#{id}")
    int clearRoleRef(@Param("id") Long id);
}

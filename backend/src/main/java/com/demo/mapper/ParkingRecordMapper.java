package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ParkingRecordMapper {

    @Select("<script>SELECT pr.*, ps.space_no AS space_no, o.name AS owner_name, su.real_name AS operator_name " +
            "FROM parking_record pr " +
            "LEFT JOIN parking_space ps ON pr.space_id=ps.id AND ps.deleted=0 " +
            "LEFT JOIN owner o ON pr.owner_id=o.id AND o.deleted=0 " +
            "LEFT JOIN sys_user su ON pr.operator_id=su.id " +
            "WHERE 1=1 " +
            "<if test='spaceId != null'> AND pr.space_id=#{spaceId}</if>" +
            "<if test='ownerId != null'> AND pr.owner_id=#{ownerId}</if>" +
            "<if test='action != null and action != \"\"'> AND pr.action=#{action}</if>" +
            " ORDER BY pr.id DESC LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM parking_record pr WHERE 1=1 " +
            "<if test='spaceId != null'> AND pr.space_id=#{spaceId}</if>" +
            "<if test='ownerId != null'> AND pr.owner_id=#{ownerId}</if>" +
            "<if test='action != null and action != \"\"'> AND pr.action=#{action}</if></script>")
    long countPage(Map<String, Object> params);
}

package com.demo.mapper;

import com.demo.entity.ParkingRecord;
import com.demo.entity.ParkingSpace;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ParkingSpaceMapper {

    @Select("<script>SELECT ps.*, c.name AS community_name, o.name AS owner_name " +
            "FROM parking_space ps " +
            "LEFT JOIN community c ON ps.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN owner o ON ps.owner_id=o.id AND o.deleted=0 " +
            "WHERE ps.deleted=0 " +
            "<if test='communityId != null'> AND ps.community_id=#{communityId}</if>" +
            "<if test='spaceNo != null and spaceNo != \"\"'> AND ps.space_no LIKE CONCAT('%',#{spaceNo},'%')</if>" +
            "<if test='areaType != null'> AND ps.area_type=#{areaType}</if>" +
            "<if test='useType != null'> AND ps.use_type=#{useType}</if>" +
            "<if test='status != null'> AND ps.status=#{status}</if>" +
            "<if test='ownerId != null'> AND ps.owner_id=#{ownerId}</if>" +
            "<if test='plateNo != null and plateNo != \"\"'> AND ps.plate_no LIKE CONCAT('%',#{plateNo},'%')</if>" +
            " ORDER BY ps.id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM parking_space ps WHERE ps.deleted=0 " +
            "<if test='communityId != null'> AND ps.community_id=#{communityId}</if>" +
            "<if test='spaceNo != null and spaceNo != \"\"'> AND ps.space_no LIKE CONCAT('%',#{spaceNo},'%')</if>" +
            "<if test='areaType != null'> AND ps.area_type=#{areaType}</if>" +
            "<if test='useType != null'> AND ps.use_type=#{useType}</if>" +
            "<if test='status != null'> AND ps.status=#{status}</if>" +
            "<if test='ownerId != null'> AND ps.owner_id=#{ownerId}</if>" +
            "<if test='plateNo != null and plateNo != \"\"'> AND ps.plate_no LIKE CONCAT('%',#{plateNo},'%')</if></script>")
    long countPage(Map<String, Object> params);

    @Select("<script>SELECT ps.*, c.name AS community_name, o.name AS owner_name " +
            "FROM parking_space ps " +
            "LEFT JOIN community c ON ps.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN owner o ON ps.owner_id=o.id AND o.deleted=0 " +
            "WHERE ps.id=#{id} AND ps.deleted=0</script>")
    Map<String, Object> selectDetail(@Param("id") Long id);

    @Select("SELECT * FROM parking_space WHERE id=#{id} AND deleted=0")
    ParkingSpace selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM parking_space WHERE community_id=#{communityId} AND space_no=#{spaceNo} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByNo(@Param("communityId") Long communityId, @Param("spaceNo") String spaceNo, @Param("id") Long id);

    @Insert("INSERT INTO parking_space(community_id,space_no,area_type,use_type,monthly_fee,status,remark,create_by,update_by) " +
            "VALUES(#{communityId},#{spaceNo},#{areaType},#{useType},#{monthlyFee},#{status},#{remark},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ParkingSpace ps);

    @Update("<script>UPDATE parking_space SET update_by=#{updateBy}" +
            "<if test='communityId != null'>,community_id=#{communityId}</if>" +
            "<if test='spaceNo != null'>,space_no=#{spaceNo}</if>" +
            "<if test='areaType != null'>,area_type=#{areaType}</if>" +
            "<if test='useType != null'>,use_type=#{useType}</if>" +
            "<if test='monthlyFee != null'>,monthly_fee=#{monthlyFee}</if>" +
            "<if test='status != null'>,status=#{status}</if>" +
            "<if test='remark != null'>,remark=#{remark}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(ParkingSpace ps);

    @Update("UPDATE parking_space SET deleted=1, update_by=#{operator} WHERE id=#{id} AND status=1")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Update("UPDATE parking_space SET owner_id=#{ownerId}, plate_no=#{plateNo}, start_date=#{startDate}, end_date=#{endDate}, " +
            "status=#{status}, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int bind(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("plateNo") String plateNo,
             @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate,
             @Param("status") int status, @Param("operator") String operator);

    @Update("UPDATE parking_space SET owner_id=NULL, plate_no=NULL, start_date=NULL, end_date=NULL, status=1, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int unbind(@Param("id") Long id, @Param("operator") String operator);

    @Update("UPDATE parking_space SET end_date=#{endDate}, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int renew(@Param("id") Long id, @Param("endDate") java.time.LocalDate endDate, @Param("operator") String operator);

    @Insert("INSERT INTO parking_record(space_id,owner_id,plate_no,action,amount,start_date,end_date,operator_id) " +
            "VALUES(#{spaceId},#{ownerId},#{plateNo},#{action},#{amount},#{startDate},#{endDate},#{operatorId})")
    int insertRecord(ParkingRecord pr);
}

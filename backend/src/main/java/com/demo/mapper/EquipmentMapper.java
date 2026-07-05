package com.demo.mapper;

import com.demo.entity.Equipment;
import com.demo.entity.EquipmentCheck;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface EquipmentMapper {

    @Select("<script>SELECT e.*, c.name AS community_name " +
            "FROM equipment e LEFT JOIN community c ON e.community_id=c.id AND c.deleted=0 " +
            "WHERE e.deleted=0 " +
            "<if test='communityId != null'> AND e.community_id=#{communityId}</if>" +
            "<if test='category != null'> AND e.category=#{category}</if>" +
            "<if test='name != null and name != \"\"'> AND e.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='code != null and code != \"\"'> AND e.code LIKE CONCAT('%',#{code},'%')</if>" +
            "<if test='status != null'> AND e.status=#{status}</if>" +
            "<if test='onlineStatus != null'> AND e.online_status=#{onlineStatus}</if>" +
            " ORDER BY e.id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM equipment e WHERE e.deleted=0 " +
            "<if test='communityId != null'> AND e.community_id=#{communityId}</if>" +
            "<if test='category != null'> AND e.category=#{category}</if>" +
            "<if test='name != null and name != \"\"'> AND e.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='code != null and code != \"\"'> AND e.code LIKE CONCAT('%',#{code},'%')</if>" +
            "<if test='status != null'> AND e.status=#{status}</if>" +
            "<if test='onlineStatus != null'> AND e.online_status=#{onlineStatus}</if></script>")
    long countPage(Map<String, Object> params);

    @Select("<script>SELECT e.*, c.name AS community_name " +
            "FROM equipment e LEFT JOIN community c ON e.community_id=c.id AND c.deleted=0 " +
            "WHERE e.id=#{id} AND e.deleted=0</script>")
    Map<String, Object> selectDetail(@Param("id") Long id);

    @Select("SELECT * FROM equipment WHERE id=#{id} AND deleted=0")
    Equipment selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM equipment WHERE community_id=#{communityId} AND category=#{category} AND code=#{code} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByCode(@Param("communityId") Long communityId, @Param("category") Integer category, @Param("code") String code, @Param("id") Long id);

    @Insert("INSERT INTO equipment(community_id,category,name,code,location,model,manufacturer,install_date,warranty_date,online_status,status,next_check_date,remark,create_by,update_by) " +
            "VALUES(#{communityId},#{category},#{name},#{code},#{location},#{model},#{manufacturer},#{installDate},#{warrantyDate},#{onlineStatus},#{status},#{nextCheckDate},#{remark},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Equipment e);

    @Update("<script>UPDATE equipment SET update_by=#{updateBy}" +
            "<if test='communityId != null'>,community_id=#{communityId}</if>" +
            "<if test='category != null'>,category=#{category}</if>" +
            "<if test='name != null'>,name=#{name}</if>" +
            "<if test='code != null'>,code=#{code}</if>" +
            "<if test='location != null'>,location=#{location}</if>" +
            "<if test='model != null'>,model=#{model}</if>" +
            "<if test='manufacturer != null'>,manufacturer=#{manufacturer}</if>" +
            "<if test='installDate != null'>,install_date=#{installDate}</if>" +
            "<if test='warrantyDate != null'>,warranty_date=#{warrantyDate}</if>" +
            "<if test='onlineStatus != null'>,online_status=#{onlineStatus}</if>" +
            "<if test='status != null'>,status=#{status}</if>" +
            "<if test='nextCheckDate != null'>,next_check_date=#{nextCheckDate}</if>" +
            "<if test='remark != null'>,remark=#{remark}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(Equipment e);

    @Update("UPDATE equipment SET deleted=1, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM equipment_check WHERE equipment_id=#{id}")
    int countChecks(@Param("id") Long id);

    @Update("UPDATE equipment SET last_check_date=#{last}, next_check_date=#{next}, status=#{status}, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int updateCheckStatus(@Param("id") Long id, @Param("last") LocalDate last, @Param("next") LocalDate next, @Param("status") int status, @Param("operator") String operator);

    @Insert("INSERT INTO equipment_check(equipment_id,checker_id,check_time,result,issue_desc,images) " +
            "VALUES(#{equipmentId},#{checkerId},#{checkTime},#{result},#{issueDesc},#{images})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCheck(EquipmentCheck c);

    @Select("<script>SELECT ec.*, su.real_name AS checker_name " +
            "FROM equipment_check ec LEFT JOIN sys_user su ON ec.checker_id=su.id " +
            "WHERE ec.equipment_id=#{equipmentId} ORDER BY ec.check_time DESC LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectChecks(@Param("equipmentId") Long equipmentId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM equipment_check WHERE equipment_id=#{equipmentId}")
    long countChecksOf(@Param("equipmentId") Long equipmentId);

    // 到期提醒查询
    @Select("SELECT e.id,e.name,e.code,e.category,c.name AS community_name,e.warranty_date,e.next_check_date,e.online_status " +
            "FROM equipment e LEFT JOIN community c ON e.community_id=c.id AND c.deleted=0 " +
            "WHERE e.deleted=0 <if test='communityId != null'> AND e.community_id=#{communityId}</if>")
    List<Map<String, Object>> selectForExpiring(@Param("communityId") Long communityId);
}

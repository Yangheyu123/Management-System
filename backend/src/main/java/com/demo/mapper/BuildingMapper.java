package com.demo.mapper;

import com.demo.entity.Building;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface BuildingMapper {

    @Select("<script>SELECT b.*, c.name AS community_name, " +
            "(SELECT COUNT(1) FROM house h WHERE h.building_id=b.id AND h.deleted=0) AS house_count " +
            "FROM building b LEFT JOIN community c ON b.community_id=c.id AND c.deleted=0 " +
            "WHERE b.deleted=0 " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='name != null and name != \"\"'> AND b.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='buildingNo != null and buildingNo != \"\"'> AND b.building_no LIKE CONCAT('%',#{buildingNo},'%')</if>" +
            " ORDER BY b.id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM building b WHERE b.deleted=0 " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='name != null and name != \"\"'> AND b.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='buildingNo != null and buildingNo != \"\"'> AND b.building_no LIKE CONCAT('%',#{buildingNo},'%')</if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT * FROM building WHERE id=#{id} AND deleted=0")
    Building selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM building WHERE community_id=#{communityId} AND building_no=#{buildingNo} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByNo(@Param("communityId") Long communityId, @Param("buildingNo") String buildingNo, @Param("id") Long id);

    @Insert("INSERT INTO building(community_id,name,building_no,floors,units,elevators,structure_type,remark,create_by,update_by) " +
            "VALUES(#{communityId},#{name},#{buildingNo},#{floors},#{units},#{elevators},#{structureType},#{remark},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Building b);

    @Update("<script>UPDATE building SET update_by=#{updateBy}" +
            "<if test='communityId != null'>,community_id=#{communityId}</if>" +
            "<if test='name != null'>,name=#{name}</if>" +
            "<if test='buildingNo != null'>,building_no=#{buildingNo}</if>" +
            "<if test='floors != null'>,floors=#{floors}</if>" +
            "<if test='units != null'>,units=#{units}</if>" +
            "<if test='elevators != null'>,elevators=#{elevators}</if>" +
            "<if test='structureType != null'>,structure_type=#{structureType}</if>" +
            "<if test='remark != null'>,remark=#{remark}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(Building b);

    @Update("UPDATE building SET deleted=1, update_by=#{operator} WHERE id=#{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM house WHERE building_id=#{id} AND deleted=0")
    int countHouses(@Param("id") Long id);
}

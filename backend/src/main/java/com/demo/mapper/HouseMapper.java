package com.demo.mapper;

import com.demo.entity.House;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface HouseMapper {

    @Select("<script>SELECT h.*, c.name AS community_name, b.name AS building_name, " +
            "GROUP_CONCAT(DISTINCT o.name) AS owner_names " +
            "FROM house h " +
            "LEFT JOIN community c ON h.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN building b ON h.building_id=b.id AND b.deleted=0 " +
            "LEFT JOIN owner_house oh ON oh.house_id=h.id " +
            "LEFT JOIN owner o ON oh.owner_id=o.id AND o.deleted=0 " +
            "WHERE h.deleted=0 " +
            "<if test='communityId != null'> AND h.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>" +
            "<if test='houseNo != null and houseNo != \"\"'> AND h.house_no LIKE CONCAT('%',#{houseNo},'%')</if>" +
            "<if test='status != null'> AND h.status=#{status}</if>" +
            "<if test='hasOwner != null and hasOwner == 1'> AND EXISTS(SELECT 1 FROM owner_house oh2 WHERE oh2.house_id=h.id)</if>" +
            " GROUP BY h.id ORDER BY h.id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(DISTINCT h.id) FROM house h WHERE h.deleted=0 " +
            "<if test='communityId != null'> AND h.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>" +
            "<if test='houseNo != null and houseNo != \"\"'> AND h.house_no LIKE CONCAT('%',#{houseNo},'%')</if>" +
            "<if test='status != null'> AND h.status=#{status}</if>" +
            "<if test='hasOwner != null and hasOwner == 1'> AND EXISTS(SELECT 1 FROM owner_house oh2 WHERE oh2.house_id=h.id)</if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT * FROM house WHERE id=#{id} AND deleted=0")
    House selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM house WHERE building_id=#{buildingId} AND house_no=#{houseNo} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByNo(@Param("buildingId") Long buildingId, @Param("houseNo") String houseNo, @Param("id") Long id);

    @Insert("INSERT INTO house(community_id,building_id,house_no,unit_no,floor_no,area,layout,status,remark,create_by,update_by) " +
            "VALUES(#{communityId},#{buildingId},#{houseNo},#{unitNo},#{floorNo},#{area},#{layout},#{status},#{remark},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(House h);

    @Update("<script>UPDATE house SET update_by=#{updateBy}" +
            "<if test='communityId != null'>,community_id=#{communityId}</if>" +
            "<if test='buildingId != null'>,building_id=#{buildingId}</if>" +
            "<if test='houseNo != null'>,house_no=#{houseNo}</if>" +
            "<if test='unitNo != null'>,unit_no=#{unitNo}</if>" +
            "<if test='floorNo != null'>,floor_no=#{floorNo}</if>" +
            "<if test='area != null'>,area=#{area}</if>" +
            "<if test='layout != null'>,layout=#{layout}</if>" +
            "<if test='status != null'>,status=#{status}</if>" +
            "<if test='remark != null'>,remark=#{remark}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(House h);

    @Update("UPDATE house SET deleted=1, update_by=#{operator} WHERE id=#{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM bill WHERE house_id=#{id} AND status IN(1,2) AND deleted=0")
    int countUnfinishedBills(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM work_order WHERE house_id=#{id} AND status IN(1,2,3) AND deleted=0")
    int countActiveOrders(@Param("id") Long id);
}

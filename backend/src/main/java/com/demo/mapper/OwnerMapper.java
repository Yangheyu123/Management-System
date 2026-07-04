package com.demo.mapper;

import com.demo.entity.Owner;
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
public interface OwnerMapper {

    @Select("<script>SELECT o.*, " +
            "(SELECT COUNT(1) FROM owner_house oh JOIN house h ON oh.house_id=h.id AND h.deleted=0 WHERE oh.owner_id=o.id) AS house_count, " +
            "(SELECT GROUP_CONCAT(h.house_no) FROM owner_house oh JOIN house h ON oh.house_id=h.id AND h.deleted=0 WHERE oh.owner_id=o.id) AS house_names " +
            "FROM owner o WHERE o.deleted=0 " +
            "<if test='name != null and name != \"\"'> AND o.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='phone != null and phone != \"\"'> AND o.phone LIKE CONCAT('%',#{phone},'%')</if>" +
            "<if test='idCard != null and idCard != \"\"'> AND o.id_card LIKE CONCAT('%',#{idCard},'%')</if>" +
            "<if test='plateNo != null and plateNo != \"\"'> AND o.plate_no LIKE CONCAT('%',#{plateNo},'%')</if>" +
            "<if test='status != null'> AND o.status=#{status}</if>" +
            " ORDER BY o.id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM owner o WHERE o.deleted=0 " +
            "<if test='name != null and name != \"\"'> AND o.name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='phone != null and phone != \"\"'> AND o.phone LIKE CONCAT('%',#{phone},'%')</if>" +
            "<if test='idCard != null and idCard != \"\"'> AND o.id_card LIKE CONCAT('%',#{idCard},'%')</if>" +
            "<if test='plateNo != null and plateNo != \"\"'> AND o.plate_no LIKE CONCAT('%',#{plateNo},'%')</if>" +
            "<if test='status != null'> AND o.status=#{status}</if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT * FROM owner WHERE id=#{id} AND deleted=0")
    Owner selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM owner WHERE id_card=#{idCard} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByIdCard(@Param("idCard") String idCard, @Param("id") Long id);

    @Insert("INSERT INTO owner(name,phone,id_card,gender,plate_no,move_in_date,status,remark,create_by,update_by) " +
            "VALUES(#{name},#{phone},#{idCard},#{gender},#{plateNo},#{moveInDate},#{status},#{remark},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Owner o);

    @Update("<script>UPDATE owner SET update_by=#{updateBy}" +
            "<if test='name != null'>,name=#{name}</if>" +
            "<if test='phone != null'>,phone=#{phone}</if>" +
            "<if test='idCard != null'>,id_card=#{idCard}</if>" +
            "<if test='gender != null'>,gender=#{gender}</if>" +
            "<if test='plateNo != null'>,plate_no=#{plateNo}</if>" +
            "<if test='moveInDate != null'>,move_in_date=#{moveInDate}</if>" +
            "<if test='status != null'>,status=#{status}</if>" +
            "<if test='remark != null'>,remark=#{remark}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(Owner o);

    @Update("UPDATE owner SET deleted=1, update_by=#{operator} WHERE id=#{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM bill WHERE owner_id=#{id} AND status IN(1,2) AND deleted=0")
    int countUnfinishedBills(@Param("id") Long id);

    // 业主-房屋关联
    @Insert("<script>INSERT INTO owner_house(owner_id,house_id,relation,is_primary) VALUES " +
            "<foreach collection='bindings' item='b' separator=','>(#{b.ownerId},#{b.houseId},#{b.relation},#{b.isPrimary})</foreach></script>")
    int insertBindings(@Param("bindings") List<Map<String, Object>> bindings);

    @Select("SELECT oh.owner_id AS ownerId, o.name, o.phone, oh.relation, oh.is_primary AS isPrimary " +
            "FROM owner_house oh JOIN owner o ON oh.owner_id=o.id AND o.deleted=0 WHERE oh.house_id=#{houseId}")
    List<Map<String, Object>> selectOwnersOfHouse(@Param("houseId") Long houseId);

    @Select("SELECT oh.house_id AS houseId, h.house_no AS houseNo, b.name AS buildingName, c.name AS communityName, h.area, oh.relation, oh.is_primary AS isPrimary " +
            "FROM owner_house oh JOIN house h ON oh.house_id=h.id AND h.deleted=0 " +
            "LEFT JOIN building b ON h.building_id=b.id AND b.deleted=0 " +
            "LEFT JOIN community c ON h.community_id=c.id AND c.deleted=0 " +
            "WHERE oh.owner_id=#{ownerId}")
    List<Map<String, Object>> selectHousesOfOwner(@Param("ownerId") Long ownerId);

    @Delete("DELETE FROM owner_house WHERE owner_id=#{ownerId}")
    int clearOwnerHouses(@Param("ownerId") Long ownerId);

    @Insert("INSERT INTO owner_house(owner_id,house_id,relation,is_primary) VALUES(#{ownerId},#{houseId},#{relation},#{isPrimary})")
    int addOwnerHouse(@Param("ownerId") Long ownerId, @Param("houseId") Long houseId, @Param("relation") String relation, @Param("isPrimary") Integer isPrimary);
}

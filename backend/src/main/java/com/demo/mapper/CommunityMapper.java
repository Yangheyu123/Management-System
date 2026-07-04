package com.demo.mapper;

import com.demo.entity.Community;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {

    @Select("<script>SELECT * FROM community WHERE deleted=0 " +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='address != null and address != \"\"'> AND address LIKE CONCAT('%',#{address},'%')</if>" +
            "<if test='status != null'> AND status=#{status}</if>" +
            " ORDER BY id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM community WHERE deleted=0 " +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='address != null and address != \"\"'> AND address LIKE CONCAT('%',#{address},'%')</if>" +
            "<if test='status != null'> AND status=#{status}</if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT id, name FROM community WHERE deleted=0 AND status=1 ORDER BY id")
    List<Map<String, Object>> selectAll();

    @Select("SELECT * FROM community WHERE id=#{id} AND deleted=0")
    Community selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM community WHERE name=#{name} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByName(@Param("name") String name, @Param("id") Long id);

    @Insert("INSERT INTO community(name,address,area,green_rate,build_year,developer,total_buildings,total_houses,contact_name,contact_phone,status,create_by,update_by) " +
            "VALUES(#{name},#{address},#{area},#{greenRate},#{buildYear},#{developer},#{totalBuildings},#{totalHouses},#{contactName},#{contactPhone},#{status},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Community c);

    @Update("<script>UPDATE community SET update_by=#{updateBy}" +
            "<if test='name != null'>,name=#{name}</if>" +
            "<if test='address != null'>,address=#{address}</if>" +
            "<if test='area != null'>,area=#{area}</if>" +
            "<if test='greenRate != null'>,green_rate=#{greenRate}</if>" +
            "<if test='buildYear != null'>,build_year=#{buildYear}</if>" +
            "<if test='developer != null'>,developer=#{developer}</if>" +
            "<if test='totalBuildings != null'>,total_buildings=#{totalBuildings}</if>" +
            "<if test='totalHouses != null'>,total_houses=#{totalHouses}</if>" +
            "<if test='contactName != null'>,contact_name=#{contactName}</if>" +
            "<if test='contactPhone != null'>,contact_phone=#{contactPhone}</if>" +
            "<if test='status != null'>,status=#{status}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateById(Community c);

    @Update("UPDATE community SET deleted=1, update_by=#{operator} WHERE id=#{id}")
    int logicDelete(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM building WHERE community_id=#{id} AND deleted=0")
    int countBuildings(@Param("id") Long id);

    @Select("SELECT name FROM community WHERE id=#{id} AND deleted=0")
    String nameOf(@Param("id") Long id);
}

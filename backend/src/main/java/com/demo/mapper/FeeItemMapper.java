package com.demo.mapper;

import com.demo.entity.FeeItem;
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
public interface FeeItemMapper {

    @Select("<script>SELECT * FROM fee_item WHERE deleted=0 " +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='type != null'> AND type=#{type}</if>" +
            "<if test='status != null'> AND status=#{status}</if>" +
            " ORDER BY id LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM fee_item WHERE deleted=0 " +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='type != null'> AND type=#{type}</if>" +
            "<if test='status != null'> AND status=#{status}</if></script>")
    long countPage(Map<String, Object> params);

    @Select("SELECT * FROM fee_item WHERE deleted=0 ORDER BY id")
    List<FeeItem> selectAll();

    @Select("SELECT * FROM fee_item WHERE id=#{id} AND deleted=0")
    FeeItem selectById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM fee_item WHERE name=#{name} AND deleted=0 <if test='id != null'> AND id&lt;&gt;#{id}</if>")
    int countByName(@Param("name") String name, @Param("id") Long id);

    @Insert("INSERT INTO fee_item(name,type,unit,unit_price,billing_cycle,status) " +
            "VALUES(#{name},#{type},#{unit},#{unitPrice},#{billingCycle},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeeItem f);

    @Update("<script>UPDATE fee_item SET " +
            "<if test='name != null'>name=#{name},</if>" +
            "<if test='type != null'>type=#{type},</if>" +
            "<if test='unit != null'>unit=#{unit},</if>" +
            "<if test='unitPrice != null'>unit_price=#{unitPrice},</if>" +
            "<if test='billingCycle != null'>billing_cycle=#{billingCycle},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "id=#{id} WHERE id=#{id} AND deleted=0</script>")
    int updateById(FeeItem f);

    @Delete("UPDATE fee_item SET deleted=1 WHERE id=#{id}")
    int logicDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM bill WHERE fee_item_id=#{id} AND deleted=0")
    int countBills(@Param("id") Long id);
}

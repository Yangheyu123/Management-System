package com.demo.mapper;

import com.demo.entity.Bill;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillMapper {

    @Select("<script>SELECT b.*, c.name AS community_name, h.house_no AS house_no, o.name AS owner_name, " +
            "fi.name AS fee_item_name, fi.type AS fee_item_type " +
            "FROM bill b " +
            "LEFT JOIN community c ON b.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN house h ON b.house_id=h.id AND h.deleted=0 " +
            "LEFT JOIN owner o ON b.owner_id=o.id AND o.deleted=0 " +
            "LEFT JOIN fee_item fi ON b.fee_item_id=fi.id AND fi.deleted=0 " +
            "WHERE b.deleted=0 " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>" +
            "<if test='houseId != null'> AND b.house_id=#{houseId}</if>" +
            "<if test='ownerId != null'> AND b.owner_id=#{ownerId}</if>" +
            "<if test='feeItemType != null'> AND fi.type=#{feeItemType}</if>" +
            "<if test='status != null'> AND b.status=#{status}</if>" +
            "<if test='period != null and period != \"\"'> AND b.period=#{period}</if>" +
            "<if test='overdue != null and overdue == 1'> AND b.status IN(1,2) AND b.due_date &lt; CURDATE()</if>" +
            " ORDER BY b.id DESC LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM bill b LEFT JOIN fee_item fi ON b.fee_item_id=fi.id AND fi.deleted=0 " +
            "LEFT JOIN house h ON b.house_id=h.id AND h.deleted=0 WHERE b.deleted=0 " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>" +
            "<if test='houseId != null'> AND b.house_id=#{houseId}</if>" +
            "<if test='ownerId != null'> AND b.owner_id=#{ownerId}</if>" +
            "<if test='feeItemType != null'> AND fi.type=#{feeItemType}</if>" +
            "<if test='status != null'> AND b.status=#{status}</if>" +
            "<if test='period != null and period != \"\"'> AND b.period=#{period}</if>" +
            "<if test='overdue != null and overdue == 1'> AND b.status IN(1,2) AND b.due_date &lt; CURDATE()</if></script>")
    long countPage(Map<String, Object> params);

    @Select("<script>SELECT b.*, c.name AS community_name, h.house_no AS house_no, o.name AS owner_name, " +
            "fi.name AS fee_item_name, fi.type AS fee_item_type " +
            "FROM bill b " +
            "LEFT JOIN community c ON b.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN house h ON b.house_id=h.id AND h.deleted=0 " +
            "LEFT JOIN owner o ON b.owner_id=o.id AND o.deleted=0 " +
            "LEFT JOIN fee_item fi ON b.fee_item_id=fi.id AND fi.deleted=0 " +
            "WHERE b.id=#{id} AND b.deleted=0</script>")
    Map<String, Object> selectDetail(@Param("id") Long id);

    @Select("SELECT * FROM bill WHERE id=#{id} AND deleted=0")
    Bill selectById(@Param("id") Long id);

    @Insert("<script>INSERT INTO bill(bill_no,community_id,house_id,owner_id,fee_item_id,period,quantity,amount,paid_amount,status,due_date,remark,create_by,update_by) VALUES " +
            "<foreach collection='bills' item='b' separator=','>" +
            "(#{b.billNo},#{b.communityId},#{b.houseId},#{b.ownerId},#{b.feeItemId},#{b.period},#{b.quantity},#{b.amount},#{b.paidAmount},#{b.status},#{b.dueDate},#{b.remark},#{b.createBy},#{b.updateBy})" +
            "</foreach></script>")
    int batchInsert(@Param("bills") List<Bill> bills);

    @Update("UPDATE bill SET paid_amount=paid_amount + #{amount}, status=#{status}, update_by=#{operator} WHERE id=#{id} AND deleted=0")
    int addPaidAmount(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount, @Param("status") int status, @Param("operator") String operator);

    @Update("UPDATE bill SET status=4, update_by=#{operator} WHERE id=#{id} AND status=1 AND deleted=0")
    int voidBill(@Param("id") Long id, @Param("operator") String operator);

    @Select("SELECT COUNT(1) FROM bill WHERE house_id=#{houseId} AND fee_item_id=#{feeItemId} AND period=#{period} AND deleted=0")
    int countExists(@Param("houseId") Long houseId, @Param("feeItemId") Long feeItemId, @Param("period") String period);

    @Select("SELECT COUNT(1) FROM payment WHERE bill_id=#{id}")
    int countPayments(@Param("id") Long id);

    // 按社区+收费类型查询可计费房屋(已入住/已售)及其面积
    @Select("SELECT h.id AS house_id, h.community_id, oh.owner_id, h.area " +
            "FROM house h JOIN owner_house oh ON oh.house_id=h.id AND oh.is_primary=1 " +
            "WHERE h.deleted=0 AND h.status IN(2,3) " +
            "<if test='communityId != null'> AND h.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>")
    List<Map<String, Object>> selectBillableHouses(Map<String, Object> params);
}

package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatMapper {

    @Select("<script>SELECT COUNT(*) FROM house WHERE deleted=0 <if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    long houseTotal(@Param("communityId") Long communityId);

    @Select("<script>SELECT COUNT(*) FROM owner WHERE deleted=0 <if test='communityId != null'>(1=0)</if></script>")
    long ownerTotalAll();

    @Select("<script>SELECT COUNT(*) FROM owner WHERE deleted=0 AND status=1</script>")
    long ownerTotal();

    @Select("<script>SELECT COUNT(*) FROM house WHERE deleted=0 AND status=3 <if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    long houseCheckIn(@Param("communityId") Long communityId);

    @Select("<script>SELECT IFNULL(SUM(amount),0) AS receivable, IFNULL(SUM(paid_amount),0) AS received, " +
            "IFNULL(SUM(amount-paid_amount),0) AS unpaid " +
            "FROM bill WHERE deleted=0 AND status IN(1,2,3) " +
            "AND DATE_FORMAT(create_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m') " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    Map<String, Object> monthCharge(@Param("communityId") Long communityId);

    @Select("<script>SELECT IFNULL(SUM(p.amount),0) AS received FROM payment p " +
            "JOIN bill b ON p.bill_id=b.id AND b.deleted=0 " +
            "WHERE DATE_FORMAT(p.pay_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m') " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if></script>")
    Map<String, Object> monthReceived(@Param("communityId") Long communityId);

    @Select("<script>SELECT IFNULL(ROUND(SUM(CASE WHEN online_status=1 THEN 1 ELSE 0 END)*100/COUNT(*),1),0) AS rate FROM equipment WHERE deleted=0 " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    Map<String, Object> equipmentOnlineRate(@Param("communityId") Long communityId);

    @Select("<script>SELECT COUNT(*) FROM bill WHERE deleted=0 AND status IN(1,2) AND due_date &lt; CURDATE() " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    long overdueBillCount(@Param("communityId") Long communityId);

    @Select("SELECT COUNT(*) FROM work_order WHERE deleted=0 AND status IN(1,2,3)")
    long workorderPending();

    @Select("<script>SELECT COUNT(*) FROM work_order WHERE DATE_FORMAT(create_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m') " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if> AND deleted=0</script>")
    long countThisMonth(@Param("communityId") Long communityId);

    @Select("<script>SELECT COUNT(*) FROM equipment WHERE deleted=0 AND " +
            "(warranty_date IS NOT NULL AND warranty_date &lt;= DATE_ADD(CURDATE(),INTERVAL 30 DAY) " +
            "OR next_check_date IS NOT NULL AND next_check_date &lt;= DATE_ADD(CURDATE(),INTERVAL 7 DAY)) " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    long equipmentExpiring(@Param("communityId") Long communityId);

    // ===== 收费汇总 =====
    @Select("<script>SELECT IFNULL(SUM(b.amount),0) AS receivable, IFNULL(SUM(b.paid_amount),0) AS received, " +
            "IFNULL(SUM(b.amount-b.paid_amount),0) AS unpaid, COUNT(*) AS bill_count, " +
            "SUM(CASE WHEN b.status=3 THEN 1 ELSE 0 END) AS paid_count, " +
            "SUM(CASE WHEN b.status IN(1,2) AND b.due_date &lt; CURDATE() THEN 1 ELSE 0 END) AS overdue_count " +
            "FROM bill b LEFT JOIN fee_item fi ON b.fee_item_id=fi.id AND fi.deleted=0 " +
            "LEFT JOIN house h ON b.house_id=h.id AND h.deleted=0 " +
            "WHERE b.deleted=0 AND b.status IN(1,2,3) " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='buildingId != null'> AND h.building_id=#{buildingId}</if>" +
            "<if test='feeItemType != null'> AND fi.type=#{feeItemType}</if>" +
            "<if test='period != null and period != \"\"'> AND b.period=#{period}</if>" +
            "<if test='startDate != null'> AND b.create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND b.create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if></script>")
    Map<String, Object> chargeSummary(Map<String, Object> params);

    @Select("<script>SELECT DATE_FORMAT(CONCAT(m,'-01'),'%Y-%m') AS period, " +
            "IFNULL(SUM(b.amount),0) AS receivable, IFNULL(SUM(b.paid_amount),0) AS received " +
            "FROM (" +
            "<foreach collection='months' item='mm' separator='UNION ALL'>SELECT #{mm} AS m</foreach>" +
            ") months LEFT JOIN bill b ON DATE_FORMAT(b.create_time,'%Y-%m')=m AND b.deleted=0 AND b.status IN(1,2,3) " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='feeItemType != null'> AND EXISTS(SELECT 1 FROM fee_item f2 WHERE f2.id=b.fee_item_id AND f2.type=#{feeItemType})</if>" +
            " GROUP BY m ORDER BY m</script>")
    List<Map<String, Object>> chargeTrend(Map<String, Object> params);

    @Select("<script>SELECT CASE fi.type WHEN 1 THEN '物业费' WHEN 2 THEN '水费' WHEN 3 THEN '电费' WHEN 4 THEN '车位费' ELSE '其他' END AS name, " +
            "IFNULL(SUM(b.paid_amount),0) AS value " +
            "FROM bill b JOIN fee_item fi ON b.fee_item_id=fi.id AND fi.deleted=0 " +
            "WHERE b.deleted=0 AND b.paid_amount>0 " +
            "<if test='communityId != null'> AND b.community_id=#{communityId}</if>" +
            "<if test='startDate != null'> AND b.create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND b.create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if>" +
            " GROUP BY fi.type ORDER BY fi.type</script>")
    List<Map<String, Object>> chargeByType(Map<String, Object> params);

    // ===== 工单 =====
    @Select("<script>SELECT COUNT(*) AS total, " +
            "SUM(CASE WHEN status=4 THEN 1 ELSE 0 END) AS finished, " +
            "SUM(CASE WHEN status IN(2,3) THEN 1 ELSE 0 END) AS handling, " +
            "SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS pending, " +
            "SUM(CASE WHEN status=6 THEN 1 ELSE 0 END) AS canceled, " +
            "ROUND(AVG(CASE WHEN status>=4 AND finish_time IS NOT NULL AND handle_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR,handle_time,finish_time) END),1) AS avg_handle_hours, " +
            "ROUND(AVG(rating),1) AS avg_rating " +
            "FROM work_order WHERE deleted=0 " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if>" +
            "<if test='startDate != null'> AND create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if></script>")
    Map<String, Object> workorderSummary(Map<String, Object> params);

    @Select("<script>SELECT status, COUNT(*) AS count FROM work_order WHERE deleted=0 " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if> GROUP BY status ORDER BY status</script>")
    List<Map<String, Object>> workorderByStatus(@Param("communityId") Long communityId);

    @Select("<script>SELECT w.handler_id AS handlerId, u.real_name AS handlerName, " +
            "SUM(CASE WHEN w.status>=4 THEN 1 ELSE 0 END) AS finishedCount, " +
            "ROUND(AVG(CASE WHEN w.status>=4 AND w.finish_time IS NOT NULL AND w.handle_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR,w.handle_time,w.finish_time) END),1) AS avgHandleHours, " +
            "ROUND(AVG(w.rating),1) AS avgRating " +
            "FROM work_order w JOIN sys_user u ON w.handler_id=u.id " +
            "WHERE w.deleted=0 AND w.handler_id IS NOT NULL AND w.status>=4 " +
            "<if test='communityId != null'> AND w.community_id=#{communityId}</if>" +
            "<if test='startDate != null'> AND w.create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND w.create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if>" +
            " GROUP BY w.handler_id, u.real_name ORDER BY finishedCount DESC</script>")
    List<Map<String, Object>> workorderByHandler(Map<String, Object> params);

    // ===== 车位 =====
    @Select("<script>SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN status=2 THEN 1 ELSE 0 END) AS inUse, " +
            "SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS free, " +
            "SUM(CASE WHEN status=3 THEN 1 ELSE 0 END) AS sold " +
            "FROM parking_space WHERE deleted=0 <if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    Map<String, Object> parkingSummary(@Param("communityId") Long communityId);

    @Select("<script>SELECT area_type AS areaType, COUNT(*) AS total, " +
            "SUM(CASE WHEN status=2 THEN 1 ELSE 0 END) AS inUse " +
            "FROM parking_space WHERE deleted=0 <if test='communityId != null'> AND community_id=#{communityId}</if> " +
            "GROUP BY area_type</script>")
    List<Map<String, Object>> parkingByArea(@Param("communityId") Long communityId);

    // ===== 设备 =====
    @Select("<script>SELECT category, COUNT(*) AS total, " +
            "SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS normal, " +
            "SUM(CASE WHEN status=2 THEN 1 ELSE 0 END) AS fault, " +
            "SUM(CASE WHEN status=3 THEN 1 ELSE 0 END) AS repairing, " +
            "SUM(CASE WHEN status=4 THEN 1 ELSE 0 END) AS scrapped, " +
            "SUM(CASE WHEN online_status=1 THEN 1 ELSE 0 END) AS online " +
            "FROM equipment WHERE deleted=0 <if test='communityId != null'> AND community_id=#{communityId}</if> " +
            "GROUP BY category ORDER BY category</script>")
    List<Map<String, Object>> equipmentByCategory(@Param("communityId") Long communityId);
}

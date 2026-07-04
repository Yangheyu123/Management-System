package com.demo.mapper;

import com.demo.entity.WorkOrder;
import com.demo.entity.WorkOrderLog;
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
public interface WorkOrderMapper {

    @Select("<script>SELECT w.*, c.name AS community_name, h.house_no AS house_no, " +
            "o.name AS owner_name, o.phone AS owner_phone, u.real_name AS handler_name " +
            "FROM work_order w " +
            "LEFT JOIN community c ON w.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN house h ON w.house_id=h.id AND h.deleted=0 " +
            "LEFT JOIN owner o ON w.owner_id=o.id AND o.deleted=0 " +
            "LEFT JOIN sys_user u ON w.handler_id=u.id " +
            "WHERE w.deleted=0 " +
            "<if test='communityId != null'> AND w.community_id=#{communityId}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND w.order_no=#{orderNo}</if>" +
            "<if test='houseId != null'> AND w.house_id=#{houseId}</if>" +
            "<if test='ownerId != null'> AND w.owner_id=#{ownerId}</if>" +
            "<if test='type != null'> AND w.type=#{type}</if>" +
            "<if test='priority != null'> AND w.priority=#{priority}</if>" +
            "<if test='status != null'> AND w.status=#{status}</if>" +
            "<if test='handlerId != null'> AND w.handler_id=#{handlerId}</if>" +
            "<if test='startDate != null'> AND w.create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND w.create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if>" +
            " ORDER BY w.id DESC LIMIT #{offset}, #{size}</script>")
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    @Select("<script>SELECT COUNT(*) FROM work_order w WHERE w.deleted=0 " +
            "<if test='communityId != null'> AND w.community_id=#{communityId}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND w.order_no=#{orderNo}</if>" +
            "<if test='houseId != null'> AND w.house_id=#{houseId}</if>" +
            "<if test='ownerId != null'> AND w.owner_id=#{ownerId}</if>" +
            "<if test='type != null'> AND w.type=#{type}</if>" +
            "<if test='priority != null'> AND w.priority=#{priority}</if>" +
            "<if test='status != null'> AND w.status=#{status}</if>" +
            "<if test='handlerId != null'> AND w.handler_id=#{handlerId}</if>" +
            "<if test='startDate != null'> AND w.create_time &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND w.create_time &lt; DATE_ADD(#{endDate},INTERVAL 1 DAY)</if></script>")
    long countPage(Map<String, Object> params);

    @Select("<script>SELECT w.*, c.name AS community_name, h.house_no AS house_no, " +
            "o.name AS owner_name, o.phone AS owner_phone, u.real_name AS handler_name " +
            "FROM work_order w " +
            "LEFT JOIN community c ON w.community_id=c.id AND c.deleted=0 " +
            "LEFT JOIN house h ON w.house_id=h.id AND h.deleted=0 " +
            "LEFT JOIN owner o ON w.owner_id=o.id AND o.deleted=0 " +
            "LEFT JOIN sys_user u ON w.handler_id=u.id " +
            "WHERE w.id=#{id} AND w.deleted=0</script>")
    Map<String, Object> selectDetail(@Param("id") Long id);

    @Select("SELECT * FROM work_order WHERE id=#{id} AND deleted=0")
    WorkOrder selectById(@Param("id") Long id);

    @Insert("INSERT INTO work_order(order_no,community_id,house_id,owner_id,title,type,priority,description,images,status,create_by,update_by) " +
            "VALUES(#{orderNo},#{communityId},#{houseId},#{ownerId},#{title},#{type},#{priority},#{description},#{images},#{status},#{createBy},#{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkOrder w);

    @Update("<script>UPDATE work_order SET update_by=#{updateBy}" +
            "<if test='title != null'>,title=#{title}</if>" +
            "<if test='type != null'>,type=#{type}</if>" +
            "<if test='priority != null'>,priority=#{priority}</if>" +
            "<if test='description != null'>,description=#{description}</if>" +
            "<if test='images != null'>,images=#{images}</if>" +
            "<if test='houseId != null'>,house_id=#{houseId}</if>" +
            " WHERE id=#{id} AND deleted=0</script>")
    int updateBasic(WorkOrder w);

    @Update("UPDATE work_order SET handler_id=#{handlerId}, status=2, update_by=#{operator} WHERE id=#{id} AND status=1 AND deleted=0")
    int assign(@Param("id") Long id, @Param("handlerId") Long handlerId, @Param("operator") String operator);

    @Update("UPDATE work_order SET status=3, handle_time=#{time}, update_by=#{operator} WHERE id=#{id} AND status=2 AND deleted=0")
    int accept(@Param("id") Long id, @Param("time") LocalDateTime time, @Param("operator") String operator);

    @Update("UPDATE work_order SET status=4, handle_result=#{result}, finish_time=#{time}, update_by=#{operator} WHERE id=#{id} AND status=3 AND deleted=0")
    int finish(@Param("id") Long id, @Param("result") String result, @Param("time") LocalDateTime time, @Param("operator") String operator);

    @Update("UPDATE work_order SET status=5, update_by=#{operator} WHERE id=#{id} AND status=4 AND deleted=0")
    int close(@Param("id") Long id, @Param("operator") String operator);

    @Update("<script>UPDATE work_order SET status=6, update_by=#{operator} WHERE id=#{id} AND status IN(1,2,3) AND deleted=0</script>")
    int cancel(@Param("id") Long id, @Param("operator") String operator);

    @Update("UPDATE work_order SET rating=#{rating}, rating_comment=#{comment}, update_by=#{operator} WHERE id=#{id} AND rating IS NULL AND status IN(4,5) AND deleted=0")
    int rate(@Param("id") Long id, @Param("rating") int rating, @Param("comment") String comment, @Param("operator") String operator);

    @Insert("INSERT INTO work_order_log(order_id,operator_id,operator_name,action,from_status,to_status,remark) " +
            "VALUES(#{orderId},#{operatorId},#{operatorName},#{action},#{fromStatus},#{toStatus},#{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLog(WorkOrderLog log);

    @Select("SELECT * FROM work_order_log WHERE order_id=#{orderId} ORDER BY id")
    List<WorkOrderLog> selectLogs(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) FROM work_order WHERE DATE_FORMAT(create_time,'%Y-%m')=#{period} AND deleted=0")
    long countByPeriod(@Param("period") String period);

    // 统计用
    @Select("<script>SELECT COUNT(*) FROM work_order WHERE deleted=0 AND status IN(1,2,3) " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if></script>")
    long countPending(@Param("communityId") Long communityId);

    @Select("SELECT COUNT(*) FROM work_order WHERE DATE_FORMAT(create_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m') " +
            "<if test='communityId != null'> AND community_id=#{communityId}</if> AND deleted=0")
    long countThisMonth(@Param("communityId") Long communityId);
}

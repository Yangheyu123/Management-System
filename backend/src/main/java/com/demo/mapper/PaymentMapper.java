package com.demo.mapper;

import com.demo.entity.Payment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper {

    @Insert("INSERT INTO payment(payment_no,bill_id,owner_id,amount,pay_method,pay_time,collector_id,remark) " +
            "VALUES(#{paymentNo},#{billId},#{ownerId},#{amount},#{payMethod},#{payTime},#{collectorId},#{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment p);

    @Select("<script>SELECT p.*, su.real_name AS collector_name, o.name AS owner_name " +
            "FROM payment p LEFT JOIN sys_user su ON p.collector_id=su.id " +
            "LEFT JOIN owner o ON p.owner_id=o.id AND o.deleted=0 " +
            "WHERE p.bill_id IN " +
            "<foreach collection='billIds' item='bid' open='(' separator=',' close=')'>#{bid}</foreach>" +
            " ORDER BY p.pay_time</script>")
    List<Map<String, Object>> selectByBillIds(@Param("billIds") List<Long> billIds);

    @Select("SELECT COUNT(*) FROM payment WHERE DATE_FORMAT(pay_time,'%Y%m%d%H%i%s') LIKE CONCAT(DATE_FORMAT(NOW(),'%Y%m%d'),'%')")
    long countTodaySeq();

    @Select("SELECT * FROM payment WHERE id=#{id}")
    Payment selectById(@Param("id") Long id);
}

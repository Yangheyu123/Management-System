package com.demo.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 枚举字典翻译（前后端共享取值），把数字状态码翻译成中文文案。
 */
public class Dict {

    public static final Map<Integer, String> GENDER = m("0", "未知", "1", "男", "2", "女");
    public static final Map<Integer, String> USER_TYPE = m("1", "员工", "2", "业主");
    public static final Map<Integer, String> COMMON_STATUS = m("1", "启用", "0", "禁用");
    public static final Map<Integer, String> HOUSE_STATUS = m("1", "空置", "2", "已售", "3", "已入住", "4", "装修中");
    public static final Map<Integer, String> OWNER_STATUS = m("1", "在住", "2", "已搬离");
    public static final Map<Integer, String> WO_TYPE = m("1", "水电", "2", "土建", "3", "门窗", "4", "公共设施", "5", "其他");
    public static final Map<Integer, String> WO_PRIORITY = m("1", "低", "2", "中", "3", "高", "4", "紧急");
    public static final Map<Integer, String> WO_STATUS = m("1", "待派单", "2", "已派单", "3", "处理中", "4", "已完成", "5", "已关闭", "6", "已撤销");
    public static final Map<Integer, String> FEE_TYPE = m("1", "物业费", "2", "水费", "3", "电费", "4", "车位费", "5", "其他");
    public static final Map<Integer, String> BILL_STATUS = m("1", "未缴", "2", "部分缴纳", "3", "已缴清", "4", "已作废");
    public static final Map<Integer, String> PAY_METHOD = m("1", "现金", "2", "微信", "3", "支付宝", "4", "银行转账", "5", "POS");
    public static final Map<Integer, String> BILLING_CYCLE = m("1", "月", "2", "季", "3", "年", "4", "一次性");
    public static final Map<Integer, String> AREA_TYPE = m("1", "地上", "2", "地下");
    public static final Map<Integer, String> USE_TYPE = m("1", "长租", "2", "出售", "3", "临时");
    public static final Map<Integer, String> PARKING_STATUS = m("1", "空闲", "2", "使用中", "3", "已售", "4", "已禁用");
    public static final Map<Integer, String> EQUIP_CATEGORY = m("1", "通闸设备", "2", "消防设备", "3", "安防设备");
    public static final Map<Integer, String> EQUIP_STATUS = m("1", "正常", "2", "故障", "3", "维修中", "4", "报废");
    public static final Map<Integer, String> CHECK_RESULT = m("1", "正常", "2", "异常");
    public static final Map<Integer, String> ONLINE = m("1", "在线", "0", "离线");

    public static String name(Map<Integer, String> dict, Integer key) {
        if (key == null) return null;
        return dict.getOrDefault(key, String.valueOf(key));
    }

    /** 身份证脱敏：保留前6后4 */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        String mid = idCard.substring(6, idCard.length() - 4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mid.length(); i++) sb.append('*');
        return idCard.substring(0, 6) + sb + idCard.substring(idCard.length() - 4);
    }

    private static Map<Integer, String> m(Object... kv) {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(Integer.parseInt(String.valueOf(kv[i])), String.valueOf(kv[i + 1]));
        }
        return map;
    }
}

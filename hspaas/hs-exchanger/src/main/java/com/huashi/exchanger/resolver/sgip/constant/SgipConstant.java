package com.huashi.exchanger.resolver.sgip.constant;

import java.util.HashMap;
import java.util.Map;

public final class SgipConstant {

	// 计费类型 int
	public static int FEE_TYPE = 1;
	// 该条短消息的收费值 stirng
	public static String FEE_VALUE = "000";
	// // 代收费标志0：应收1：实收 int
	public static int FEE_CODE = 0;
	
	// GSM协议类型
	public static int TP_PID = 0;
	// GSM协议类型
	public static int TP_UDHI = 0;
	
	public static final int MSG_FMT_GBK = 15;
	public static final int MSG_FMT_UCS2 = 8;
    
    /**
     * GBK编码单条最大字节数
     */
	public static final int MAX_MESSAGE_GBK_LENGTH = 140;
    /**
     * UCS2编码单挑最大字节数
     */
    public static final int MAX_MESSAGE_UCS2_LENGTH = 140;
	
    // 公共状态回执成功码
 	public static final String COMMON_MT_STATUS_SUCCESS_CODE = "DELIVRD";

 	// SGIP 重连计时(毫秒)
 	public volatile static Map<Integer, Long> SGIP_RECONNECT_TIMEMILLS = new HashMap<>();
}

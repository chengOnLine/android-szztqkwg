package com.jicengzhili_gm.AMapLocation.base;
public interface ErrorCode {

    public static final Integer SUCCESS=0;// 成功
    public static final Integer CANCLE=1000;// 取消
    public static final Integer UNKNOW=1001;// 未知错误
    public static final Integer E_NO_SDCARD=1002;// 没有内存卡
    public static final Integer UNKNOW_NUM=1003;// 解析车牌号码失败
    public static final Integer FAIL=1004;// 识别失败
}

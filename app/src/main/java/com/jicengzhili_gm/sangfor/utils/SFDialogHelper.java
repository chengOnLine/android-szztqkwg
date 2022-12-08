package com.jicengzhili_gm.sangfor.utils;

import com.sangfor.sdk.base.SFAuthType;

public class SFDialogHelper {
    /**
     * 对话框标题
     *
     * @param authType 认证类型
     * @return 对话框标题
     */
    public static String getDialogTitle(SFAuthType authType) {
        switch (authType) {
            case AUTH_TYPE_PASSWORD:
                return "密码认证";
            case AUTH_TYPE_CERTIFICATE:
                return "证书认证";
            case AUTH_TYPE_SMS:
                return "短信认证";
            case AUTH_TYPE_RADIUS:
                return "挑战认证";
            case AUTH_TYPE_TOKEN:
                return "令牌认证";
            case AUTH_TYPE_RAND:
                return "图形校验码";
            case AUTH_TYPE_RENEW_PASSWORD:
                return "修改密码";
            default:
                return "";
        }
    }

    public static int getAuthDialogViewId(SFAuthType authType) {
        switch (authType) {
            case AUTH_TYPE_PASSWORD:
                return com.sangfor.sangforsdk.R.layout.dialog_pwd;
            case AUTH_TYPE_CERTIFICATE:
                return com.sangfor.sangforsdk.R.layout.dialog_certificate;
            case AUTH_TYPE_SMS:
                return com.sangfor.sangforsdk.R.layout.dialog_sms;
            case AUTH_TYPE_RADIUS:
                return com.sangfor.sangforsdk.R.layout.dialog_challenge;
            case AUTH_TYPE_TOKEN:
                return com.sangfor.sangforsdk.R.layout.dialog_token;
            case AUTH_TYPE_RAND:
                return com.sangfor.sangforsdk.R.layout.dialog_graph_check;
            case AUTH_TYPE_RENEW_PASSWORD:
                return com.sangfor.sangforsdk.R.layout.dialog_force_update_pwd;
            default:
                return -1;
        }
    }
}

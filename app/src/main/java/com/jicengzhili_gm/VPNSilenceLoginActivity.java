package com.jicengzhili_gm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sangfor.sdk.SFMobileSecuritySDK;
import com.sangfor.sdk.base.SFAuthResultListener;
import com.sangfor.sdk.base.SFAuthType;
import com.sangfor.sdk.base.SFBaseMessage;
import com.sangfor.sdk.base.SFChangePswMessage;
import com.sangfor.sdk.base.SFConstants;
import com.sangfor.sdk.utils.SFLogN;
import com.jicengzhili_gm.sangfor.utils.Constants;
import com.jicengzhili_gm.sangfor.utils.PermissionUtil;
import com.jicengzhili_gm.sangfor.utils.SFDialogHelper;

import java.util.HashMap;
import java.util.Map;

public class VPNSilenceLoginActivity extends Activity implements SFAuthResultListener,View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final int PERMISSON_REQUEST_CODE = 32;       //动态权限请求码
    private static final int CERTFILE_REQUEST_CODE = 33;        //当证书认证是主认证时获取证书路径请求码
    private static final int DIALOG_CERTFILE_REQUEST_CODE = 34; //当证书认证是辅助认证时获取证书路径请求码
    private static final int DEFAULT_SMS_COUN_TDOWN_TIME = 30;  //短信验证码默认倒计时时间, 以秒为单位
    //VPN设备地址
    private String mVpnAddress = "https://61.144.225.144:4439";
    //用户名密码认证, 测试用账号（需要替换为客户VPN设备管理员提供的账号)
    private String mUserName = "gmzlt";
    private String mUserPassword = "gmzlt";
    //证书认证, 测试用账号（需要替换为客户VPN设备管理员提供的账号)
    private String mCertPath = "/storage/emulated/0/zhuyongsm2.p12";
    private String mCertPassword = "";
    //View
    private AlertDialog mAuthDialog = null;
    private EditText mVpnAddressEditText = null;
    private EditText mUserNameEditView = null;
    private EditText mUserPasswordEditView = null;
    private EditText mCertPathEditView = null;
    private EditText mCertPasswordEditView = null;
    private ImageView mCertFileSelectView = null;    //证书路径选择按钮
    private RadioGroup mAuthMethodRadioGroup = null;
    private Button mLoginButton = null;
    private View mRandCodeDialogView = null;    //图形校验码对话框视图
    private View mSmsCodeDialogView = null;     //短信验证码对话框视图
    private EditText mCertPathDialogEditView = null;//证书路径输入框
    private ProgressDialog mProgressDialog = null;  // 对话框对象
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpnsilencelogin);

        SFMobileSecuritySDK.getInstance().setAuthResultListener(this);

        showWaitingDialog(false);
        SFMobileSecuritySDK.getInstance().startPasswordAuth(mVpnAddress, mUserName, mUserPassword);

        Constants.activityList.add(this);
    }

    @Override
    public void onClick(View v) {
        //认证完成，跳转认证成功界面，可以开始访问资源
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(VPNSilenceLoginActivity.this, MainActivity.class));
            }
        });
    }

    //显示“请稍候...”提示框
    public void showWaitingDialog(final boolean isCancelable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                    mProgressDialog = new ProgressDialog(VPNSilenceLoginActivity.this);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setTitle("");
                    mProgressDialog.setMessage(VPNSilenceLoginActivity.this.getString(R.string.waiting));
                    mProgressDialog.setCancelable(isCancelable);
                    mProgressDialog.show();
                }
            }
        });
    }

    //取消“请稍候...”提示框
    public void dismissWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        });
    }

    //显示错误提示对话框
    public void showErrorMessage(final String errMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog messageDialog = new AlertDialog.Builder(VPNSilenceLoginActivity.this)
                        .setTitle(VPNSilenceLoginActivity.this.getString(R.string.info))
                        .setMessage(errMsg)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Process.killProcess(Process.myPid());
                                System.exit(1);
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                showWaitingDialog(false);
                                SFMobileSecuritySDK.getInstance().startPasswordAuth(mVpnAddress, mUserName, mUserPassword);
                            }
                        })
                        .create();

                messageDialog.show();
            }
        });
    }

    //封装Toast
    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VPNSilenceLoginActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 认证成功
     *
     * @param message  认证成功信息
     */
    @Override
    public void onAuthSuccess(final SFBaseMessage message) {
        SFLogN.info(TAG, "auth success");
        dismissWaitingDialog();

        //认证完成，跳转认证成功界面，可以开始访问资源
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(getString(R.string.auth_success));
                startActivity(new Intent(VPNSilenceLoginActivity.this, MainActivity.class));
            }
        });
    }

    /**
     * 认证失败
     *
     * @param authType 当前认证类型
     * @param message  认证失败信息
     */
    @Override
    public void onAuthFailed(final SFAuthType authType, final SFBaseMessage message)  {
        SFLogN.error2(TAG, "auth failed", "errMsg: " + message.mErrStr + ",authType: " + authType.name());
        dismissWaitingDialog();

        //认证失败
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (authType.equals(SFAuthType.AUTH_TYPE_TICKET)) {
                    showToast(getString(R.string.ticket_auth_failed));
                    return;
                }

                showErrorMessage("您的VPN链接中断或者网络出现异常，请重新链接VPN或退出系统重新登录。");
            }
        });
    }

    /**
     * 主认证成功，但需要辅助认证（下一步认证）
     *
     * @param nextAuthType  下一步认证类型
     * @param message       下一步认证信息
     */
    @Override
    public void onAuthProgress(SFAuthType nextAuthType, SFBaseMessage message) {
        SFLogN.info(TAG, "need next auth, authType: " + nextAuthType.name());
        dismissWaitingDialog();
        //显示下一步认证UI界面
        showAuthDialog(nextAuthType, message);
    }

    /**
     * 关闭对话框
     */
    private void closeDialog() {
//        if (mAuthDialog != null && mAuthDialog.isShowing()) {
//            mAuthDialog.dismiss();
//            mAuthDialog = null;
//        }
    }


    /**
     * 显示下一步认证UI界面
     */
    public void showAuthDialog(final SFAuthType authType, final SFBaseMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDialog();
                //获取认证类型对应的标题
                String dialogTitle = SFDialogHelper.getDialogTitle(authType);
                if (TextUtils.isEmpty(dialogTitle)) {
                    showErrorMessage("暂不支持此种认证类型(" + authType.toString() + ")");
                    return;
                }
                //获取认证类型对应的layout布局
                int viewLayoutId = SFDialogHelper.getAuthDialogViewId(authType);
                //创建认证类型对应的对话框显示视图
                final View dialogView = createDialogView(authType, viewLayoutId, message);
                mAuthDialog = new AlertDialog.Builder(VPNSilenceLoginActivity.this)
                        .setTitle(dialogTitle)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.str_commit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeDialog();
                                //构建辅助认证类型所需参数
                                Map<String, String> authParams = new HashMap<String, String>();
                                String errorMsg = buildAuthParams(authType, dialogView, authParams);
                                //检查参数构建是否成功
                                if (TextUtils.isEmpty(errorMsg)) {
                                    showWaitingDialog(false);
                                    //开始辅助认证
                                    SFMobileSecuritySDK.getInstance().doSecondaryAuth(authType, authParams);
                                } else {
                                    //参数构建失败，进行提示
                                    showErrorMessage(errorMsg);
                                }
                                return;
                            }
                        })
                        .setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //放弃继续认证
                                closeDialog();
                                return;
                            }
                        })
                        .create();
                //显示认证对话框
                mAuthDialog.show();
            }
        });
    }

    /**
     * 调用系统自带的文件管理器选择证书文件
     */
    private void selectCertFile(int requestCode) {
        if (PermissionUtil.isNeedRequestSDCardPermission(this)) {
            PermissionUtil.requestSDCardPermissions(this, PERMISSON_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 创建认证对话框中间显示的视图
     * @param sfAuthType 认证类型
     * @param layoutId 要加载的视图的布局ID
     * @param message  认证附加信息
     * @return  认证对话框视图
     */
    private View createDialogView(SFAuthType sfAuthType, int layoutId, final SFBaseMessage message) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layoutId, null);
        switch (sfAuthType) {
            case AUTH_TYPE_CERTIFICATE: {
                mCertPathDialogEditView = dialogView.findViewById(R.id.et_certPath);
                TextView tvCertPath = dialogView.findViewById(R.id.tv_certPath);
                tvCertPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //辅助认证时获取证书路径
                        selectCertFile(DIALOG_CERTFILE_REQUEST_CODE);
                    }
                });

                break;
            }
            case AUTH_TYPE_RENEW_PASSWORD: {
                TextView tvPolicy = (TextView) dialogView.findViewById(R.id.tv_policy);
                String policy = "";
                //获取密码策略
                if (message instanceof SFChangePswMessage) {
                    policy = ((SFChangePswMessage) message).policyMsg;
                }
                if (TextUtils.isEmpty(policy)) {
                    tvPolicy.setText(R.string.str_no_policy);
                } else {
                    tvPolicy.setText(getString(R.string.str_policy_hint) + "\n" + policy);
                }
                break;
            }
            default:
                break;
        }

        return dialogView;
    }

    /**
     * 构建辅助认证所需参数
     * @param sfAuthType 认证类型
     * @param dialogView 认证视图
     * @param authParams 保存认证参数
     * @return 参数错误信息
     */
    private String buildAuthParams(SFAuthType sfAuthType, View dialogView, Map<String, String> authParams) {
        String errorMsg = "";
        switch (sfAuthType) {
            case AUTH_TYPE_PASSWORD: {

                authParams.put(SFConstants.AUTH_KEY_USERNAME, mUserName);
                authParams.put(SFConstants.AUTH_KEY_PASSWORD, mUserPassword);
                break;
            }
            default:
                errorMsg = "暂不支持的认证类型";
                break;
        }

        return errorMsg;
    }
}

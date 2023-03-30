package com.jicengzhili_gm.AMapLocation.widget;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.R;

/***
 * @author Alen
 * @Package com.jwd.shenzhentong.widget
 * @Title: MessageDialog.java
 * @Description 自定义对话框
 * @date 2017-3-2
 * @version 1.0
 *
 */
public class MessageDialog extends Dialog implements
        View.OnClickListener {
    private TextView content,title;
    private TextView cancel,sure;
    private MessageDialogCallBack mCallBack;

    private EditText txtQiantui;
    private LinearLayout DivQiantui;

    public MessageDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_message);
//        Window mWindow = getWindow();
//        WindowManager.LayoutParams layoutParams = mWindow.getAttributes();
//        layoutParams.width = (int) (Constant.weight*0.88);

        initView();
    }

    private void initView() {
        // TODO Auto-generated method stub
        title = (TextView) findViewById(R.id.tip_content_title);
        content = (TextView) findViewById(R.id.tip_content_tv);
        cancel = (TextView) findViewById(R.id.button_cancel);
        cancel.setOnClickListener(this);
        sure = (TextView) findViewById(R.id.button_sure);
        sure.setOnClickListener(this);

        txtQiantui= (EditText)findViewById(R.id.txtQiantui);
        DivQiantui= (LinearLayout)findViewById(R.id.DivQiantui);

        Log.e("alen", "初始化dialog2");
    }

    public void showDialog(String text,MessageDialogCallBack mCallBack) {
        content.setText(text);
        if(text.indexOf("签退")>0){
            DivQiantui.setVisibility(View.VISIBLE);
        }else{
            DivQiantui.setVisibility(View.GONE);
        }

        this.mCallBack = mCallBack;
        this.show();
    }

    public void showDialog(String tit,String msg,MessageDialogCallBack mCallBack) {
        title.setVisibility(View.VISIBLE);
        title.setText(tit);
        content.setText(msg);
        this.mCallBack = mCallBack;
        this.show();
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        int i = arg0.getId();
        if (i == R.id.button_cancel) {
            dismiss();
            mCallBack.cancel();

        } else if (i == R.id.button_sure) {

            String txtDesc=txtQiantui.getText().toString();
            Log.e("alen", "签退备注1" + txtDesc);
            Constant.txtQiantuiDesc=txtDesc;

            dismiss();
            mCallBack.sure();

        } else {
        }
    }

    public interface MessageDialogCallBack {
        void cancel();

        void sure();
    }
}

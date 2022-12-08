package com.jicengzhili_gm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.react.ReactActivity;

import org.devio.rn.splashscreen.SplashScreen;

public class MainActivity extends ReactActivity {


  //如果没有这个方法的话可以把整一个方法都复制进去
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // SplashScreen.show(this);  // <--添加这一句

    super.onCreate(savedInstanceState);
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "JiCengZhiLi_GM";
  }
}

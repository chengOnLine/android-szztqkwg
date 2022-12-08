package com.jicengzhili_gm.AMapLocation.mapView;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.TextureMapView;

public class LinesMapView extends TextureMapView {

    public LinesMapView(Context context) {

        super(context);

        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);

        super.onCreate(null);
    }
}

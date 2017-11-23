package com.gaodemap;


import android.os.Bundle;
import android.widget.Toast;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DriveWayView;
import com.uidemo.R;

public class CustomDriveWayViewActivity extends BaseActivity implements AMapNaviListener {
    private DriveWayView mDriveWayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //为了能最快的看到效果
        mStartLatlng = new NaviLatLng(39.92458861111111, 116.43543861111111);
        setContentView(R.layout.activity_custom_drive_way_view);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);

        //设置布局完全不可见
        AMapNaviViewOptions options = mAMapNaviView.getViewOptions();
        options.setLayoutVisible(false);
        mAMapNaviView.setViewOptions(options);

        mDriveWayView = (DriveWayView) findViewById(R.id.myDriveWayView);
        mAMapNaviView.setLazyDriveWayView(mDriveWayView);

    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);

    }

    @Override
    public void onCalculateRouteSuccess(int[] ids) {
        super.onCalculateRouteSuccess(ids);
        mAMapNavi.startNavi(NaviType.EMULATOR);
    }
    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        StringBuffer sb = new StringBuffer();
        sb.append("共"+laneInfos.length+"车道");
        for (int i = 0; i < laneInfos.length; i++) {
            AMapLaneInfo info = laneInfos[i];
            //当前车道可以选择的动作
            char background = info.getLaneTypeIdHexString().charAt(0);
            //当前用户要执行的动作
            char Recommend = info.getLaneTypeIdHexString().charAt(1);
            //根据文档中每个动作对应的枚举类型，显示对应的图片
            try {
                sb.append("，第"+(i+1)+"车道为"+array[getHex(background)]+"，该车道可执行动作为"+actions[getHex(Recommend)]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this, sb.toString(), 3000).show();
    }
    String array [] = {
            "直行车道"
            ,"左转车道"
            ,"左转或直行车道"
            ,"右转车道"
            ,"右转或这行车道"
            ,"左掉头车道"
            ,"左转或者右转车道"
            ," 左转或右转或直行车道"
            ,"右转掉头车道"
            ,"直行或左转掉头车道"
            ,"直行或右转掉头车道"
            ,"左转或左掉头车道"
            ,"右转或右掉头车道"
            ,"无"
            ,"无"
            ,"不可以选择该车道"
    };

    String actions[] = {
            "直行"
            ,"左转"
            ,"左转或直行"
            ,"右转"
            ,"右转或这行"
            ,"左掉头"
            ,"左转或者右转"
            ," 左转或右转或直行"
            ,"右转掉头"
            ,"直行或左转掉头"
            ,"直行或右转掉头"
            ,"左转或左掉头"
            ,"右转或右掉头"
            ,"无"
            ,"无"
            ,"不可以选择"
    };
    @Override
    public void hideLaneInfo() {
    }

    //计算16进制对应的数值
    public static int getHex(char ch) throws Exception{
        if ( ch>='0' && ch<='9' )
            return (int)(ch-'0');
        if ( ch>='a' && ch<='f' )
            return (int)(ch-'a'+10);
        if ( ch>='A' && ch<='F' )
            return (int)(ch-'A'+10);
        throw new Exception("error param");
    }
}


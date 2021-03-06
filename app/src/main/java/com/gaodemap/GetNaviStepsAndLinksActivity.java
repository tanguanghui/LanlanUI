package com.gaodemap;

import android.os.Bundle;
import android.util.Log;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapNaviGuide;
import com.uidemo.R;

import java.util.List;


public class GetNaviStepsAndLinksActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_navi);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
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
        //概览
        List<AMapNaviGuide> guides = mAMapNavi.getNaviGuideList();
        for (int i = 0; i < guides.size(); i++) {
            //guide step相生相惜，指的是大导航段
            AMapNaviGuide guide = guides.get(i);
            Log.d("wlx", "AMapNaviGuide 路线经纬度:" + guide.getCoord() + "");
            Log.d("wlx", "AMapNaviGuide 路线名:" + guide.getName() + "");
            Log.d("wlx", "AMapNaviGuide 路线长:" + guide.getLength() + "m");
            Log.d("wlx", "AMapNaviGuide 路线耗时:" + guide.getTime() + "s");
            Log.d("wlx", "AMapNaviGuide 路线IconType" + guide.getIconType() + "");
            Log.d("wlx", "AMapNaviGuide 路段step开始索引" + guide.getStartSegId() + "");
            Log.d("wlx", "AMapNaviGuide 路段step数量" + guide.getSegCount() + "");
        }
    }
}

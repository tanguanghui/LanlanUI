package com.gaodemap.custom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.gaodemap.IndexAdapter;
import com.gaodemap.util.AmapTTSController;
import com.uidemo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentActivity extends Activity implements INaviInfoCallback {
    private String[] examples = new String[]{ "定位导航","定位导航","定位导航","定位导航"};
    private AmapTTSController amapTTSController;
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position == 0) {
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), new AmapNaviParams(null, null, null, AmapNaviType.DRIVER), ComponentActivity.this);//设置为当前位置
            } else if (position == 1) {

            } else if (position == 2) {
                    List<Poi> poiList = new ArrayList();
                    poiList.add(null);
                    poiList.add(null);
                    poiList.add(null);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), new AmapNaviParams(new Poi("", null, ""), poiList, new Poi("", null, ""), AmapNaviType.DRIVER), ComponentActivity.this);
            } else if (position == 3) {
                Poi start = new Poi("", null, "");//起点

                //<editor-fold desc="途径点">
                List<Poi> poiList = new ArrayList();
                poiList.add(null);
                poiList.add(null);
                poiList.add(null);
                //</editor-fold>

                Poi end = new Poi("", null, "");//终点
                AmapNaviParams amapNaviParams = new AmapNaviParams(start, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI);
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), amapNaviParams, ComponentActivity.this);
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), new AmapNaviParams(null, null, null, AmapNaviType.DRIVER), ComponentActivity.this);



        initView();
        // SpeechUtils.getInstance(this).speakText();系统自带的语音播报
        amapTTSController = AmapTTSController.getInstance(getApplicationContext());
        amapTTSController.init();

    }

    private void initView() {
        ListView listView = (ListView) findViewById(R.id.list);
//        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, examples));
        IndexAdapter adapter = new IndexAdapter(this, Arrays.asList(examples));
        listView.setAdapter(adapter);

        setTitle("导航" + AMapNavi.getVersion());
        listView.setOnItemClickListener(mItemClickListener);
    }

    @Override
    public void onInitNaviFailure() {

    }
    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }
    @Override
    public void onArriveDestination(boolean b) {

    }
    @Override
    public void onStartNavi(int i) {

    }
    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }
    @Override
    public void onCalculateRouteFailure(int i) {

    }
    @Override
    public void onGetNavigationText(String s) {
        amapTTSController.onGetNavigationText(s);
    }
    @Override
    public void onStopSpeaking() {
        amapTTSController.stopSpeaking();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        amapTTSController.destroy();
    }
}

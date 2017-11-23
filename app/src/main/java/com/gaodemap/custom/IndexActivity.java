package com.gaodemap.custom;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.gaodemap.DriverListActivity;
import com.gaodemap.EmulatorActivity;
import com.gaodemap.IndexAdapter;
import com.gaodemap.RideRouteCalculateActivity;
import com.gaodemap.WalkRouteCalculateActivity;
import com.gaodemap.util.CheckPermissionsActivity;
import com.uidemo.R;
//import com.amap.navi.demo.R;
//import com.amap.navi.demo.activity.DriverListActivity;
//import com.amap.navi.demo.activity.EmulatorActivity;
//import com.amap.navi.demo.activity.IndexAdapter;
//import com.amap.navi.demo.activity.RestRouteShowActivity;
//import com.amap.navi.demo.activity.RideRouteCalculateActivity;
//import com.amap.navi.demo.activity.WalkRouteCalculateActivity;
//import com.amap.navi.demo.util.CheckPermissionsActivity;
import java.util.Arrays;

/**
 * Created by shixin on 16/8/23.
 * bug反馈QQ:1438734562
 */
public class IndexActivity extends CheckPermissionsActivity {

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {//导航(新)
              startActivity(new Intent(IndexActivity.this, ComponentActivity.class));
//                AmapNaviPage.getInstance().showRouteActivity(getBaseContext(), new AmapNaviParams(null, null, null, AmapNaviType.DRIVER), null);
            } else if (position == 1) {//驾车路线规划
                startActivity(new Intent(IndexActivity.this, DriverListActivity.class));
//                startActivity(new Intent(IndexActivity.this, RestRouteShowActivity.class));
            } else if (position == 2) {//步行路线规划
                startActivity(new Intent(IndexActivity.this, WalkRouteCalculateActivity.class));
            } else if (position == 3) {//骑行路线规划
                startActivity(new Intent(IndexActivity.this, RideRouteCalculateActivity.class));
            } else if (position == 5) {//模拟导航
                startActivity(new Intent(IndexActivity.this, EmulatorActivity.class));
           }
//            else if (position == 4) {//实时导航
//                startActivity(new Intent(IndexActivity.this, GPSNaviActivity.class));
//            }
//          else if (position == 6) {//只能巡航
//                startActivity(new Intent(IndexActivity.this, IntelligentBroadcastActivity.class));
//            } else if (position == 7) {//使用设备外GPS数据导航
//                startActivity(new Intent(IndexActivity.this, UseExtraGpsDataActivity.class));
//            } else if (position == 8) {//导航UI在自定义
//                startActivity(new Intent(IndexActivity.this, CustomUiActivity.class));
//            } else if (position == 9) {//HUD导航
//                startActivity(new Intent(IndexActivity.this, HudDisplayActivity.class));
//            } else if (position == 10) {//展示导航路径详情
//                startActivity(new Intent(IndexActivity.this, GetNaviStepsAndLinksActivity.class));
//            }
        }
    };
    private String[] examples = new String[]
            {"导航<font color='red'>(新)<font>", "驾车路线规划", "步行路线规划", "骑行路线规划"};
//,撤下这几个：     "实时导航", "模拟导航", "智能巡航", "传入GPS数据导航","导航UI自定义", "HUD导航", "展示导航路径详情"
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        new AmapNaviParams(null, null, null, AmapNaviType.DRIVER);
        initView();
    }
    private void initView() {
        int w=R.color.white;
        ListView listView = (ListView) findViewById(R.id.list);
        IndexAdapter adapter = new IndexAdapter(this, Arrays.asList(examples));
        listView.setAdapter(adapter);
        setTitle("导航SDK " + AMapNavi.getVersion());
        listView.setOnItemClickListener(mItemClickListener);
    }
    /**
     * 返回键处理事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
//            System.exit(0);// 退出程序
        }
        return super.onKeyDown(keyCode, event);
    }
}

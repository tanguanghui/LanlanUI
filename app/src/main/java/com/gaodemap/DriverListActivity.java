package com.gaodemap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.uidemo.R;

import java.util.Arrays;


public class DriverListActivity extends Activity {

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {//驾车路线规划（单路径）
                startActivity(new Intent(DriverListActivity.this, SingleRouteCalculateActivity.class));
            } else if (position == 1) {//驾车路线规划（多路径）
                startActivity(new Intent(DriverListActivity.this, RestRouteShowActivity.class));
            }

        }
    };
    private String[] examples = new String[]

            {"驾车路线规划(单路径)", "驾车路线规划(多路径)"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        initView();

    }

    private void initView() {
        ListView listView = (ListView) findViewById(R.id.list);
        IndexAdapter adapter = new IndexAdapter(this, Arrays.asList(examples));
        listView.setAdapter(adapter);
//        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, examples));
        setTitle("路线规划");
        listView.setOnItemClickListener(mItemClickListener);
    }

}

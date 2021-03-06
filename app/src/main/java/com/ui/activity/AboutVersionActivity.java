package com.ui.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.BaseActivity;
import com.uidemo.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Eren on 2017/6/19.
 * <p/>
 * 关于版本
 */
public class AboutVersionActivity extends BaseActivity {
    private TextView currentCode;
    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private List<String> mDatas1;
    private List<String> mDatas2;
    private HomeAdapter mAdapter;
    @BindView(R.id.aboutVicmob_back)
    ImageView mAboutVicmobBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_about_version;
    }

    @Override
    public void findView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initView() {
        currentCode = (TextView) findViewById(R.id.version_text);
        currentCode.setText("版本号：" + getVersion());
        initDate();
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    protected void initDate() {
        mDatas = new ArrayList<>();
        mDatas1 = new ArrayList<>();
        mDatas2 = new ArrayList<>();
        String versioncode[] = {getResources().getString(R.string.version_log1)};
        String versiondate[] = {getResources().getString(R.string.version_log1_1)};
        String versiondata[] = {getResources().getString(R.string.version_log1_2)};
        for (int i = (versioncode.length - 1); i >= 0; i--) {
            String dd = "version_log" + i;
            mDatas.add(versioncode[i]);
            mDatas1.add(versiondate[i]);
            mDatas2.add(versiondata[i]);
        }
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    public String getVersion() {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    AboutVersionActivity.this).inflate(R.layout.recycle_item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv.setText(mDatas.get(position));
            holder.tv1.setText(mDatas1.get(position));
            holder.tv2.setText(mDatas2.get(position));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv;
            TextView tv1;
            TextView tv2;

            public MyViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.version1);
                tv1 = (TextView) view.findViewById(R.id.version2);
                tv2 = (TextView) view.findViewById(R.id.version3);
            }
        }
    }

    @OnClick(R.id.aboutVicmob_back)
    public void onViewClicked() {
        finish();
    }
}

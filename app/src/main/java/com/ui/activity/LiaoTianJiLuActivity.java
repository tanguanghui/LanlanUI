package com.ui.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.BaseActivity;
import com.bumptech.glide.Glide;
import com.entity.LiaoTianJiLuBean;
import com.entity.SelectBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ui.Adapter.ChatRecordListAdapter;
import com.ui.Adapter.MyLiaoTianJiLuAdpter;
import com.uidemo.R;
import com.utils.Urlutils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LiaoTianJiLuActivity extends BaseActivity {
    @BindView(R.id.iv_fanhui_liaotianjilu)
    ImageView ivFanhuiLiaotianjilu;
    @BindView(R.id.tv_liaotianjilu)
    TextView tvLiaotianjilu;
    @BindView(R.id.key_et_put)
    EditText keyEtPut;
    @BindView(R.id.key_serch_btn)
    Button keySerchBtn;
    @BindView(R.id.devices_load_img)
    ImageView devicesLoadImg;
    @BindView(R.id.devices_load)
    RelativeLayout devicesLoad;
    @BindView(R.id.devices_noneww_img)
    ImageView devicesNonewwImg;
    @BindView(R.id.rl_liaotianjilu)
    RecyclerView rlLiaotianjilu;
    @BindView(R.id.devices_noneww)
    RelativeLayout devicesNoneww;
    @BindView(R.id.rl_serch)
    RelativeLayout rlSerch;
    @BindView(R.id.tv_shijian)
    TextView tvShijian;


    private List<LiaoTianJiLuBean> listData = new ArrayList<>();
    private List<LiaoTianJiLuBean> liaotianjiluList = new ArrayList<LiaoTianJiLuBean>();
    private ChatRecordListAdapter adapter;
    //动画
    private AnimationDrawable loadingDrawable;
    private SelectBean selectBean;
    private int lastVisibleItem;

    private String weixinhao = "";
    private String weixinhaoyou = "";
    private static final int EXMSG = 1;
    private static final int LOADSUCCESS = 2;
    private static final int LOADFAIL = 3;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EXMSG:
                    devicesLoad.setVisibility(View.VISIBLE);
                    devicesNoneww.setVisibility(View.GONE);
                    loadingDrawable.start();
                    break;
                case LOADSUCCESS:
//                    mSwipeRefreshLayout.setRefreshing(false);
                    devicesLoad.setVisibility(View.GONE);
                    break;
                case LOADFAIL:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            mSwipeRefreshLayout.setRefreshing(false);
                            devicesLoad.setVisibility(View.GONE);
                            devicesNoneww.setVisibility(View.VISIBLE);
                        }
                    }, 4000);
                    break;
            }
        }
    };
    private String keyword = "";
    private String date = "";
    private int start = 0;
    private int numOfpages = 15;
    private boolean isLoad = true;
    private int loadIndex = 0;

    @Override
    public int getLayoutId() {
        return R.layout.activity_liao_tian_ji_lu;
    }

    @Override
    public void findView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initView() {
        //显示加载的动画
        loadingDrawable = (AnimationDrawable) devicesLoadImg.getBackground();
        rlLiaotianjilu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!LiaoTianJiLuActivity.this.isDestroyed()) {
                        Glide.with(LiaoTianJiLuActivity.this).resumeRequests();
                    }
                } else {
                    if (!LiaoTianJiLuActivity.this.isDestroyed()) {
                        Glide.with(LiaoTianJiLuActivity.this).pauseRequests();
                    }

                }
            }
        });



    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        Intent intent1 = getIntent();
        weixinhao = intent1.getStringExtra("weixinhao");
        weixinhaoyou = intent1.getStringExtra("weixinhaoyou");
        String weixinhaoyouname = intent1.getStringExtra("weixinhaoyouname");
        tvLiaotianjilu.setText(weixinhao + "与" + weixinhaoyouname + "的聊天");
        final LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        rlLiaotianjilu.setLayoutManager(manager);

        adapter = new ChatRecordListAdapter(LiaoTianJiLuActivity.this, listData);
        rlLiaotianjilu.setAdapter(adapter);
        adapter.setLoad(false,0);

        selectBean = new SelectBean(keyword, date, start, numOfpages);
        selectLiaoTianNeiRongByBean(weixinhao, weixinhaoyou, selectBean);

        rlLiaotianjilu.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //RecyclerView没有拖动而且已经到达了最后一个item，执行自动加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    if (loadIndex > 1) {
                        isLoad = false;
                        adapter.setLoad(true, 2);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                start += numOfpages;
                                Log.i("zw1102", start + "");
                                selectBean = new SelectBean(keyword, date, start, numOfpages);
                                selectLiaoTianNeiRongByBean(weixinhao, weixinhaoyou, selectBean);
                                adapter.notifyDataSetChanged();
                            }
                        }, 2500);
                    } else {
                        adapter.setLoad(false, 0);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = manager.findLastVisibleItemPosition();
            }

        });
    }

    @OnClick({R.id.iv_fanhui_liaotianjilu, R.id.devices_noneww_img, R.id.key_serch_btn, R.id.tv_shijian})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_fanhui_liaotianjilu:
                finish();
                break;
            case R.id.devices_noneww_img:
                start = 0;
                loadIndex = 0;
                adapter.setLoad(false, 0);
                selectBean = new SelectBean(keyword, date, start, numOfpages);
                selectLiaoTianNeiRongByBean(weixinhao, weixinhaoyou, selectBean);
                break;
            case R.id.key_serch_btn:
                listData.clear();
                start = 0;
                loadIndex = 0;
                adapter.setLoad(false, 0);
                keyword = keyEtPut.getText().toString().trim();
                selectBean = new SelectBean(keyword, date, start, numOfpages);
                selectLiaoTianNeiRongByBean(weixinhao, weixinhaoyou, selectBean);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(keyEtPut.getWindowToken(), 0);

                break;

            case R.id.tv_shijian:
                showDatePickDlg();
                break;
        }
    }

    private void selectLiaoTianNeiRongByBean(String weixinhao, String weixinhaoyou, SelectBean selectBean) {
        if (loadIndex == 0) {
            mHandler.sendEmptyMessage(EXMSG);
        }
        //String url = Urlutils.selectLiaoTianJiLuUrl;
        String keyword = selectBean.getKeyword();
        String date = selectBean.getDate();
        Log.i("zw1102", date + "");
        int start = selectBean.getStart();
        int numOfpages = selectBean.getNumOfpages();
        String url = Urlutils.selectLiaoTianJiLuUrl
                + "?weixinhao=" + weixinhao
                + "&weixinhaoyou=" + weixinhaoyou
                + "&keyword=" + keyword
                + "&date=" + date
                + "&start=" + start
                + "&numOfpages=" + numOfpages;
        Log.i("zw1102",url);
        RequestParams params = new RequestParams(url);
      /*  params.addBodyParameter("weixinhao", weixinhao);
        params.addBodyParameter("weixinhaoyou", weixinhaoyou);
        params.addBodyParameter("keyword", keyword);
        params.addBodyParameter("date", date + "");
        params.addBodyParameter("start", start + "");
        params.addBodyParameter("numOfpages", numOfpages + "");*/
        //Log.i("zw1102", params.toJSONString());
        x.http().get(params, new Callback.CommonCallback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onSuccess(String result) {
                Log.i("zw1102", result);
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<LiaoTianJiLuBean>>() {
                    }.getType();
                    liaotianjiluList = gson.fromJson(result, type);
                    if (liaotianjiluList.size() == 0) {
                        Log.i("zw1102", liaotianjiluList.size() + "123");
                        if (loadIndex > 0) {
//                            isLoad = false;
                            adapter.setLoad(false, 1);
                            devicesNoneww.setVisibility(View.GONE);
                        } else {
                            adapter.setLoad(false, 0);
                            devicesNoneww.setVisibility(View.VISIBLE);
//                            rlSerch.setVisibility(View.GONE);
                        }

                        loadIndex = 1;
                        adapter.notifyDataSetChanged();
                    } else {
                        rlSerch.setVisibility(View.VISIBLE);
                        devicesNoneww.setVisibility(View.GONE);
                        Log.i("weixinhaoyouList", liaotianjiluList.size() + "");
                        listData.addAll(liaotianjiluList);
                        adapter.setLoad(true, 2);
                        loadIndex = loadIndex + 2;
                        adapter.notifyDataSetChanged();

                        String shijian = liaotianjiluList.get(liaotianjiluList.size() - 1).getLiaotianshijian();
                        shijian = shijian.substring(0, 10);
                        tvShijian.setText(shijian);
                    }
                    mHandler.sendEmptyMessage(LOADSUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(LOADFAIL);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                mHandler.sendEmptyMessage(LOADFAIL);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    protected void showDatePickDlg() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(LiaoTianJiLuActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                tvShijian.setText(date + "");
                /*SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟
                try {
                    date=sdf.parse(shijian);
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
                listData.clear();
                start = 0;
                loadIndex = 0;
                adapter.setLoad(false, 0);
                selectBean = new SelectBean(keyword, date, start, numOfpages);
                selectLiaoTianNeiRongByBean(weixinhao, weixinhaoyou, selectBean);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();

    }
}

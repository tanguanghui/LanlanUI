package com.gaodemap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapRestrictionInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.PoiInputItemWidget;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gaodemap.search.SearchPoiActivity;
import com.gaodemap.search.SearchResultAdapter;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.speechcontrol.VoiceChatActivity;
import com.speechcontrol.speech.setting.TtsSettings;
import com.speechcontrol.speech.util.JsonParser;
import com.speechcontrol.util.Tools;
import com.tapadoo.alerter.Alerter;
import com.ui.fragment.LeftFragment;
import com.uidemo.R;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.utils.ToastUtils;
import com.widget.LoadDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.uidemo.R.id.spectrum_voice;

/// OnCheckedChangeListener
public class RestRouteShowActivity extends Activity implements LocationSource,AMapLocationListener,
        AMapNaviListener,AMapNaviViewListener,PoiSearch.OnPoiSearchListener,
        OnClickListener,TextWatcher,AdapterView.OnItemClickListener, View.OnTouchListener,Inputtips.InputtipsListener
       {
    private boolean congestion, cost, hightspeed, avoidhightspeed;
    /**
     * 导航对象(单例)
     */
    private AMapNavi mAMapNavi;
    /**
     * 地图对象
     */
    private AMap mAmap;  //地图对象


    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;


    private MapView mRouteMapView;  //地图控件
//    private AMapNaviView mRouteMapView;
    private Marker mStartMarker;
    private Marker mEndMarker;
   //30°14′45″N 120°8′30″E   杭州西湖坐标
    private NaviLatLng endLatlng = new NaviLatLng(30.1445, 120.830);
//    private NaviLatLng startLatlng = new NaviLatLng(39.925041, 116.437901);
//    private NaviLatLng endLatlng = null;
    private NaviLatLng startLatlng =  new NaviLatLng(30.1445, 120.830);



    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    /**
     * 当前用户选中的路线，在下个页面进行导航
     */
    private int routeIndex;
    /**
     * 路线的权值，重合路线情况下，权值高的路线会覆盖权值低的路线
     **/
    private int zindex = 1;
    /**
     * 路线计算成功标志位
     */
    private boolean calculateSuccess = false;
    private boolean chooseRouteSuccess = false;
    private EditText editText;
    private AutoCompleteTextView mKeywordText;
    private ListView resultList;
    private List<Tip> mCurrentTipList;
    private SearchResultAdapter resultAdapter;
    private ProgressBar loadingBar;
    private TextView tvMsg,mDestName,mStartName;
    private Poi selectedPoi;
    private String city = "北京市";
           private static String TAG = RestRouteShowActivity.class.getSimpleName();
////////////////////////////////////////////////////////////
// 语音合成对象
    private SpeechSynthesizer mTts;
// 语音听写对象
    private SpeechRecognizer mIat;
// 默认云端发音人
    private String voicer = "xiaoyan";
    public static String voicerCloud="xiaoyan";
           // 默认本地发音人
//    public static String voicerLocal="xiaoyan";
// 云端发音人列表
    private String[] cloudVoicersValue ;
    private String[] cloudVoicersEntries;
           private Timer speakTimer;
           private MySpeakTask speakTask;
           // 本地发音人列表
//    private String[] localVoicersEntries;
//    private String[] localVoicersValue ;
// 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
           //缓冲进度
           private int mPercentForBuffering = 0;
           //播放进度
           private int mPercentForPlaying = 0;

           // 云端/本地选择按钮
           private RadioGroup mRadioGroup;
           // 引擎类型
           private String mEngineType = SpeechConstant.TYPE_CLOUD;

           private Toast mToast;
           private SharedPreferences mSharedPreferences;
           private RecognizerDialog mIatDialog;
///** 返回结果，开始说话 */
public final int SPEECH_START = 2;
           /** 开始识别 */
           public final int RECOGNIZE_RESULT = 1;
           /** 开始识别 */
           public final int RECOGNIZE_START = 0;
           public final int RECOGNIZE_RESULT_TULING = 10;
//////////////////////////////////////////////////////////////////////

public Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case RECOGNIZE_START:
                    listening();
                break;
            case SPEECH_START:
//                mKeywordText.setText(null);
                String msg_content = (String) msg.obj;
                speak(msg_content);
//                mKeywordText.setText(msg_content);

                break;
            case RECOGNIZE_RESULT:
                mKeywordText.setText(null);// 清空显示内容
                mIatResults.clear();
                mKeywordText.setText((String)msg.obj);
                break;
            /////图灵回复
            case RECOGNIZE_RESULT_TULING:
//                mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
//                mResultText.setText((String)msg.obj);
//                    speak((String) msg.obj);
                break;
            default:
                break;
        }
    }
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_calculate);
        requestPermissions();
        findViews();

        editText = (EditText) findViewById(R.id.car_number);
        Button endPoint = (Button) findViewById(R.id.endpoint);
        Button selectroute = (Button) findViewById(R.id.selectroute);
        Button gpsnavi = (Button) findViewById(R.id.gpsnavi);
        Button emulatornavi = (Button) findViewById(R.id.emulatornavi);

        endPoint.setOnClickListener(this);
        selectroute.setOnClickListener(this);
        gpsnavi.setOnClickListener(this);
        emulatornavi.setOnClickListener(this);

        //show map
        mRouteMapView = (MapView) findViewById(R.id.navi_view_mulit);
        // must be write this;
        mRouteMapView.onCreate(savedInstanceState);
        // get map object;
        //开始定位
        initLoc();

        // 初始化Marker添加到地图
        mStartMarker = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start))));
        mEndMarker = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.end))));

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        /////
        resultList.setOnItemClickListener(this);
        resultList.setOnTouchListener(this);
        tvMsg.setVisibility(View.GONE);
        mKeywordText.addTextChangedListener(this);
        mKeywordText.requestFocus();
        initSpeechIat();

    }
    public void initSpeechIat(){
        mIat = SpeechRecognizer.createRecognizer(RestRouteShowActivity.this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(RestRouteShowActivity.this, mTtsInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(RestRouteShowActivity.this, mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        speak("请问有什么可以帮您");

    }
    /**
     * Iat 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

         }
       public void onError(SpeechError error) {
         Log.e(TAG, error.getPlainDescription(true));
       }

    };
    public class MySpeakTask extends TimerTask {

    @Override
    public void run() {
        Message message = new Message();
        message.what = RECOGNIZE_START;
        mHandler.sendMessage(message);
    }
}
    private void speak(String s) {
    setTtsParam();

    if (mIat.isListening()) {
        mIat.cancel();
    }
    mTts.startSpeaking(s, mTtsListener);
}
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

               @Override
               public void onSpeakBegin() {
//                   showVoice(1);
               }

               @Override
               public void onSpeakPaused() {
               }

               @Override
               public void onSpeakResumed() {
               }

               @Override
               public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                   // 合成进度
               }

               @Override
               public void onSpeakProgress(int percent, int beginPos, int endPos) {
                   // 播放进度
               }

               @Override
               public void onCompleted(SpeechError error) {
                   if (error == null) {
                       // playerDi();
//                去掉叮咚的响声10月24日
//                playerUtil.playDi(getBaseContext(), R.raw.office);
                       if (speakTimer != null) {
                           speakTimer.cancel();
                       }
                       if (speakTask != null) {
                           speakTask.cancel();
                       }
                       speakTimer = new Timer(true);
                       speakTask = new MySpeakTask();
                       speakTimer.schedule(speakTask, 1000);
//                       if (!playerUtil.isPlayering() && !text_page) {
//                           listening();
//                       }
                   } else {
//                       showTip("语音合成错误");
                   }
               }

               @Override
               public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                   // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                   // 若使用本地能力，会话id为null
                   // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                   // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                   // Log.d(TAG, "session id =" + sid);
                   // }
               }
           };
    private InitListener mTtsInitListener = new InitListener() {
               @Override
        public void onInit(int code) {
                   Log.d(TAG, "InitListener init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
//                       showTip("初始化失败,错误码：" + code);
                   } else {
                       // 初始化成功，之后可以调用startSpeaking方法
                       // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                       // 正确的做法是将onCreate中的startSpeaking调用移至这里
                   }
               }
           };
   private void setTtsParam() {
               // 清空参数
               mTts.setParameter(SpeechConstant.PARAMS, null);
               // 根据合成引擎设置相应参数
               if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
                   mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                   // 设置在线合成发音人
                   mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
                   // 设置合成语速
                   mTts.setParameter(SpeechConstant.SPEED, "50");
                   // 设置合成音调
                   mTts.setParameter(SpeechConstant.PITCH, "50");
                   // 设置合成音量
                   mTts.setParameter(SpeechConstant.VOLUME, "50");
               } else {
                   mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
                   // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
                   mTts.setParameter(SpeechConstant.VOICE_NAME, "");
                   /**
                    * TODO 本地合成不设置语速、音调、音量，默认使用语记设置 开发者如需自定义参数，请参考在线合成参数设置
                    */
               }
               // 设置播放器音频流类型
               mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
               // 设置播放合成音频打断音乐播放，默认为true
               mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

               // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
               // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
               mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
               mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
           }

   private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
               String sn = null;
               // 读取json结果中的sn字段
               try {
                   JSONObject resultJson = new JSONObject(results.getResultString());
                   sn = resultJson.optString("sn");
               } catch (JSONException e) {
                   e.printStackTrace();
               }

               mIatResults.put(sn, text);

               StringBuffer resultBuffer = new StringBuffer();
               for (String key : mIatResults.keySet()) {
                   resultBuffer.append(mIatResults.get(key));
               }
               String result = resultBuffer.toString();
               resultBuffer = null;
               return result;
  }

    int ret = 0; // 函数调用返回值
  private void listening() {
               // 设置参数
    setIatParam();
    boolean isShowDialog = false;
    if (isShowDialog) {
    // 显示听写对话框
    mIatDialog.setListener(mRecognizerDialogListener);
    mIatDialog.show();
    mToast.makeText(RestRouteShowActivity.this, getString(R.string.text_begin), Toast.LENGTH_LONG).show();
      } else {
      // 不显示听写对话框
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
       Toast.makeText(RestRouteShowActivity.this, "听写失败,错误码：" + ret,Toast.LENGTH_SHORT).show();
                       ;
  } else {
     Toast.makeText(RestRouteShowActivity.this, getString(R.string.text_begin), Toast.LENGTH_SHORT).show();
        ;
       }
    }
  };
  public void setIatParam() {
               // 清空参数
               mIat.setParameter(SpeechConstant.PARAMS, null);

               // 设置听写引擎
               mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
               // 设置返回结果格式
               mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

               String lag = "mandarin";
               if (lag.equals("en_us")) {
                   // 设置语言
                   mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
               } else {
                   // 设置语言
                   mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                   // 设置语言区域
                   mIat.setParameter(SpeechConstant.ACCENT, lag);
               }

               // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
               // mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

               // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
               mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

               // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
               mIat.setParameter(SpeechConstant.ASR_PTT, "1");

               // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
               // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
               mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
               mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
           }
           /**
            * 听写监听器。
            */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

               @Override
               public void onBeginOfSpeech() {
                   // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
                   // showTip("开始说话");
               }

               @Override
               public void onError(SpeechError error) {
                   // Tips：
                   // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
                   // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
                   // showTip(error.getPlainDescription(true));
               }

               @Override
               public void onEndOfSpeech() {
                   // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
                   // showTip("结束说话");
               }

               @Override
               public void onResult(RecognizerResult results, boolean isLast) {
                   Log.d(TAG, results.getResultString());
//			mResultText.setText(null);
                   String contentText = printResult(results);
                   Log.d(TAG, "mmmmmmmmmm"+contentText);
                   if (null != contentText  && !"".equals(contentText.trim()) && contentText.length() > 1) {

                       mHandler.obtainMessage(RECOGNIZE_RESULT, contentText).sendToTarget();

                   } else {
                       listening();
                   }

               }

               @Override
               public void onVolumeChanged(int volume, byte[] data) {
                   Log.d(TAG, "返回音频数据：" + data.length);

               }

               @Override
               public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                   // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                   // 若使用本地能力，会话id为null
                   // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                   // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                   // Log.d(TAG, "session id =" + sid);
                   // }
               }
           };
    private void findViews() {
        mKeywordText = (AutoCompleteTextView) findViewById(R.id.search_input);
        resultList = (ListView) findViewById(R.id.resultList);
        loadingBar = (ProgressBar) findViewById(R.id.search_loading);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        mDestName = (TextView) findViewById(R.id.dest_text);
        mStartName = (TextView) findViewById(R.id.start_text);

    }

    //定位
    private void initLoc() {

        if(mAmap == null){
            mAmap = mRouteMapView.getMap();
            setMap();
        }
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }
    private void setMap() {
        //定位的小图标 默认是蓝点 这里自定义图标，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.start));
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        mAmap.setMyLocationStyle(myLocationStyle);

        mAmap.setLocationSource( this);
        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }
    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    mAmap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    mAmap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //添加图钉
//                    mAmap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                    ////add start point for caluatue road;
                    startLatlng = new NaviLatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                mStartMarker.setPosition(new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude()));
                startList.clear();
                startList.add(startLatlng);
                    mStartName.setText( buffer.toString());
                }


            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }
    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private MarkerOptions getMarkerOptions(AMapLocation amapLocation) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
        //位置
        options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        StringBuffer buffer = new StringBuffer();
        buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() +  "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
        //标题
        options.title(buffer.toString());
        //子标题
        options.snippet("I'm here");
        //设置多少帧刷新一次图片资源
        options.period(60);

        return options;

    }
    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

    }
    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mRouteMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mRouteMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRouteMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        startList.clear();
        wayList.clear();
        endList.clear();
        routeOverlays.clear();
        mRouteMapView.onDestroy();
        /**
         * 当前页面只是展示地图，activity销毁后不需要再回调导航的状态
         */
        mAMapNavi.removeAMapNaviListener(this);
        mAMapNavi.destroy();

    }



    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //清空上次计算的路径列表。
        routeOverlays.clear();
        HashMap<Integer, AMapNaviPath> paths = mAMapNavi.getNaviPaths();
        for (int i = 0; i < ints.length; i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if (path != null) {
                drawRoutes(ints[i], path);
            }
        }
    }

    @Override
    public void onCalculateRouteFailure(int arg0) {
        calculateSuccess = false;
        Toast.makeText(getApplicationContext(), "计算路线失败，errorcode＝" + arg0, Toast.LENGTH_SHORT).show();
    }

    private void drawRoutes(int routeId, AMapNaviPath path) {
        calculateSuccess = true;
        mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, this);
        routeOverLay.setTrafficLine(false);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
    }

    public void changeRoute() {
//        if (!calculateSuccess) {
//            Toast.makeText(this, "请先算路", Toast.LENGTH_SHORT).show();
//            return;
//        }
        /**
         * 计算出来的路径只有一条
         */
        if (routeOverlays.size() == 1) {
            chooseRouteSuccess = true;
            //必须告诉AMapNavi 你最后选择的哪条路
            mAMapNavi.selectRouteId(routeOverlays.keyAt(0));
            Toast.makeText(this, "导航距离:" + (mAMapNavi.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mAMapNavi.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeOverlays.size())
            routeIndex = 0;
        int routeID = routeOverlays.keyAt(routeIndex);
        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.4f);
        }
        routeOverlays.get(routeID).setTransparency(1);
        /**把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明**/
        routeOverlays.get(routeID).setZindex(zindex++);

        //必须告诉AMapNavi 你最后选择的哪条路
        mAMapNavi.selectRouteId(routeID);
        Toast.makeText(this, "路线标签:" + mAMapNavi.getNaviPath().getLabels(), Toast.LENGTH_SHORT).show();
        routeIndex++;
        chooseRouteSuccess = true;

        /**选完路径后判断路线是否是限行路线**/
        AMapRestrictionInfo info = mAMapNavi.getNaviPath().getRestrictionInfo();
        if (!TextUtils.isEmpty(info.getRestrictionTitle())) {
            Toast.makeText(this, info.getRestrictionTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 清除当前地图上算好的路线
     */
    private void clearRoute() {
        for (int i = 0; i < routeOverlays.size(); i++) {
            RouteOverLay routeOverlay = routeOverlays.valueAt(i);
            routeOverlay.removeFromMap();
        }
        routeOverlays.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//         case R.id.calculate:
            case R.id.selectroute:
                clearRoute();
                if (avoidhightspeed && hightspeed) {
                    Toast.makeText(getApplicationContext(), "不走高速与高速优先不能同时为true.", Toast.LENGTH_LONG).show();
                }
                if (cost && hightspeed) {
                    Toast.makeText(getApplicationContext(), "高速优先与避免收费不能同时为true.", Toast.LENGTH_LONG).show();
                }
            /*
             * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
			 * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
			 */
                int strategyFlag = 0;
                try {
                    strategyFlag = mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (strategyFlag >= 0) {
                    String carNumber = editText.getText().toString();
                    AMapCarInfo carInfo = new AMapCarInfo();
                    //设置车牌
                    carInfo.setCarNumber(carNumber);
                    //设置车牌是否参与限行算路
                    carInfo.setRestriction(true);
                    mAMapNavi.setCarInfo(carInfo);
                    mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
                    Toast.makeText(getApplicationContext(), "策略:" + strategyFlag, Toast.LENGTH_LONG).show();
                }
                changeRoute();
                break;
            case R.id.startpoint:
                Intent sintent = new Intent(RestRouteShowActivity.this, SearchPoiActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("pointType", PoiInputItemWidget.TYPE_START);
                sintent.putExtras(bundle);
                startActivityForResult(sintent, 100);
                break;
            case R.id.endpoint:
                Intent eintent = new Intent(RestRouteShowActivity.this, SearchPoiActivity.class);
                Bundle ebundle = new Bundle();
                ebundle.putInt("pointType", PoiInputItemWidget.TYPE_DEST);
                eintent.putExtras(ebundle);
                startActivityForResult(eintent, 200);
                break;
//            case R.id.endpoint:
//                Intent eintent = new Intent(RestRouteShowActivity.this, SearchPoiActivity.class);
//                Bundle ebundle = new Bundle();
//                ebundle.putInt("pointType", PoiInputItemWidget.TYPE_DEST);
//                eintent.putExtras(ebundle);
//                startActivityForResult(eintent, 200);
//                break;
//            case R.id.selectroute:
//                changeRoute();
//                break;
            case R.id.gpsnavi:
                Intent gpsintent = new Intent(getApplicationContext(), RouteNaviActivity.class);
                gpsintent.putExtra("gps", true);
                startActivity(gpsintent);
                break;
            case R.id.emulatornavi:
                Intent intent = new Intent(getApplicationContext(), RouteNaviActivity.class);
                intent.putExtra("gps", false);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getParcelableExtra("poi") != null) {
            clearRoute();
            Poi poi = data.getParcelableExtra("poi");
//            if (requestCode == 100) {//起点选择完成
//                //Toast.makeText(this, "100", Toast.LENGTH_SHORT).show();
//                startLatlng = new NaviLatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude);
//                mStartMarker.setPosition(new LatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude));
//                startList.clear();
//                startList.add(startLatlng);
//            }


            if (requestCode == 200) {//终点选择完成
                //Toast.makeText(this, "200", Toast.LENGTH_SHORT).show();
                endLatlng = new NaviLatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude);
                mEndMarker.setPosition(new LatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude));
                endList.clear();
                endList.add(endLatlng);
            }
        }
    }

    /**
     *  在算路页面，以下接口全不需要处理，在以后的版本中我们会进行优化
     **/
    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] arg0) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void notifyParallelRoad(int arg0) {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onArrivedWayPoint(int arg0) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onGetNavigationText(int arg0, String arg1) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onGpsOpenStatus(boolean arg0) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation arg0) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onStartNavi(int arg0) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void showCross(AMapNaviCross arg0) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo arg0) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat arg0) {

    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            {
                if (tvMsg.getVisibility() == View.VISIBLE) {
                    tvMsg.setVisibility(View.GONE);
                }
                String newText = s.toString().trim();
                if (!TextUtils.isEmpty(newText)) {
                    setLoadingVisible(true);
                    InputtipsQuery inputquery = new InputtipsQuery(newText, city);
                    Inputtips inputTips = new Inputtips(getApplicationContext(), inputquery);
                    inputTips.setInputtipsListener(this);
                    inputTips.requestInputtipsAsyn();
                } else {
                    resultList.setVisibility(View.GONE);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private void setLoadingVisible(boolean isVisible) {
        if (isVisible) {
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            loadingBar.setVisibility(View.GONE);
        }
    }
    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击提示后再次进行搜索，获取POI出入口信息
        if (mCurrentTipList != null) {
            Tip tip = (Tip) parent.getItemAtPosition(position);
            selectedPoi = new Poi(tip.getName(), new LatLng(tip.getPoint().getLatitude(), tip.getPoint().getLongitude()), tip.getPoiID());
            if (!TextUtils.isEmpty(selectedPoi.getPoiId())) {
                PoiSearch.Query query = new PoiSearch.Query(selectedPoi.getName(), "", city);
                query.setDistanceSort(false);
                query.requireSubPois(true);
                PoiSearch poiSearch = new PoiSearch(getApplicationContext(), query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.searchPOIIdAsyn(selectedPoi.getPoiId());
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        setLoadingVisible(false);
        try {
            if (rCode == 1000) {
                mCurrentTipList = new ArrayList<Tip>();
                for (Tip tip : tipList) {
                    if (null == tip.getPoint()) {
                        continue;
                    }
                    mCurrentTipList.add(tip);
                }

                if (null == mCurrentTipList || mCurrentTipList.isEmpty()) {
                    tvMsg.setText("抱歉，没有搜索到结果，请换个关键词试试");
                    tvMsg.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.GONE);
                } else {
                    resultList.setVisibility(View.VISIBLE);
                    resultAdapter = new SearchResultAdapter(getApplicationContext(), mCurrentTipList);
                    resultList.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                }
            } else {
                tvMsg.setText("出错了，请稍后重试");
                tvMsg.setVisibility(View.VISIBLE);
            }
        } catch (Throwable e) {
            tvMsg.setText("出错了，请稍后重试");
            tvMsg.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }
    public void locNav(){
    clearRoute();
//    if (avoidhightspeed && hightspeed) {
//        Toast.makeText(getApplicationContext(), "不走高速与高速优先不能同时为true.", Toast.LENGTH_LONG).show();
//    }
//    if (cost && hightspeed) {
//        Toast.makeText(getApplicationContext(), "高速优先与避免收费不能同时为true.", Toast.LENGTH_LONG).show();
//    }
            /*
             * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
			 * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
			 */
    int strategyFlag = 0;
    try {
        strategyFlag = mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true);
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (strategyFlag >= 0) {
        String carNumber = editText.getText().toString();
        AMapCarInfo carInfo = new AMapCarInfo();
        //设置车牌
        carInfo.setCarNumber(carNumber);
        //设置车牌是否参与限行算路
        carInfo.setRestriction(true);
        mAMapNavi.setCarInfo(carInfo);
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
//        Toast.makeText(getApplicationContext(), "策略:" + strategyFlag, Toast.LENGTH_LONG).show();
    }
    changeRoute();

}

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int errorCode) {
        try {
            LatLng latLng = null;
            int code = 0;
            if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                if (poiItem == null) {
                    return;
                }

                LatLonPoint enterP = poiItem.getEnter();

                if (enterP != null) {
                        latLng = new LatLng(enterP.getLatitude(), enterP.getLongitude());
                    }
            }
//            Poi poi;
//            if (latLng != null) {
//                poi = new Poi(selectedPoi.getName(), latLng, selectedPoi.getPoiId());
//            } else {
//                poi = selectedPoi;
//            }
            mKeywordText.setText(null);
            mDestName.setVisibility(View.VISIBLE);
            mDestName.setText( selectedPoi.getName());

            endLatlng = new NaviLatLng(latLng.latitude, latLng.longitude);
            mEndMarker.setPosition(new LatLng(latLng.latitude, latLng.longitude));
            endList.clear();
            endList.add(endLatlng);
            locNav();
            resultList.setVisibility(View.GONE);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

           private void requestPermissions(){
               try {
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                       int permission = ActivityCompat.checkSelfPermission(this,
                               Manifest.permission.RECORD_AUDIO);
                       if(permission!= PackageManager.PERMISSION_GRANTED) {
                           ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                                   Manifest.permission.ACCESS_FINE_LOCATION,
                                   Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,
                                   Manifest.permission.LOCATION_HARDWARE,Manifest.permission.ACCESS_WIFI_STATE,
                                   Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                                   Manifest.permission.RECORD_AUDIO, Manifest.permission.MOUNT_FORMAT_FILESYSTEMS},0x0010);
                       }
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }

           @Override
           public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
               super.onRequestPermissionsResult(requestCode, permissions, grantResults);
           }
           @Override
           public void onBackPressed() {
               super.onBackPressed();
            finish();
           }
}

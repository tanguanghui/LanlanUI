package com.speechcontrol;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.service.SpeechTtsService;
import com.speechcontrol.bluetooth.BluetoothService;
import com.speechcontrol.speech.PlayerUtil;
import com.speechcontrol.speech.ViewPagerAdapter;
import com.speechcontrol.speech.VoiceSpectrum;
import com.speechcontrol.speech.util.JsonParser;
import com.speechcontrol.tuling.ChatMessage;
import com.speechcontrol.tuling.HttpUtils;
import com.speechcontrol.util.Msg;
import com.speechcontrol.util.Tools;
import com.speechcontrol.util.User;
import com.tapadoo.alerter.Alerter;
import com.ui.fragment.LeftFragment;
import com.uidemo.R;
import com.utils.Constant;
import com.utils.ToastUtils;
import com.utils.Urlutils;
import com.widget.LoadDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

/**
 * Created by smartOrange_4 on 2017/10/24.
 */

public class VoiceChatActivity extends AppCompatActivity implements View.OnClickListener{



    //////////////////
    public boolean isPaused=false;
    ListView listView=null;
    public List<User> userList = null;
    public List<Map<String, Object>> adapterList = null;
    Tools tools=null;
    SimpleAdapter adapter=null;
    Msg m=null;
    ///////////////////////
    private int index = 0;



    private static String TAG = VoiceChatActivity.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    private SpeechSynthesizer mTts;
    /*
     * 图灵机器人
     */
//    private TuringApiManager mTuringApiManager;

    protected int timeCount = 1;
    // 默认发音人
    private String voicer = "xiaoyan";
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private TextView mResultText;
    private TextView mChoiceText;
    private Button mbutton;
    private Toast mToast;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    /*
     * 话筒相关view
     */
    private ViewPager viewPager;// 滑屏
    private View textView;// 文本界面
    private View voiceView;// 语音界面
    private ViewPagerAdapter viewPagerAdapter;// view适配器
    private List<View> views;

    private ImageView btn_send_message;
    private EditText et_content;
    private Button btn_asr;
    private Button btn_rec_1;
    private Button btn_rec_2;
    /**
     * 定义文本输入和语音输入的选择按钮
     */
    private Button btn_choice_voice;
    private Button btn_choice_text;
    private Animation voiceAnim;
    private VoiceSpectrum spectrum_voice;
    protected boolean text_page = false;

    private PlayerUtil playerUtil;

    private Timer speakTimer;
    private MySpeakTask speakTask;

    /** 返回结果，开始说话 */
    public final int SPEECH_START = 2;
    /** 开始识别 */
    public final int RECOGNIZE_RESULT = 1;
    /** 开始识别 */
    public final int RECOGNIZE_START = 0;
    public final int RECOGNIZE_RESULT_TULING = 10;
    //连接成功信息
    private static final int CONNECT_OK = 21;
    //连接失败信息
    private static final int CONNECT_FAIL = 22;
    //设备数量
    public static int mDeviceNums = 0;
    public static String deviceList = "";
    public static String[] devices;
    private List<String> devicesList;
    /**
     * define bluetooth
     */
    private BluetoothAdapter bluetoothAdapter;
    private Switch bluetoothSwitch;
    private Button mBluetoothSearch;
    private Spinner bluetoothList;
    private Button mBluetoothConnect;
    private ArrayAdapter<String> mAdapter;
    private List<String> list = new ArrayList<String>();
    private String strMacAddress;
    private static boolean booleanConnect = false;
    private LinearLayout mLayoutControl;
    //msg 定义
    private static final int msgShowConnect = 1;
    /*方向按钮定义*/
    private Button mButtonRun;
    private Button mButtonBack;
    private Button mButtonLeft;
    private Button mButtonRight;
    private Button mButtonStop;
    private Button mButtonLevo;
    private Button mButtonDextro;

    /**************service 命令*********/
    static final int CMD_STOP_SERVICE = 0x01;       // Main -> service
    static final int CMD_SEND_DATA = 0x02;          // Main -> service
    static final int CMD_SYSTEM_EXIT =0x03;         // service -> Main
    static final int CMD_SHOW_TOAST =0x04;          // service -> Main
    static final int CMD_CONNECT_BLUETOOTH = 0x05;  // Main -> service
    static final int CMD_RECEIVE_DATA = 0x06;       // service -> Main

    MyReceiver receiver;
    String body;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Tools.CMD_UPDATEINFORMATION:
                    //更新自己信息
                    reBroad();
                    break;
                case Tools.CMD_BROAD_SENDMSG:
                    //广播发送信息
                    sendMsgBroad(body);
                    break;
                case Tools.FLUSH:
                    adapter.notifyDataSetChanged();
                    break;
                case Tools.ADDUSER:
                    adapterList.add((Map)msg.obj);
                    adapter.notifyDataSetChanged();
                    break;
                case Tools.SHOW:
                    Toast.makeText(VoiceChatActivity.this, (String) msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case Tools.DESTROYUSER:
                    int i=(Integer) msg.obj;
                    userList.remove(i);
                    adapterList.remove(i);
                    adapter.notifyDataSetChanged();
                case Tools.REFLESHCOUNT:
                    String ip=msg.obj.toString();
                    Tools.out("刷新条目"+ip);
                    for (int k = 0; k < adapterList.size(); k++) {
                        if (adapterList.get(k).get("ip").equals("IP:"+ip))
                        { // 遍历
                            if(Tools.msgContainer.get(ip)==null)
                            {
                                adapterList.get(k).put("UnReadMsgCount", "");
                            }
                            else {
                                adapterList.get(k).put("UnReadMsgCount", "未读:"+Tools.msgContainer.get(ip).size());
                                Tools.out("找到了:"+Tools.msgContainer.get(ip));
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case RECOGNIZE_START:
                    if (!text_page)
                        listening();
                    break;
                case SPEECH_START:
                    mChoiceText.setText(null);
                    String msg_content = (String) msg.obj;
                    speak(msg_content);
                    mChoiceText.setText(msg_content);

                    break;
                case RECOGNIZE_RESULT:
                    mResultText.setText(null);// 清空显示内容
                    mIatResults.clear();
                    mResultText.setText((String)msg.obj);
                    break;
                /////图灵回复
                case RECOGNIZE_RESULT_TULING:
                    mResultText.setText(null);// 清空显示内容
                    mIatResults.clear();
                    mResultText.setText((String)msg.obj);
//                    speak((String) msg.obj);
                    break;
                /////
                case CONNECT_OK:
                    try {
                        //解析字符串(xx;xx;xx)
                        String mm = (String) msg.obj;
                        Log.i("123", mm);
                        int j = mm.lastIndexOf(";");
                        String num = mm.substring(0, j);
                        int k = num.lastIndexOf(";");
                        String numbers = mm.substring(0, k);
                        int ii = Integer.parseInt(numbers);
                        if (ii == 0) {
                            String deviceNum = mm.substring(k + 1, j);
                            mDeviceNums = Integer.parseInt(deviceNum);
                            deviceList = mm.substring(j + 1);
                            devices = deviceList.replace("[", "").replace("]", "").split(",");
                            ToastUtils.showShort(VoiceChatActivity.this, "当前设备:" + mDeviceNums + "台");
                            devicesList = new ArrayList<>();
                            for (int aa = 0; aa < LeftFragment.mDeviceNums; aa++) {
                                devicesList.add(devices[aa]);
                            }
//                            app.setDeviceTile(devicesList);
                        } else if (ii == 1) {
//                            stop();
//                            start(Constant.musices[7]);  //连接成功语音
                            speak("懒懒成功连接设备，请主人指示");
                            LoadDialog.dismiss(VoiceChatActivity.this);
                            Alerter.create(VoiceChatActivity.this)
                                    .setTitle("连接成功")
                                    .setDuration(500)
                                    .setBackgroundColor(R.color.alert_bg)
                                    .show();
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showShort(VoiceChatActivity.this, "获取设备异常");
                    }
                    break;
                case CONNECT_FAIL:
                    LoadDialog.dismiss(VoiceChatActivity.this);
                    Alerter.create(VoiceChatActivity.this)
                            .setTitle("连接服务器异常，请检查")
                            .setDuration(600)
                            .setBackgroundColor(R.color.alert_bg)
                            .show();
                    speak("懒懒连接服务器异常，请主人检查配置");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        隐藏 返回键 Home键 隐藏最下面的NAVIGATION栏
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
//        getWindow().setAttributes(params);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        setContentView(R.layout.activity_voice);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        initDeviceInfo();
        initLayout();
        initListener();
        initData();
        initSpeech();
        initBluetooth();

    }

    public void initDeviceInfo() {
        Tools.State=Tools.VOICEACTIVITY;//状态
        Tools.mainA=this;
        init();
        tools=new Tools(this,Tools.ACTIVITY_VOICE);
        //广播上线(包括自己)
        reBroad();
        Tools.out("开始");
        // 开启接收端 时时更新在线列表
        tools.receiveMsg();
        // 心跳
        tools.startCheck();

    }
    // 初始化布局
    public void init() {

        listView = (ListView) super.findViewById(R.id.listView);

        // 初始化
        userList = new ArrayList<User>();
        Tools.me = new User(Build.MODEL,Tools.getLocalHostIp(),0,System.currentTimeMillis());
        userList.add(Tools.me.getCopy());
        adapterList = new ArrayList<Map<String, Object>>();
        Map map = new HashMap<String, Object>();
        map.put("headicon", Tools.headIconIds[Tools.me.getHeadIconPos()]);
        map.put("name", Tools.me.getName());
        map.put("ip", " IP:"+Tools.me.getIp());
        map.put("UnReadMsgCount", "");

        adapterList.add(map);
        //初始化view适配器
        adapter = new SimpleAdapter(this, adapterList, R.layout.voice_ip_item,
                new String[] {"headicon","name", "ip", "UnReadMsgCount" }, new int[] {
                R.id.headicon,R.id.name, R.id.ip, R.id.UnReadMsgCount });
        listView.setAdapter(adapter);
    }
    //广播自己
    public void reBroad() {
        //广播上线(包括自己)
        Msg msg=new Msg();
        msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
        msg.setHeadIconPos(Tools.me.getHeadIconPos());
        msg.setSendUserIp(Tools.me.getIp());
        msg.setReceiveUserIp(Tools.getBroadCastIP());
        msg.setMsgType(Tools.CMD_ONLINE);//通知上线命令
        msg.setPackId(Tools.getTimel());
        // 发送广播通知上线
        tools.sendMsg(msg);
    }

    //广播自己
    public void sendMsgBroad(String body) {

        Msg msg=new Msg(Tools.getBroadCastIP(),Tools.CMD_BROAD_SENDMSG, body);
        // 发送广播命令
        tools.sendMsg(msg);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        }else{
            bluetoothSwitch.setChecked(true);
        }

        isPaused = false;
        Tools.out("Resume");
        reBroad();
        Tools.State=Tools.VOICEACTIVITY;
        speak("请问有什么可以帮您");

        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.bluetooth.admin.bluetooth");
        VoiceChatActivity.this.registerReceiver(receiver,filter);


    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
        isPaused = false;
        mIat.cancel();
        mIat.destroy();

        showVoice(1);
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        speakTask = null;
        speakTimer = null;
        playerUtil.stop();
        playerUtil.release();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
        mIat.cancel();
        mIat.destroy();

        showVoice(1);
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        speakTask = null;
        speakTimer = null;
        playerUtil.stop();
        playerUtil.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPaused=true;
        // 退出时释放连接
        if(receiver!=null){
           VoiceChatActivity.this.unregisterReceiver(receiver);
        }
        mIat.cancel();
        mIat.destroy();

        showVoice(1);
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        speakTask = null;
        speakTimer = null;
        playerUtil.stop();
        playerUtil.release();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        System.out.println("按下了back键 onBackPressed()");
//        showTip("按下了back键 onBackPressed()");
    }

    private void initLayout() {
        mResultText = (TextView) findViewById(R.id.tv_show);
        mChoiceText = (TextView) findViewById(R.id.tv_choice);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        voiceView = LayoutInflater.from(this).inflate(R.layout.part_voice, null);
//		voiceView.setBackgroundColor(android.graphics.Color.parseColor("#00000000"));
        textView = LayoutInflater.from(this).inflate(R.layout.part_text, null);

        // 频谱
        spectrum_voice = (VoiceSpectrum) voiceView.findViewById(R.id.spectrum_voice);
        btn_asr = (Button) voiceView.findViewById(R.id.btn_asr);
        btn_rec_1 = (Button) voiceView.findViewById(R.id.btn_rec_1);
        btn_rec_2 = (Button) voiceView.findViewById(R.id.btn_rec_2);
        //初始化文本输入和语音识别的选择按钮
        btn_choice_voice = (Button) voiceView.findViewById(R.id.btn_voice);
        btn_choice_text = (Button) voiceView.findViewById(R.id.btn_text);

        btn_send_message = (ImageView) textView.findViewById(R.id.btn_send_message);
        et_content = (EditText) textView.findViewById(R.id.et_content);
        initSpeech();
//        int permission = ContextCompat.checkSelfPermission(VoiceChatActivity.this, Manifest.permission.READ_PHONE_STATE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            //请求权限
//            ActivityCompat.requestPermissions(VoiceChatActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.LOCATION_HARDWARE,
//                    Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.RECORD_AUDIO,Manifest.permission.MOUNT_FORMAT_FILESYSTEMS}, 0x0010);
//        } else {
//
//            initSpeech();
//        }
    }

    /**
     * 使用图灵的API接口，
     * @param content
     */
    private void tuLingAPi(final String content) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatMessage fromMessage = HttpUtils.sendMessage(content);
                Log.d("aaaa",fromMessage.getMsg());
                mHandler.obtainMessage(SPEECH_START, fromMessage.getMsg()).sendToTarget();

            }
        }).start();

    }
    /**
     * init  Iat and Tts
     */
    private void initSpeech() {


        mIat = SpeechRecognizer.createRecognizer(VoiceChatActivity.this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(VoiceChatActivity.this, mTtsInitListener);
//    cancel playerUtil  10月23日     控制监听
        playerUtil = new PlayerUtil();
//    cancel playerUtil
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(VoiceChatActivity.this, mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        speak("请问有什么可以帮您");
    }

    private void initListener() {
        btn_send_message.setOnClickListener(this);
        btn_asr.setOnClickListener(this);
        spectrum_voice.setOnClickListener(this);
        et_content.setOnClickListener(this);
    }

    private void initData() {
        views = new ArrayList<View>();
        views.add(voiceView);
        views.add(textView);
        viewPagerAdapter = new ViewPagerAdapter();
        viewPagerAdapter.setViews(views);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(onPageChangeListener);

        voiceAnim = AnimationUtils.loadAnimation(this, R.anim.voice_rotate);
        voiceAnim.setInterpolator(new LinearInterpolator());
        showVoice(1);
    }

    private void initBluetooth() {
        mLayoutControl=(LinearLayout) findViewById(R.id.layout_control);
        bluetoothSwitch = (Switch) findViewById(R.id.bluetoothswitch);
        mBluetoothSearch = (Button) findViewById(R.id.buttonSearchBluetooth);
        bluetoothList = (Spinner) findViewById(R.id.list_bluetooth);
        mBluetoothConnect = (Button) findViewById(R.id.buttonConnectBluetooth);
		 /*按钮监听按下弹起*/
        ButtonListener b = new ButtonListener();
        mButtonRun = (Button) findViewById(R.id.button_run);
        mButtonBack = (Button) findViewById(R.id.button_back);
        mButtonLeft = (Button) findViewById(R.id.button_left);
        mButtonRight = (Button) findViewById(R.id.button_right);
        mButtonStop = (Button) findViewById(R.id.button_stop);
        mButtonLevo = (Button) findViewById(R.id.button_levo);
        mButtonDextro = (Button) findViewById(R.id.button_dextro);


        //mButtonRun.setOnClickListener(b);
        mButtonRun.setOnTouchListener(b);
        mButtonBack.setOnTouchListener(b);
        mButtonLeft.setOnTouchListener(b);
        mButtonRight.setOnTouchListener(b);
        mButtonStop.setOnTouchListener(b);
        mButtonLevo.setOnTouchListener(b);
        mButtonDextro.setOnTouchListener(b);
        mBluetoothSearch.setOnTouchListener(b);
        mBluetoothConnect.setOnTouchListener(b);
		 /*检查手机是否支持蓝牙*/
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //表明此手机不支持蓝牙
            Toast.makeText(VoiceChatActivity.this, "未发现蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(true);
        }
/*添加蓝牙列表*/
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothList.setAdapter(mAdapter);
        bluetoothList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                strMacAddress = mAdapter.getItem(i);
                adapterView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
		/*蓝牙总开关*/
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启，则开启蓝牙
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableIntent);

                    } else {
                        Toast.makeText(VoiceChatActivity.this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                        bluetoothSwitch.setChecked(true);
                    }
                } else {
                    bluetoothAdapter.disable();
                    bluetoothSwitch.setChecked(false);
                    Toast.makeText(VoiceChatActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("fond:", "mReceiver");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 已经配对的则跳过
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mAdapter.add(device.getAddress());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //搜索结束
                Log.e("fond:", "ACTION_DISCOVERY_FINISHED");
                if (mAdapter.getCount() == 0) {
                    Toast.makeText(VoiceChatActivity.this, "没有搜索到设备", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };
    public void showToast(String str){//显示提示信息
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }
    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.bluetooth.admin.bluetooth")){
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");

                if(cmd == CMD_SHOW_TOAST){
                    String str = bundle.getString("str");
                    showToast(str);
                    if ("连接成功建立，可以开始操控了!".equals(str))
                    {
                        booleanConnect = true;
                        mBluetoothConnect.setEnabled(true);
                        mBluetoothConnect.setText("断开");
                        mLayoutControl.setVisibility(View.VISIBLE);
                    }
                    else if("连接失败".equals(str))
                    {
                        booleanConnect = false;
                        mBluetoothConnect.setEnabled(true);
                        mBluetoothConnect.setText("连接");
                        mLayoutControl.setVisibility(View.GONE);
                    }
                }
                else if(cmd == CMD_SYSTEM_EXIT){
                    System.exit(0);
                }
                else if(cmd == CMD_RECEIVE_DATA)  //此处是可以接收蓝牙发送过来的数据可以解析，此例程暂时不解析返回来的数据，需要解析的在我们的全功能版会有
                {
//                    String strtemp = bundle.getString("str");
//                    int start = strtemp.indexOf("$");
//                    int end = strtemp.indexOf("#");
//
//                    if (start >= 0 && end > 0 && end > start && strtemp.length() > 23 )
//                    {
//                        String str = strtemp.substring(23);
//                        String strCSB = str.substring(0, str.indexOf(","));
//                        String strVolume = str.substring(str.indexOf(",")+1, str.indexOf("#"));
//                        tvCSB.setText(strCSB);
//                        tvVolume.setText(strVolume);
//                    }
                }

            }
        }
    }
    public void SendBlueToothProtocol(String value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        intent.putExtra("cmd", CMD_SEND_DATA);
        intent.putExtra("command", (byte)0x00);
        intent.putExtra("value", value);
        sendBroadcast(intent);//发送广播
    }
    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {
            if (v.getId() == R.id.button_run) {

            }
        }

        public boolean onTouch(View v, MotionEvent event)
        {
            switch (v.getId()) {
                case R.id.buttonSearchBluetooth: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mBluetoothSearch.setBackgroundResource(R.drawable.button_stytle);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (bluetoothAdapter == null) {
                            Toast.makeText(VoiceChatActivity.this, "未发现蓝牙设备", Toast.LENGTH_SHORT).show();

                        } else if (!bluetoothAdapter.isEnabled()) {
                            Toast.makeText(VoiceChatActivity.this, "蓝牙设备未开启", Toast.LENGTH_SHORT).show();
                        }

                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                mAdapter.remove(device.getAddress());
                                mAdapter.add(device.getAddress());
                            }
                        } else {
                            //注册，当一个设备被发现时调用mReceive
                            Log.i("seach", "hhhhhh");
                            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            registerReceiver(mReceiver, filter);
                        }

                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mBluetoothSearch.setBackgroundResource(R.drawable.button_bg_red);
                    }
                }break;
                case R.id.buttonConnectBluetooth: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mBluetoothConnect.setBackgroundResource(R.drawable.button_stytle);
                        if (strMacAddress == null) {
                            Toast.makeText(VoiceChatActivity.this, "请先搜索设备", Toast.LENGTH_SHORT).show();
                        } else {
                            if (booleanConnect == false) {
                                Intent i = new Intent(VoiceChatActivity.this, BluetoothService.class);
                                i.putExtra("Mac", strMacAddress);
                                startService(i);
                                mBluetoothConnect.setEnabled(false);
                            }
                            else // 断开蓝牙
                            {
                                mLayoutControl.setVisibility(View.GONE);
                                booleanConnect = false;
                                mBluetoothConnect.setText("连接");
                                Intent intent = new Intent();//创建Intent对象
                                intent.setAction("android.intent.action.cmd");
                                intent.putExtra("cmd", CMD_STOP_SERVICE);
                                sendBroadcast(intent);//发送广播连接蓝牙

                            }
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mBluetoothConnect.setBackgroundResource(R.drawable.button_bg_red);
                    }
                }break;


                case R.id.button_run: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {

//						mButtonRun.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        mButtonRun.setBackgroundResource(R.drawable.up);
//                        SendBlueToothProtocol("$0,0,0,0,0,0,0,0,0,0#");
                        SendBlueToothProtocol("ONF");
//                        SendBlueToothProtocol("0");
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        SendBlueToothProtocol("1");
                        SendBlueToothProtocol("ONA");
//                        SendBlueToothProtocol("$1,0,0,0,0,0,0,0,0,0#");
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
//						mButtonRun.setBackgroundColor(Color.parseColor("#CC0000"));
                        mButtonRun.setBackgroundResource(R.drawable.button_shape_red);

                    }
                }break;

                case R.id.button_back: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//						mButtonBack.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        mButtonBack.setBackgroundResource(R.drawable.down);
                        SendBlueToothProtocol("ONF");
//                        SendBlueToothProtocol("0");
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mButtonBack.setBackgroundResource(R.drawable.button_shape_red);
//                        SendBlueToothProtocol("$2,0,0,0,0,0,0,0,0,0#");
                        SendBlueToothProtocol("ONB");
//                        SendBlueToothProtocol("2");
                    }
                }break;

                case R.id.button_left: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mButtonLeft.setBackgroundResource(R.drawable.left);
//                        SendBlueToothProtocol("0");
                        SendBlueToothProtocol("ONF");
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mButtonLeft.setBackgroundResource(R.drawable.button_shape_red);
//                        SendBlueToothProtocol("$3,0,0,0,0,0,0,0,0,0#");
                        SendBlueToothProtocol("ONC");
//                        SendBlueToothProtocol("3");
                    }
                }break;

                case R.id.button_right: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mButtonRight.setBackgroundResource(R.drawable.right);
                        SendBlueToothProtocol("ONF");
//                        SendBlueToothProtocol("0");
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mButtonRight.setBackgroundResource(R.drawable.button_shape_red);
                        SendBlueToothProtocol("OND");
//                        SendBlueToothProtocol("$4,0,0,0,0,0,0,0,0,0#");
//                        SendBlueToothProtocol("4");
                    }
                }break;

                case R.id.button_stop: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mButtonStop.setBackgroundResource(R.drawable.stop);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        mButtonStop.setBackgroundResource(R.drawable.button_shape_red);
//                        SendBlueToothProtocol("0");
                        SendBlueToothProtocol("ONE");
                    }
                } break;

                /*左旋*/
                case R.id.button_levo: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        SendBlueToothProtocol("0");
                        SendBlueToothProtocol("ONF");
                        mButtonLevo.setBackgroundResource(R.drawable.left_handed_rotation);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);

                        mButtonLevo.setBackgroundResource(R.drawable.button_shape_red);
                        SendBlueToothProtocol("ONC");
//                        SendBlueToothProtocol("3");
                    }
                } break;

                /*右旋*/
                case R.id.button_dextro: {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mButtonDextro.setBackgroundResource(R.drawable.right_handed_rotation);
//                        SendBlueToothProtocol("0");
                        SendBlueToothProtocol("ONF");


                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        v.playSoundEffect(SoundEffectConstants.CLICK);

                        mButtonDextro.setBackgroundResource(R.drawable.button_shape_red);
                        SendBlueToothProtocol("OND");
//                        SendBlueToothProtocol("4");
                    }
                }break;


            }
            return false;
        }

    }
    /**
     * pageChange 页面滑动时调用
     */
    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:// voice
                    text_page = false;
                    closeInputMethod();
                    if (playerUtil != null && playerUtil.isPlayering()) {
                        showVoice(1);
                    } else {
                        listening();
                    }
                    break;
                default:
                    text_page = true;
                    mTts.destroy();
                    mIat.destroy();
//                    播放  监听
                    playerUtil.stop();
                    playerUtil.release();
                    break;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            ArgbEvaluator evaluator = new ArgbEvaluator(); // ARGB求值器
            int evaluate = 0x00FFFFFF; // 初始默认颜色（透明白）

            ((View)viewPager.getParent()).setBackgroundColor(evaluate);

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * 关闭键盘
     */
    public void closeInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
            ;
        } catch (Exception e) {
            Log.e(TAG, "关闭键盘失败");
        }
    }

    int ret = 0; // 函数调用返回值

    private void listening() {
//        if(null == mTuringApiManager){
//            initTuring();
//        }
        // 设置参数
        setIatParam();
        boolean isShowDialog = false;
        if (isShowDialog) {
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            mToast.makeText(VoiceChatActivity.this, getString(R.string.text_begin), Toast.LENGTH_LONG).show();
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                Toast.makeText(VoiceChatActivity.this, "听写失败,错误码：" + ret,Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(VoiceChatActivity.this, getString(R.string.text_begin), Toast.LENGTH_SHORT).show();

            }
        }
    };
    /**
     * 网络请求回调
     */
    HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {

        @Override
        public void onSuccess(RequestResult result) {
            if (result != null) {
                try {
                    Log.d(TAG, result.getContent().toString());
                    JSONObject result_obj = new JSONObject(result.getContent()
                            .toString());
                    if (result_obj.has("text")) {
                        Log.d(TAG, result_obj.get("text").toString());
                        mHandler.obtainMessage(SPEECH_START,
                                result_obj.get("text")).sendToTarget();
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException:" + e.getMessage());
                }
            }
        }

        @Override
        public void onError(ErrorMessage errorMessage) {
            Log.d(TAG, errorMessage.getMessage());
        }
    };

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

    /**
     * TTs 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            // showTip("开始说话");
            showVoice(3);
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            // showTip(error.getPlainDescription(true));
            if (true) {// 一直监听
                if (speakTimer != null) {
                    speakTimer.cancel();
                }
                if (speakTask != null) {
                    speakTask.cancel();
                }
                speakTimer = new Timer(true);
                speakTask = new MySpeakTask();
                speakTimer.schedule(speakTask, 95);
            } else {
                if (timeCount < 3) {
                    speak(getResources().getString(R.string.help));
                    // mIat.startListening(mRecognizerListener);
                    timeCount++;
                } else {// 三次未说话就停止
                    showVoice(1);
                    if (mIat.isListening()) {
                        mIat.stopListening();
                    }
                    timeCount = 1;

                }
            }

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            // showTip("结束说话");
            showVoice(2);
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            mChoiceText.setText(null);
//			mResultText.setText(null);
            String contentText = printResult(results);
            Log.d(TAG, "mmmmmmmmmm"+contentText);
            if (null != contentText  && !"".equals(contentText.trim()) && contentText.length() > 1) {

                if(contentText.contains("你真听话") ||contentText.contains("懒懒")||contentText.contains("兰兰")||contentText.contains("蓝染")
                        ||contentText.contains("男篮")||contentText.contains("蓝蓝")||contentText.contains("啦啦")||contentText.contains("拉拉")||contentText.contains("岚岚")||contentText.contains("烂了")||contentText.contains("懒了")||contentText.contains("懒啦")
                        ){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText(R.string.text_consult);
                    sendMsgBroad("害羞");
                    speak("主人，有什么吩咐的吗？");
                    SendBlueToothProtocol("ONE");

                }else if(contentText.contains("前")&&!contentText.contains("不")&&!contentText.contains("后")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("我正向前走的");
                    sendMsgBroad("说话");
                    speak("我正向前走的");
                    SendBlueToothProtocol("ONA");
                    Log.d(TAG,"results：我正向前走的");
                }
                else if(contentText.contains("后退")&&!contentText.contains("不")&&!contentText.contains("前")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("我正在后退中");
                    sendMsgBroad("说话");
                    speak("我正在后退中");
                    SendBlueToothProtocol("ONB");
                }
                else if(contentText.contains("左转")&&!contentText.contains("不")&&!contentText.contains("右")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("我正在向左转");
                    sendMsgBroad("说话");
                    speak("我正在向左转");
                    SendBlueToothProtocol("ONC");
                }
                else if(contentText.contains("右转")&&!contentText.contains("不")&&!contentText.contains("左")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("我正在向右转");
                    sendMsgBroad("说话");
                    speak("我正在向右转");
                    SendBlueToothProtocol("OND");
                }
                else if(contentText.equals("停")&&!contentText.contains("不")||contentText.equals("别动")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("我停下来");
                    sendMsgBroad("说话");
                    speak("我停下来");
                    SendBlueToothProtocol("ONE");
                }
//                连接
                else if(contentText.contains("连接设备")&&!contentText.contains("不")||contentText.contains("连接")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒我正在连接设备，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 1;
                    new Thread(networkTask).start();
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();
                    speak("懒懒正在连接设备，请主人稍等");
                    SendBlueToothProtocol("ONE");
                }
//                亮屏
                else if(contentText.contains("亮")&&!contentText.contains("不")||contentText.contains("亮平")||contentText.equals("亮平")||contentText.equals("两瓶")||contentText.equals("两平")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒我正在点亮屏幕，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 2;
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();

                    new Thread(networkTask).start();
                    speak("懒懒我正在点亮屏幕，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                熄屏
                else if(contentText.contains("熄屏")&&!contentText.contains("不")||contentText.contains("西平")||contentText.contains("希平")||contentText.contains("黑屏")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒我正在关闭屏幕，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 3;
                    new Thread(networkTask).start();
                    speak("懒懒我正在关闭屏幕，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                停止
                else if(contentText.contains("退出")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("主人，懒懒累了，要休息一会会");
                    sendMsgBroad("说话");
                    index = 4;
                    new Thread(networkTask).start();
                    speak("主人，懒懒累了，要休息一会会");
                    SendBlueToothProtocol("ONE");
                }
//                通讯录
                else if(contentText.contains("通讯录")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("主人，懒懒要看看你的通讯录哦");
                    sendMsgBroad("说话");
                    index = 6;
                    new Thread(networkTask).start();
                    speak("主人，懒懒要看看你的通讯录哦");
                    SendBlueToothProtocol("ONE");
                }
//                联系人
                else if(contentText.contains("联系人")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("主人，您准备好了吗？懒懒开始了");
                    sendMsgBroad("说话");
                    index = 7;
                    new Thread(networkTask).start();
                    speak("主人，您准备好了吗？懒懒开始了");
                    SendBlueToothProtocol("ONE");
                }
//                微信附近人
                else if(contentText.contains("微信附近人")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 5;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }

//                微信群好友
                else if(contentText.contains("微信群好友")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 8;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信公众号
                else if(contentText.contains("微信公众号")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 9;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信朋友圈
                else if(contentText.contains("微信朋友圈")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 11;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信一键发消息
                else if(contentText.contains("微信一键发消息")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 12;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                聊天记录
                else if(contentText.contains("聊天记录")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ附近人
                else if(contentText.contains("QQ附近人")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 13;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ群好友
                else if(contentText.contains("QQ群好友")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    index = 14;
                    new Thread(networkTask).start();
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ公众号
                else if(contentText.contains("QQ公众号")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ一键发消息
                else if(contentText.contains("QQ一键发消息")&&!contentText.contains("不")){
                    mResultText.setText(null);
                    mResultText.setText(contentText);
                    mChoiceText.setText("懒懒开始了，请主人稍等一会");
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
                else {
                    mResultText.setText(null);
                    Log.d(TAG,"requestTuringAPI：我正向前走的:"+contentText);
                    if(contentText.contains("好漂亮")||contentText.contains("好可爱")||contentText.contains("喜欢你")||contentText.contains("真好看")||contentText.contains("真漂亮")||contentText.contains("真美")||contentText.contains("真可爱")){

                        sendMsgBroad("害羞");
                    } else if(contentText.contains("好丑")||contentText.contains("臭")||contentText.contains("真丑")||
                            contentText.contains("好难看")||contentText.contains("真难看")
                            ||contentText.contains("好坏")||contentText.contains("垃圾")||contentText.contains("傻逼")||
                            contentText.contains("煞笔")||contentText.contains("菜鸟")||contentText.contains("二货")
                            ||contentText.contains("废物")||contentText.contains("滚蛋")||contentText.contains("麻痹")||contentText.contains("妈逼")){

                        sendMsgBroad("愤怒");
                    }else if(contentText.contains("讨厌你")) {

                        sendMsgBroad("说话");

                    }else if(contentText.contains("你大爷")) {

                        sendMsgBroad("说话");

                    }else if(contentText.contains("想你")) {

                        sendMsgBroad("思念");


                    }else if(contentText.contains("杀了你")||contentText.contains("打死你")||contentText.contains("砸")||contentText.contains("毁了你")||contentText.contains("有怪物")||contentText.contains("弄死你")) {

                        sendMsgBroad("恐惧");


                    }else if(contentText.contains("可怜")||contentText.contains("可惜")||contentText.contains("没关系")||
                            contentText.contains("再不努力就老了")||contentText.contains("好累")||contentText.contains("好险")) {

                        sendMsgBroad("叹气");


                    }else if(contentText.contains("乖")||contentText.contains("笑")) {

                        sendMsgBroad("乖巧");


                    }else if(contentText.contains("看")||contentText.contains("好吃的")||contentText.contains("好看吗")||contentText.contains("好吃的")) {

                        sendMsgBroad("惊讶");


                    }else if(contentText.contains("你好")||contentText.contains("嗨")||contentText.contains("Hello")||contentText.contains("哈喽")) {

                        sendMsgBroad("打招呼");

                    }else if(contentText.contains("耶")) {

                        sendMsgBroad("打招呼");


                    }else if(contentText.contains("你猜")) {

                        sendMsgBroad("眨眼");

                    }
                    mHandler.obtainMessage(RECOGNIZE_RESULT, contentText).sendToTarget();
                    String content =contentText;
                    Log.d(TAG,"requestTuringAPI：我正向前走的0:"+content);
                    tuLingAPi(content);
                }

            } else {
                listening();
            }

        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "返回音频数据：" + data.length);
            spectrum_voice.updateVisualizer(data);
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

    /**
     * Tts合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showVoice(1);
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
                if (!playerUtil.isPlayering() && !text_page) {
                    listening();
                }
            } else {
                showTip("语音合成错误");
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

    /**
     * Iat听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Log.e(TAG, error.getPlainDescription(true));
        }

    };

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

    /**
     * iat语音识别参数设置
     *
     * @setIatParam
     * @return
     */
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
     * tts语音合成参数设置
     *
     * @setTtsParam
     * @return
     */
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

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private void speak(String s) {
        setTtsParam();
        showVoice(1);
        if (mIat.isListening()) {
            mIat.cancel();
        }
        mTts.startSpeaking(s, mTtsListener);
    }

    /**
     * 不同operation，bottom展示不同
     *
     * @showVoice
     */
    private void showVoice(int opration) {
        switch (opration) {
            case 1:// 话筒
                if (voiceAnim != null) {
                    btn_rec_1.clearAnimation();
                }
                btn_asr.setVisibility(View.VISIBLE);
                btn_rec_1.setVisibility(View.GONE);
                btn_rec_2.setVisibility(View.GONE);
                spectrum_voice.setVisibility(View.GONE);
                break;
            case 2:// 识别中
                btn_asr.setVisibility(View.GONE);
                btn_rec_1.setVisibility(View.VISIBLE);
                btn_rec_2.setVisibility(View.VISIBLE);
                spectrum_voice.setVisibility(View.GONE);
                if (voiceAnim != null) {
                    btn_rec_1.startAnimation(voiceAnim);
                }
                break;
            case 3:// 频谱
                if (voiceAnim != null) {
                    btn_rec_1.clearAnimation();
                }
                btn_asr.setVisibility(View.GONE);
                btn_rec_1.setVisibility(View.GONE);
                btn_rec_2.setVisibility(View.GONE);
                spectrum_voice.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_send_message:// 文本的发送
                // baseApplication.closeInputMethod(mContext);
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    showTip("您输入的内容为空。");
                } else {
                    mResultText.setText(null);
                    mResultText.setText(content);
                    mChoiceText.setText(null);
//				mChoiceText.setText(content);
                    if(content.equals("懒懒")){
                        mChoiceText.setText(R.string.text_consult);
                        speak("主人，有什么吩咐的吗？");

                    }else {
                        mChoiceText.setText(content);
//                        mTuringApiManager.requestTuringAPI(content);
                        tuLingAPi(content);
                    }
                    //	speak(content);
                    et_content.setText("");
                }
                break;
            case R.id.btn_asr:// 话筒
                if (btn_asr.getVisibility() == View.VISIBLE) {
                    mTts.destroy();
                    listening();
                }
                break;
            case R.id.spectrum_voice:// 频谱
                if (spectrum_voice.getVisibility() == View.VISIBLE) {
                    mTts.stopSpeaking();
                    /////java.io.FileNotFoundException: /storage/emulated/0/msc/iat.wav: open failed: EACCES (Permission denied)
                    mIat.cancel();
                    showVoice(1);
                    if (mIat.isListening()) {
                        mIat.stopListening();
                    }

                }
                break;
            default:
                break;
        }
    }

    public class MySpeakTask extends TimerTask {

        @Override
        public void run() {
            Message message = new Message();
            message.what = RECOGNIZE_START;
            mHandler.sendMessage(message);
        }
    }

    /**
     * 微信网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            Socket socket = null;
            String socketUrl = Urlutils.getWebUrl();
            try {
                // 建立与vicmob服务器连接的socket
                socket = new Socket(socketUrl, 18888);
                // 写数据到服务端:建立写数据流,往流写数据
                String strVal = Constant.indexStr[index];
                OutputStream os = socket.getOutputStream();
                os.write(strVal.getBytes());
                // 写关闭,不然会影响读:不然会一直阻塞着,服务器会认为客户端还一直在写数据
                // 由于从客户端发送的消息长度是任意的，客户端需要关闭连接以通知服务器消息发送完毕，如果客户端在发送完最后一个字节后
                // 关闭输出流，此时服务端将知道"我已经读到了客户端发送过来的数据的末尾了,即-1",就会读取出数据并关闭服务端的读数据流,在之后就可以
                // 自己(服务端)的输出流了,往客户端写数据了
                socket.shutdownOutput();
                InputStream is = socket.getInputStream();
                byte[] bytes = new byte[1024];
                int len;
                while ((len = is.read(bytes)) != -1) {
                    String printName = new String(bytes, 0, len);
                    String pictureUrl = "http://www.vicmob.net";
                    System.out.println(pictureUrl + printName.toString());
                    Message message = new Message();
                    message.what = CONNECT_OK;
                    message.obj = printName;
                    Thread.sleep(3000);
                    mHandler.sendMessage(message);
                }
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                mHandler.sendEmptyMessage(CONNECT_FAIL);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(CONNECT_FAIL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };



    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
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
}

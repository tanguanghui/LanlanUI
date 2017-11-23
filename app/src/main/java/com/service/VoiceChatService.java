package com.service;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
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
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
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

import android.widget.ImageButton;
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
import com.speechcontrol.VoiceChatActivity;
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
import com.widget.CustomerListView;
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

public class VoiceChatService extends Service {
    public VoiceChatService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        initSpeech();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    ///////////////////////////////////


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
    private static String TAG = VoiceChatService.class.getSimpleName();

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    private SpeechSynthesizer mTts;
    /*
     * 图灵机器人
     */

    protected int timeCount = 1;
    // 默认发音人
    private String voicer = "xiaoyan";
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private Toast mToast;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    protected boolean text_page = false;

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
    private Button mBluetoothSearch,mBluetoothConnect;
    private Spinner bluetoothList;
    private CustomerListView mBluetoothList;
    private Button mOKButton;
    private ImageButton mCancelImageButton;
    private AlertDialog mDialog;

    private ArrayAdapter<String> mAdapter;
    private List<String> list = new ArrayList<String>();
    private String strMacAddress;
    private static boolean booleanConnect = false;
    private LinearLayout mLayoutControl;
    //msg 定义
    private static final int msgShowConnect = 1;


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
                    Toast.makeText(getBaseContext(), (String) msg.obj,
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
                    String msg_content = (String) msg.obj;
                    speak(msg_content);
                    break;


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
                            ToastUtils.showShort(getBaseContext(), "当前设备:" + mDeviceNums + "台");
                            devicesList = new ArrayList<>();
                            for (int aa = 0; aa < LeftFragment.mDeviceNums; aa++) {
                                devicesList.add(devices[aa]);
                            }

                        } else if (ii == 1) {

                            speak("懒懒成功连接设备，请主人指示");
                            LoadDialog.dismiss(getBaseContext());
                            Alerter.create((Activity) getBaseContext())
                                    .setTitle("连接成功")
                                    .setDuration(500)
                                    .setBackgroundColor(R.color.alert_bg)
                                    .show();
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showShort(getBaseContext(), "获取设备异常");
                    }
                    break;
                case CONNECT_FAIL:
                    LoadDialog.dismiss(getBaseContext());
//                    Alerter.create((Activity) getBaseContext())
//                            .setTitle("连接服务器异常，请检查")
//                            .setDuration(600)
//                            .setBackgroundColor(R.color.alert_bg)
//                            .show();
                    speak("懒懒连接服务器异常，请主人检查配置");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initSpeech();


    }

    public void initDeviceInfo() {
        Tools.State=Tools.VOICECHATSERVICE;//状态
        Tools.VoiceChat=this;
        init();
        tools=new Tools(this,Tools.SERVICE_VOICECHAT);
        //广播上线(包括自己)
        reBroad();
        Tools.out("开始");
        // 开启接收端 时时更新在线列表
        tools.receiveMsg();
        // 心跳
        tools.CheckDevice();


    }
    // 初始化布局
    public void init() {

//        listView = (ListView)findViewById(R.id.listView);

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
//        listView.setAdapter(adapter);
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

    public void onRestart() {

        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
    }


    public void onResume() {

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
        getBaseContext().registerReceiver(receiver,filter);
    }

    public void onPause() {
        // TODO Auto-generated method stub
//        super.onPause();
        if (!bluetoothAdapter.isEnabled()) { //蓝牙未开启
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
        isPaused = false;
        mIat.cancel();
        mIat.destroy();

        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        speakTask = null;
        speakTimer = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPaused=true;
        // 退出时释放连接
        if(receiver!=null){
            getBaseContext().unregisterReceiver(receiver);
        }
        mIat.cancel();
        mIat.destroy();

        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        speakTask = null;
        speakTimer = null;

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
//        get devices info of Lan；
        initDeviceInfo();

        mIat = SpeechRecognizer.createRecognizer(getBaseContext(), mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(getBaseContext(), mTtsInitListener);
//    cancel playerUtil  10月23日     控制监听
//        playerUtil = new PlayerUtil();
//    cancel playerUtil
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(getBaseContext(), mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        speak("请问有什么可以帮您");
    }


     public void initBluetooth() {

        View view = View.inflate(VoiceChatService.this, R.layout.dialog_bluetooth, null);
        // 初始Dialog 里面的内容
        mOKButton = (Button) view.findViewById(R.id.btn_order);
        mBluetoothSearch = (Button)view.findViewById(R.id.button_Search);
        mBluetoothConnect = (Button)view.findViewById(R.id.button_Connect);
        mCancelImageButton =(ImageButton)view.findViewById(R.id.image_button_cancel);
            bluetoothSwitch = (Switch) view.findViewById(R.id.bluetoothswitch);
            bluetoothList = (Spinner) view.findViewById(R.id.list_bluetooth);
        ButtonListener b = new ButtonListener();
        mCancelImageButton.setOnClickListener(b);
        mOKButton.setOnClickListener(b);
        mBluetoothSearch.setOnTouchListener(b);
        mBluetoothConnect.setOnTouchListener(b);

//        mBluetoothList = (CustomerListView) view.findViewById(R.id.listView_bluetooth);

        AlertDialog.Builder builder = new AlertDialog.Builder(VoiceChatService.this);
        builder.setView(view);
        mDialog = builder.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
         mDialog.show();
		 /*检查手机是否支持蓝牙*/
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //表明此手机不支持蓝牙
            Toast.makeText(getBaseContext(), "未发现蓝牙设备", Toast.LENGTH_SHORT).show();
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
                        enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(enableIntent);
                    } else {
                        Toast.makeText(getBaseContext(), "蓝牙已开启", Toast.LENGTH_SHORT).show();
                        speak("蓝牙已开启");
                        bluetoothSwitch.setChecked(true);
                    }
                } else {
                    bluetoothAdapter.disable();
                    bluetoothSwitch.setChecked(false);
                    Toast.makeText(getBaseContext(), "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                    speak("蓝牙已关闭");
                }
            }
        });

    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
                    Toast.makeText(getBaseContext(), "没有搜索到设备", Toast.LENGTH_SHORT).show();
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
            switch (v.getId()) {
                case R.id.btn_order:
                    //TODO
                    mDialog.dismiss();
                    break;
                case R.id.image_button_cancel:
                    //TODO
                    mDialog.dismiss();
                    break;

                default:
                    break;
            }
        }

        public boolean onTouch(View v, MotionEvent event)
        {
            switch (v.getId()) {
                case R.id.button_Search:
                {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mBluetoothSearch.setBackgroundResource(R.drawable.button_stytle);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (bluetoothAdapter == null) {
                            Toast.makeText(getBaseContext(), "未发现蓝牙设备", Toast.LENGTH_SHORT).show();

                        } else if (!bluetoothAdapter.isEnabled()) {
                            Toast.makeText(getBaseContext(), "蓝牙设备未开启", Toast.LENGTH_SHORT).show();
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
                }
                break;
                case R.id.button_Connect:
                {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mBluetoothConnect.setBackgroundResource(R.drawable.button_stytle);
                        if (strMacAddress == null) {
                            Toast.makeText(getBaseContext(), "请先搜索设备", Toast.LENGTH_SHORT).show();
                        } else {
                            if (booleanConnect == false) {
                                Intent i = new Intent(getBaseContext(), BluetoothService.class);
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
                }
                break;

            }
            return false;
        }

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
            mToast.makeText(getBaseContext(), getString(R.string.text_begin), Toast.LENGTH_LONG).show();
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                Toast.makeText(getBaseContext(), "听写失败,错误码：" + ret,Toast.LENGTH_SHORT).show();
                ;
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.text_begin), Toast.LENGTH_SHORT).show();
                ;
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
//            showVoice(3);
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if(error.getErrorCode() == 10118)
            {
                //TODO
            }
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
                     mIat.startListening(mRecognizerListener);
                    timeCount++;
                } else {// 三次未说话就停止

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
//            showVoice(2);
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            String contentText = printResult(results);
            if (null != contentText  && !"".equals(contentText.trim()) && contentText.length() > 1) {

                if(contentText.contains("你真听话") ||contentText.contains("懒懒")||contentText.contains("兰兰")||contentText.contains("蓝染")
                        ||contentText.contains("男篮")||contentText.contains("蓝蓝")||contentText.contains("啦啦")||contentText.contains("拉拉")||contentText.contains("岚岚")||contentText.contains("烂了")||contentText.contains("懒了")||contentText.contains("懒啦")
                        ){
                    sendMsgBroad("害羞");
                    speak("主人，有什么吩咐的吗？");
                    SendBlueToothProtocol("ONE");

                }else if(contentText.contains("开启蓝牙")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    speak("我正查找蓝牙");

                    Log.d(TAG,"contentText：开启蓝牙" + contentText);
                    ////
                    initBluetooth();
                    ////
                    {
                            if (bluetoothAdapter == null) {
                                Toast.makeText(getBaseContext(), "未发现蓝牙设备", Toast.LENGTH_SHORT).show();

                            } else if (!bluetoothAdapter.isEnabled()) {
                                Toast.makeText(getBaseContext(), "蓝牙设备未开启", Toast.LENGTH_SHORT).show();
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


                    }
                    SendBlueToothProtocol("ONA");
                    //////
                }

                else if(contentText.contains("前")&&!contentText.contains("不")&&!contentText.contains("后")){
                    sendMsgBroad("说话");
                    speak("我正向前走的");
                    SendBlueToothProtocol("ONA");
                    Log.d(TAG,"results：我正向前走的");
                }
                else if(contentText.contains("后退")&&!contentText.contains("不")&&!contentText.contains("前")){
                    sendMsgBroad("说话");
                    speak("我正在后退中");
                    SendBlueToothProtocol("ONB");
                }
                else if(contentText.contains("左转")&&!contentText.contains("不")&&!contentText.contains("右")){
                    sendMsgBroad("说话");
                    speak("我正在向左转");
                    SendBlueToothProtocol("ONC");
                }
                else if(contentText.contains("右转")&&!contentText.contains("不")&&!contentText.contains("左")){
                    sendMsgBroad("说话");
                    speak("我正在向右转");
                    SendBlueToothProtocol("OND");
                }
                else if(contentText.equals("停")&&!contentText.contains("不")||contentText.equals("别动")){
                    sendMsgBroad("说话");
                    speak("我停下来");
                    SendBlueToothProtocol("ONE");
                }
//                连接
                else if(contentText.contains("连接设备")&&!contentText.contains("不")||contentText.contains("连接")){
                    sendMsgBroad("说话");
                    index = 1;
                    new Thread(networkTask).start();
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();
                    speak("懒懒我正在连接设备，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                亮屏
                else if(contentText.contains("亮屏")&&!contentText.contains("不")||contentText.equals("亮平")||contentText.equals("两平")){
                    sendMsgBroad("说话");
                    index = 2;
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();

                    new Thread(networkTask).start();
                    speak("懒懒我正在点亮屏幕，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                熄屏
                else if(contentText.contains("熄屏")&&!contentText.contains("不")||contentText.equals("西平")||contentText.equals("希平")||contentText.equals("黑屏")){
                    sendMsgBroad("说话");
                    index = 3;
                    new Thread(networkTask).start();
                    speak("懒懒我正在关闭屏幕，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                停止
                else if(contentText.contains("退出")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 4;
                    new Thread(networkTask).start();
                    speak("主人，懒懒累了，要休息一会会");
                    SendBlueToothProtocol("ONE");
                }
//                通讯录
                else if(contentText.contains("通讯录")){
                    sendMsgBroad("说话");
                    index = 6;
                    new Thread(networkTask).start();
                    speak("主人，懒懒要看看你的通讯录哦");
                    SendBlueToothProtocol("ONE");
                }
//                联系人
                else if(contentText.contains("联系人")){
                    sendMsgBroad("说话");
                    index = 7;
                    new Thread(networkTask).start();
                    speak("主人，您准备好了吗？懒懒开始了");
                    SendBlueToothProtocol("ONE");
                }
//                微信附近人
                else if(contentText.contains("微信附近人")){
                    sendMsgBroad("说话");
                    index = 5;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }

//                微信群好友
                else if(contentText.contains("微信群好友")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 8;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信公众号
                else if(contentText.contains("微信公众号")){
                    sendMsgBroad("说话");
                    index = 9;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信朋友圈
                else if(contentText.contains("微信朋友圈")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 11;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                微信一键发消息
                else if(contentText.contains("微信一键发消息")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 12;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                聊天记录
                else if(contentText.contains("聊天记录")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ附近人
                else if(contentText.contains("QQ附近人")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 13;
                    new Thread(networkTask).start();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ群好友
                else if(contentText.contains("QQ群好友")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    index = 14;
                    new Thread(networkTask).start();
//                    mHandler.obtainMessage(CONNECT_OK, index).sendToTarget();
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ公众号
                else if(contentText.contains("QQ公众号")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
//                QQ一键发消息
                else if(contentText.contains("QQ一键发消息")&&!contentText.contains("不")){
                    sendMsgBroad("说话");
                    speak("懒懒开始了，请主人稍等一会");
                    SendBlueToothProtocol("ONE");
                }
                else {
//                    mResultText.setText(null);
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
//            spectrum_voice.updateVisualizer(data);
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
//            showVoice(1);
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

                listening();
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
//        showVoice(1);
        if (mIat.isListening()) {
            mIat.cancel();
        }
        mTts.startSpeaking(s, mTtsListener);
    }

   public class MySpeakTask extends TimerTask {

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



}

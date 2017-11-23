package com.speechcontrol.tuling;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by smartOrange_4 on 2017/10/10.
 */

public class HttpUtils {
    private static final String TULing_URL = "http://www.tuling123.com/openapi/api";
    private static final String API_KEY = "31fda990ac134ab5bff3fdd04dfeb20d";
//    private static final String API_KEY = "dbc824a0f5164b6a97ef7c9c828e350c";
    //http://www.tuling123.com/openapi/api?key=dbc824a0f5164b6a97ef7c9c828e350c&info=你好啊
    //发送消息得到消息
    public static ChatMessage sendMessage(String msg) {
        ChatMessage chatMessage = new ChatMessage();
        String jsonRes = doGet(msg);
        Gson gson = new Gson();
        ResultBean resultBean = null;
        try {
            resultBean = gson.fromJson(jsonRes, ResultBean.class);
            chatMessage.setMsg(resultBean.getText());
        }catch (Exception e){
//            chatMessage.setMsg("服务器繁忙，请稍等一下");
        }
        chatMessage.setDate(new Date());
        chatMessage.setType(ChatMessage.Type.INCOMING);
        return chatMessage;
    }

    public static String doGet(String msg) {
        String result = "";
        String url = setParams(msg);
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            URL urlNet = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlNet.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");

            is = conn.getInputStream();
            int len = -1;
            byte[] buffer = new byte[1024];
            baos = new ByteArrayOutputStream();
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            result = new String(baos.toByteArray());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String setParams(String msg) {
        String url = "";
        try {
            url = TULing_URL + "?key=" + API_KEY + "&info=" + URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }


}

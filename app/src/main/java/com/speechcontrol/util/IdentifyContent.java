package com.speechcontrol.util;

/**
 * Created by smartOrange_4 on 2017/10/25.
 */

public class IdentifyContent {

    private String WordJudgment(String s) {
        String choiceText = "";
        if (s.contains("钱")) {
            choiceText = "钱币识别";
        } else if (s.contains("谁")) {
            choiceText = "人脸识别";
        } else if (s.contains("什么东西")) {
            choiceText = "商品识别";
        } else if (s.contains("牌")) {
            choiceText = "LOGO识别";
        } else
            choiceText = s;

        return "您选择了：" + choiceText;

    }

}

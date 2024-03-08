package com.carpooling.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;


//屏蔽敏感词初始化
@SuppressWarnings({"rawtypes", "unchecked"})
public class SensitiveWordInit {
    // 字符编码
    private String ENCODING = "UTF-8";

    // 初始化敏感字库
    public Map initKeyWord() {
        // 读取敏感词库 ,存入Set中
        Set<String> wordSet = readSensitiveWordFile();
        // 将敏感词库加入到HashMap中//确定有穷自动机DFA
        return addSensitiveWordToHashMap(wordSet);
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
     * 中 = {
     * isEnd = 0
     * 国 = {
     * isEnd = 1
     * 人 = {isEnd = 0
     * 民 = {isEnd = 1}
     * }
     * 男  = {
     * isEnd = 0
     * 人 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     * 五 = {
     * isEnd = 0
     * 星 = {
     * isEnd = 0
     * 红 = {
     * isEnd = 0
     * 旗 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     */


    // 读取敏感词库 ,存入HashMap中
    private Set<String> readSensitiveWordFile() {
        Set<String> wordSet = null;
        //敏感词库
        ClassLoader classLoader = SensitiveWordInit.class.getClassLoader();
        URL resource = classLoader.getResource("censorwords.txt");
        File file = new File(resource.getFile());
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    file), ENCODING);
            if (file.isFile() && file.exists()) {
                wordSet = new HashSet<String>();
                BufferedReader br = new BufferedReader(read);
                String txt = null;
                // 读取文件，将文件内容放入到set中
                while ((txt = br.readLine()) != null) {
                    wordSet.add(txt);
                }
                br.close();
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordSet;
    }


    // 将HashSet中的敏感词,存入HashMap中
    private Map addSensitiveWordToHashMap(Set<String> wordSet) {
        Map wordMap = new HashMap(wordSet.size());
        for (String word : wordSet) {
            Map nowMap = wordMap;
            for (int i = 0; i < word.length(); i++) {
                char keyChar = word.charAt(i);
                Object tempMap = nowMap.get(keyChar);
                if (tempMap != null) {
                    nowMap = (Map) tempMap;
                } else {
                    // 设置标志位
                    Map<String, String> newMap = new HashMap<>();
                    newMap.put("isEnd", "0");
                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return wordMap;
    }

    public static void main(String[] args) {
        //需要屏蔽哪些字就在censorword.txt文档内添加即可


        SensitiveFilterService filter = SensitiveFilterService.getInstance();

        String txt = "";

        Scanner scanner = new Scanner(System.in);

        while (!"exit".equals(txt = scanner.next())) {
            String hou = filter.replaceSensitiveWord(txt, 1, "*");
            System.out.println("替换前的文字为：" + txt);
            System.out.println("替换后的文字为：" + hou);
            boolean check = filter.checkContainCount(txt);
            System.out.println("是否存在关键词：" + check);
            //获得包含的关键词 逗号隔开
            String str = filter.returnSensitiveWord(txt);
            System.out.println("所包含的关键词：" + str);
        }


    }

}

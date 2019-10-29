package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qhhu
 * @date 2019/10/28 - 23:01
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符号
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 注解@PostConstruct标记该方法为初始化方法
    // 服务启动, 容器实例化SensitiveFilter后(在调用构造器后), 自动调用该方法
    // 通过读取文件中的敏感词来初始化前缀树
    @PostConstruct
    public void init() {
        try (
                // 读取文件, 在try中开启或创建某个对象, 编译时自动加上finally将其关闭
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加前缀树
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }

    }

    // 将一个敏感词添加到前缀树中
    private void addKeyWord(String keyword) {
        TrieNode curNode = rootNode;
        for (char chr : keyword.toCharArray()) {
            TrieNode subNode = rootNode.getSubNode(chr);

            // 若无当前子节点, 则初始化当前子节点
            if (subNode == null) {
                subNode = new TrieNode();
                curNode.addSubNode(chr, subNode);
            }

            // 指向子节点, 继续循环
            curNode = subNode;
        }

        // 设置结束标识
        curNode.setKeyWordEnd(true);
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode curNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder(); // 结果

        // 这样应该是默认任意敏感词中没有其他敏感词, 所以当position到达结尾时结束算法
        while (position < text.length()) {
            char chr = text.charAt(position);

            // 跳过符号
            if (isSymbol(chr)) {
                // 若符号在敏感词中间, 则跳过该符号; 不在则加入结果
                if (begin == position) {
                    sb.append(chr);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查当前节点
            curNode = curNode.getSubNode(chr);
            if (curNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                begin++;
                position = begin;
                // 重新指向根节点
                curNode = rootNode;
            } else if (curNode.isKeyWordEnd) {
                // 发现敏感词, 将[begin, position]字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                position++;
                begin = position;
                // 重新指向根节点
                curNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 当position到达结尾时结束算法, 最后一批[begin, position]字符未计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断字符是否是符号
    private boolean isSymbol(char chr) {
        // [0x2E80, 0x9FFF]是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(chr) && (chr < 0x2E80 || chr > 0x9FFF);
    }

    // 前缀树节点类
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeyWordEnd = false;

        // 所有子节点(路径上保存的是字符, key是下级字符, value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character character, TrieNode trieNode) {
            subNodes.put(character, trieNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character character) {
            return subNodes.get(character);
        }
    }

}

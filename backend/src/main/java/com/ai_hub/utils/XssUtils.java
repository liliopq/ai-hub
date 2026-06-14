package com.ai_hub.utils;

import org.springframework.stereotype.Component;

/**
 * XSS 防护工具类
 * 用于对用户输入进行 HTML 转义，防止 XSS 攻击
 */
@Component
public class XssUtils {

    /**
     * HTML 特殊字符映射表
     */
    private static final String[][] HTML_ESCAPE_TABLE = {
            {"&", "&amp;"},
            {"<", "&lt;"},
            {">", "&gt;"},
            {"\"", "&quot;"},
            {"'", "&#x27;"},
            {"/", "&#x2F;"}
    };

    /**
     * 对字符串进行 HTML 转义
     * 
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        for (String[] entry : HTML_ESCAPE_TABLE) {
            result = result.replace(entry[0], entry[1]);
        }
        return result;
    }

    /**
     * 对字符串进行 HTML 反转义
     * 
     * @param input 输入字符串
     * @return 反转义后的字符串
     */
    public static String unescapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        // 反向遍历，先转换 &amp; 再转换其他
        for (int i = HTML_ESCAPE_TABLE.length - 1; i >= 0; i--) {
            result = result.replace(HTML_ESCAPE_TABLE[i][1], HTML_ESCAPE_TABLE[i][0]);
        }
        return result;
    }

    /**
     * 移除潜在的 XSS 脚本标签
     * 
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String removeScript(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        // 移除 script 标签及其内容
        result = result.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        // 移除事件处理器属性
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*\\S+", "");
        // 移除 javascript: 协议
        result = result.replaceAll("(?i)javascript\\s*:", "");
        // 移除 data: 协议
        result = result.replaceAll("(?i)data\\s*:\\s*text/html", "");
        
        return result;
    }
}

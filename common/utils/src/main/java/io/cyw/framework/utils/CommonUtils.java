package io.cyw.framework.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

    public static final String EXPRESSION_BRACE = "{}";

    /**
     * 将字符串中的'{}'按照数组中的值依次替换
     *
     * @param messagePattern 待替换的字符串
     * @param argArray       替换数据的数组
     * @return
     */
    public static String arrayFormat(String messagePattern, Object[] argArray) {
        if (argArray != null && argArray.length > 0) {
            for (Object arg : argArray) {
                messagePattern = StringUtils.replaceOnce(messagePattern, EXPRESSION_BRACE, ArrayUtils.toString(arg));
            }
        }
        return messagePattern;
    }

    public static String toString(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof String) {
            return (String) object;
        }

        return object.toString();
    }
}
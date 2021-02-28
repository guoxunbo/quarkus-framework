package io.cyw.framework.core.i18n;

import io.cyw.framework.utils.CommonUtils;
import io.cyw.framework.utils.lang.StringPool;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;
import java.util.function.Supplier;

public class I18nUtils {

    /**
     * 根据源的数据去加载对应的国际化数据
     *
     * @return
     */
    public static Function<Supplier<String>, Supplier<String>> load() {
        // todo 加载的方式可能从db  文件，支持切换加载引擎
        return supplier -> supplier;
    }

    public static Function<Supplier<String>, Supplier<String>> args(final Object... args) {
        return supplier -> () -> {
            String message = supplier.get();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    message = StringUtils.replaceOnce(message, StringPool.LEFT_BRACE + StringPool.RIGHT_BRACE,
                                                      CommonUtils.toString(arg));
                }
            }
            return message;
        };
    }

}

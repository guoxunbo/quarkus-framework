package io.cyw.framework.core.exception;

import io.cyw.framework.utils.lang.NonNull;
import io.cyw.framework.utils.lang.Nullable;

public class SystemIllegalException extends IllegalStateException {
    private static final long serialVersionUID = -3807222391429108353L;

    /**
     * message id 异常消息ID
     *
     * <p>可以从持久化的消息表中查出指定的ID</p>
     * <p>没有作为构建参数，对于基础异常，不能全依赖msgId查询，更多的时候只有message</p>
     */
    @Nullable
    private String msgId;

    /**
     * exception code 异常代码
     */
    @NonNull
    private int code;

    public SystemIllegalException(String s) {
        super(s);
    }

    /**
     * 系统运行时异常
     *
     * @param s     异常消息
     * @param cause 故障问题
     */
    public SystemIllegalException(String s, Throwable cause) {
        super(s, cause);
        this.code = ErrorCode.UNKNOW;
    }

    /**
     * 系统运行时异常
     *
     * @param code 异常代码
     * @param s    异常消息
     */
    public SystemIllegalException(int code, String s) {
        super(s);
        this.code = code;
    }

    /**
     * 系统运行时异常
     *
     * @param code  异常代码
     * @param s     异常消息
     * @param cause 故障问题
     */
    public SystemIllegalException(int code, String s, Throwable cause) {
        super(s, cause);
        this.code = code;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getCode() {
        return code;
    }

}

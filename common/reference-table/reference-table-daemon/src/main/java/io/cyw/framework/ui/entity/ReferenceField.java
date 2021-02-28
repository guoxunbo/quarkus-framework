package io.cyw.framework.ui.entity;

import io.cyw.framework.utils.lang.StringPool;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 页面栏位显示
 * Created by guoxunbo on 2018/2/7.
 */
@Entity
@Table(name = "NB_FIELD")
public class ReferenceField {
    private static final long serialVersionUID = 2536500351356282982L;

    public static final Long DEFAULT_DISPLAY_LENGTH = 200L;

    public static final String DISPLAY_TYPE_TEXT = "text";
    public static final String DISPLAY_TYPE_PASSWORD = "password";
    public static final String DISPLAY_TYPE_INT = "int";
    public static final String DISPLAY_TYPE_DOUBLE = "double";
    public static final String DISPLAY_TYPE_CALENDAR = "calendar";
    public static final String DISPLAY_TYPE_CALENDAR_FROM_TO = "calendarFromTo";
    public static final String DISPLAY_TYPE_DATETIME = "datetime";
    public static final String DISPLAY_TYPE_DATETIME_FROM_TO = "datetimeFromTo";
    public static final String DISPLAY_TYPE_SYS_REF_LIST = "sysRefList";
    public static final String DISPLAY_TYPE_USER_REF_LIST = "userRefList";
    public static final String DISPLAY_TYPE_REF_TABLE = "referenceTable";
    public static final String DISPLAY_TYPE_RADIO= "radio";
    public static final String DISPLAY_TYPE_FILE = "file";

    @Column(name="NAME")
    private String name;

    @Column(name="COLUMN_NAME")
    private String columnName;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="TABLE_RRN")
    private String tableRrn;

    @Column(name="TAB_RRN")
    private String tabRrn;

    @Column(name="SEQ_NO")
    private Long seqNo;

    /**
     * 就支持100 150 200 250 300这些宽度
     */
    @Column(name="DISPLAY_LENGTH")
    private Long displayLength = DEFAULT_DISPLAY_LENGTH;

    /**
     * 栏位的显示类型
     * 如text、password、refTable等
     */
    @Column(name="DISPLAY_TYPE")
    private String displayType;

    /**
     * 栏位输入值的命名规则
     * 正则表达式
     */
    @Column(name="NAMING_RULE")
    private String namingRule;

    /**
     * 是否是关键字。可以有多个联合，表示唯一值。在保存的时候会做验证
     */
    @Column(name="KEY_FLAG")
    private String keyFlag;

    /**
     * 是否允许负数
     */
    @Column(name="NEGATIVE_FLAG")
    private String negativeFlag;

    /**
     * 栏位所对应的参考表
     */
    @Column(name="REF_TABLE_NAME")
    private String refTableName;

    /**
     * 栏位所对应的参考值名称
     */
    @Column(name="REF_LIST_NAME")
    private String refListName;

    /**
     * 栏位的参考规则 比如user.name
     */
    @Column(name="REFERENCE_RULE")
    private String referenceRule;

    /**
     * 栏位的默认值
     */
    @Column(name="DEFAULT_VALUE")
    private String defaultValue;

    /**
     * 英文标签
     */
    @Column(name="LABEL")
    private String label;

    /**
     * 中文标签
     */
    @Column(name="LABEL_ZH")
    private String labelZh;

    /**
     * 其它语言标签
     */
    @Column(name="LABEL_RES")
    private String labelRes;

    /**
     * 栏位是否显示
     */
    @Column(name="DISPLAY_FLAG")
    private String displayFlag;

    /**
     * 栏位是否显示在基本信息中
     */
    @Column(name="BASIC_FLAG")
    private String basicFlag;

    /**
     * 栏位是否在表格中显示->在表格中显示，即为导出模板栏位
     */
    @Column(name="MAIN_FLAG")
    private String mainFlag;

    /**
     * 栏位是否是只读
     */
    @Column(name="READONLY_FLAG")
    private String readonlyFlag;

    /**
     * 保存之后是否可编辑
     */
    @Column(name="EDITABLE")
    private String editable;

    /**
     * 是否占用整行
     */
    @Column(name="ALL_LINE")
    private String allLine;

    /**
     * 必输
     */
    @Column(name="REQUIRED_FLAG")
    private String requiredFlag;

    /**
     * 是否自动转换成大写
     */
    @Column(name="UPPER_FLAG")
    private String upperFlag;

    /**
     * 从父对象上取值
     * 设置了此值之后必须设置referenceRule为父对象的哪个栏位比如objectRrn
     */
    @Column(name = "FROM_PARENT")
    private String fromParent;

    /**
     * 是否是查询栏位
     */
    @Column(name="QUERY_FLAG")
    private String queryFlag;

    /**
     * 是否是查询必须
     */
    @Column(name="QUERY_REQUIRE_FLAG")
    private String queryRequireFlag;

    @Column(name="QUERY_LIKE_FLAG")
    private String queryLikeFlag;

    public Boolean getDisplayFlag() {
        return StringPool.YES.equalsIgnoreCase(displayFlag);
    }

    public void setDisplayFlag(Boolean displayFlag) {
        this.displayFlag = displayFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getBasicFlag() {
        return StringPool.YES.equalsIgnoreCase(basicFlag);
    }

    public void setBasicFlag(Boolean basicFlag) {
        this.basicFlag = basicFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getMainFlag() {
        return StringPool.YES.equalsIgnoreCase(mainFlag);
    }

    public void setMainFlag(Boolean mainFlag) {
        this.mainFlag = mainFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getReadonlyFlag() {
        return StringPool.YES.equalsIgnoreCase(readonlyFlag);
    }

    public void setReadonlyFlag(Boolean readonlyFlag) {
        this.readonlyFlag = readonlyFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getEditable() {
        return StringPool.YES.equalsIgnoreCase(editable);
    }

    public void setEditable(Boolean editable) {
        this.editable = editable ? StringPool.YES : StringPool.NO;
    }

    public Boolean getAllLine() {
        return StringPool.YES.equalsIgnoreCase(allLine);
    }

    public void setAllLine(Boolean allLine) {
        this.allLine = allLine ? StringPool.YES : StringPool.NO;
    }

    public Boolean getRequiredFlag() {
        return StringPool.YES.equalsIgnoreCase(requiredFlag);
    }

    public void setRequiredFlag(Boolean requiredFlag) {
        this.requiredFlag = requiredFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getUpperFlag() {
        return StringPool.YES.equalsIgnoreCase(upperFlag);
    }

    public void setUpperFlag(Boolean upperFlag) {
        this.upperFlag = upperFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getFromParent() {
        return StringPool.YES.equalsIgnoreCase(fromParent);
    }

    public void setFromParent(Boolean fromParent) {
        this.fromParent = fromParent ? StringPool.YES : StringPool.NO;
    }

    public Boolean getQueryFlag() {
        return StringPool.YES.equalsIgnoreCase(queryFlag);
    }

    public void setQueryFlag(Boolean queryFlag) {
        this.queryFlag = queryFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getQueryRequireFlag() {
        return StringPool.YES.equalsIgnoreCase(queryRequireFlag);
    }

    public void setQueryRequireFlag(Boolean queryRequireFlag) {
        this.queryRequireFlag = queryRequireFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getNegativeFlag() {
        return StringPool.YES.equalsIgnoreCase(negativeFlag);
    }

    public void setNegativeFlag(Boolean negativeFlag) {
        this.negativeFlag = negativeFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getQueryLikeFlag() {
        return StringPool.YES.equalsIgnoreCase(queryLikeFlag);
    }

    public void setQueryLikeFlag(Boolean queryLikeFlag) {
        this.queryLikeFlag = queryLikeFlag ? StringPool.YES : StringPool.NO;
    }

    public Boolean getKeyFlag() {
        return StringPool.YES.equalsIgnoreCase(keyFlag);
    }

    public void setKeyFlag(Boolean keyFlag) {
        this.keyFlag = keyFlag ? StringPool.YES : StringPool.NO;
    }
}

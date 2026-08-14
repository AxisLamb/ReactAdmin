package com.lain.common.constant;

public enum StatusEnum {
    /**
     * 禁用
     */
    DISABLE(0, "禁用"),

    /**
     * 启用
     */
    ENABLE(1, "启用");

    private final int code;
    private final String description;

    StatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static StatusEnum getByCode(int code) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为启用状态
     */
    public boolean isEnable() {
        return this == ENABLE;
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isDisable() {
        return this == DISABLE;
    }

    @Override
    public String toString() {
        return "StatusEnum{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}

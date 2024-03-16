package com.drake.shortlink.project.common.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ValidateTypeEnum {
    TEMPORARY(1),
    PERMANENT(0);
    public final int type;
}

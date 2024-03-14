package com.drake.shortlink.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkGroupSaveReqDTO {
    /**
     * 分组名
     */
    private String name;
}

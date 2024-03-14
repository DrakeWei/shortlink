package com.drake.shortlink.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkGroupSortReqDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 分组排序
     */
    private Integer sortOrder;
}

package com.drake.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkGroupCountQueryRespDTO {
    /**
     * 分组id
     */
    private String gid;

    /**
     * 短链接分组计数
     */
    private Integer shortLinkCount;
}

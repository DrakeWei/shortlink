package com.drake.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.admin.dao.entity.LinkAccessLogsDO;
import lombok.Data;

@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}

package com.drake.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}

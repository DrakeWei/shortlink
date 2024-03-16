package com.drake.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService {
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageQuery(RecycleBinPageReqDTO requestParam);

    void recoverShortLink(RecycleBinRecoverReqDTO requestParam);

    void removeShortLink(RecycleBinRemoveReqDTO requestParam);
}

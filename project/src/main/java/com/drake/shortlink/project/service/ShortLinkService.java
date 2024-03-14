package com.drake.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO create(ShortLinkCreateReqDTO requestParam);
}

package com.drake.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import com.drake.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO create(ShortLinkCreateReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParam);

    List<ShortLinkGroupCountQueryRespDTO> listShortLinkGroup(List<String> gid);

    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    void restoreUrl(String shortUri, ServletRequest servletRequest, ServletResponse servletResponse) throws IOException;
}

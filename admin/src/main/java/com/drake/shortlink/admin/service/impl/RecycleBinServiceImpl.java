package com.drake.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.admin.common.biz.user.UserContext;
import com.drake.shortlink.admin.common.convention.exception.ServiceException;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.dao.entity.GroupDO;
import com.drake.shortlink.admin.dao.mapper.GroupMapper;
import com.drake.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.drake.shortlink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.admin.service.RecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(RecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam);
    }
}

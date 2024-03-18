package com.drake.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.drake.shortlink.project.common.convention.exception.ClientException;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import com.drake.shortlink.project.dao.mapper.ShortLinkMapper;
import com.drake.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.project.service.RecycleBinService;
import com.drake.shortlink.project.service.ShortLinkService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.drake.shortlink.project.common.constant.RedisCacheConstant.LINK_GOTO_KEY;
import static com.drake.shortlink.project.common.constant.RedisCacheConstant.LINK_NULL_KEY;
import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.RECYCLE_RECOVER_ERROR;
import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.RECYCLE_SAVE_ERROR;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private ShortLinkMapper shortLinkMapper;

    /**
     * 将短链接移入回收站
     * @param requestParam
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(requestParam.getFullShortUrl())
                .enableStatus(1)
                .updateTime(DateTime.now())
                .build();
        try {
            shortLinkService.update()
                    .eq("gid",requestParam.getGid())
                    .eq("full_short_url",requestParam.getFullShortUrl())
                    .update(shortLinkDO);
            stringRedisTemplate.delete(LINK_GOTO_KEY+requestParam.getFullShortUrl());
        }
        catch (Exception e){
            throw new ClientException(RECYCLE_SAVE_ERROR);
        }
    }

    /**
     * 回收站分页查询
     * @param requestParam
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageQuery(RecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid,requestParam.getGidList())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .orderByDesc(ShortLinkDO::getUpdateTime);
        IPage<ShortLinkDO> selectPage = shortLinkMapper.selectPage(requestParam, lambdaQueryWrapper);
        return selectPage.convert(each-> BeanUtil.toBean(each,ShortLinkPageRespDTO.class));
    }

    /**
     * 短链接移除回收站
     * @param requestParam
     */
    @Override
    public void recoverShortLink(RecycleBinRecoverReqDTO requestParam) {
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(requestParam.getFullShortUrl())
                .enableStatus(0)
                .updateTime(DateTime.now())
                .build();
        try {
            shortLinkService.update()
                    .eq("gid",requestParam.getGid())
                    .eq("full_short_url",requestParam.getFullShortUrl())
                    .update(shortLinkDO);
            stringRedisTemplate.delete(LINK_NULL_KEY + requestParam.getFullShortUrl());
        }
        catch (Exception e){
            throw new ClientException(RECYCLE_SAVE_ERROR);
        }
    }

    /**
     * 回收站删除短链接
     * @param requestParam
     */
    @Override
    public void removeShortLink(RecycleBinRemoveReqDTO requestParam) {
        // 逻辑删除 <--?--> 物理删除
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().delFlag(1).delTime(DateTime.now()).build();
        try {
            shortLinkService.update()
                    .eq("gid",requestParam.getGid())
                    .eq("full_short_url",requestParam.getFullShortUrl())
                    .eq("enable_status",1)
                    .update(shortLinkDO);
        }
        catch (Exception e){
            throw new ClientException(RECYCLE_RECOVER_ERROR);
        }
    }
}

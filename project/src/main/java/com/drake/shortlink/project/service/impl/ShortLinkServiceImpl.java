package com.drake.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.project.common.convention.exception.ClientException;
import com.drake.shortlink.project.common.enums.ValidateTypeEnum;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import com.drake.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.drake.shortlink.project.dao.mapper.ShortLinkMapper;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.project.service.ShortLinkGotoService;
import com.drake.shortlink.project.service.ShortLinkService;
import com.drake.shortlink.project.util.HashUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.*;

@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Resource
    private RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    @Resource
    private ShortLinkGotoService shortLinkGotoService;

    /**
     * 创建短链接
     * @param requestParam
     * @return
     */
    @Override
    public ShortLinkCreateRespDTO create(ShortLinkCreateReqDTO requestParam) {
        String suffix = generateSuffix(requestParam.getOriginUrl(), requestParam.getDomain());
        if(suffix==null){
            throw new ClientException(URI_CREATE_ERROR);
        }
        /* Warning!!! 此处传参 domain 应为 nurl.ink (做了本地host映射的domain) 而不是 originUrl 自己的 domain;
           因为短链接跳转要让请求打到本机服务器!!!   */
        ShortLinkDO shortLinkDO=new ShortLinkDO();
        BeanUtil.copyProperties(requestParam,shortLinkDO);
        shortLinkDO.setShortUri(suffix);
        shortLinkDO.setFullShortUrl(requestParam.getDomain()+"/"+suffix);
        shortLinkDO.setCreateTime(DateTime.now());
        shortLinkDO.setUpdateTime(DateTime.now());
        shortLinkDO.setEnableStatus(1);
        shortLinkDO.setDelFlag(0);
        try {
            save(shortLinkDO);
        }
        //解决高并发环境下的线程安全问题，可能多个线程同时获取了同一个合法短链接
        catch (DuplicateKeyException e){
            log.error("短链接已存在！{}",suffix);
            shortLinkCreateCachePenetrationBloomFilter.add(requestParam.getDomain()+"/"+suffix);
            throw new ClientException(URL_HAS_EXIST);
        }
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(requestParam.getDomain() + "/" + suffix)
                .build();
        shortLinkGotoService.save(shortLinkGotoDO);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .fullShortUrl(requestParam.getDomain()+"/"+suffix).build();
    }

    /**
     * 短链接分页查询
     * @param requestParam
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getUpdateTime);
        IPage<ShortLinkDO> selectPage = baseMapper.selectPage(requestParam, lambdaQueryWrapper);
        return selectPage.convert(each->BeanUtil.toBean(each,ShortLinkPageRespDTO.class));
    }

    /**
     * 查询短链接分组数量
     * @param requestParam
     * @return
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listShortLinkGroup(List<String> requestParam) {
        // TODO gid应设置为全局唯一！
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("del_flag", 0)
                .eq("enable_status", 1)
                .groupBy("gid");
        List<Map<String,Object>> list = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(list,ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 修改短链接信息
     * @param requestParam
     */
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 查询原纪录
        ShortLinkDO originShortLink = query().eq("full_short_url", requestParam.getFullShortUrl()).one();
        if(originShortLink==null){
            throw new ClientException(URI_UPDATE_ERROR);
        }
        ShortLinkDO shortLinkDO=new ShortLinkDO();
        BeanUtil.copyProperties(requestParam,shortLinkDO);
        // 需判断是否修改 gid;  若未修改，则可按gid分表查询;否则gid失效，需按full_short_url查询;
        if(Objects.equals(originShortLink.getGid(),requestParam.getGid())){
            // TODO 似乎可以不修改短链接，即使修改了originUrl，也不修改短链接;
            try {
                shortLinkDO.setUpdateTime(DateTime.now());
                // 需判断是否将有效期设置为永久;  若设置为永久，validDate字段设置为空
                update().eq("gid", shortLinkDO.getGid()).update(shortLinkDO);
                UpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.update(new ShortLinkDO())
                        .eq("gid", shortLinkDO.getGid());
                if(Objects.equals(requestParam.getValidDateType(), ValidateTypeEnum.PERMANENT.type)){
                    updateWrapper.set("valid_date", null);
                }
                baseMapper.update(shortLinkDO,updateWrapper);
            }
            catch (Exception e){
                throw new ClientException(URI_UPDATE_ERROR);
            }
        }
        else{
            try {
                update().eq("gid",originShortLink.getGid()).set("del_flag",1).update();
                if(!Objects.equals(originShortLink.getOriginUrl(),requestParam.getOriginUrl())){
                    shortLinkDO.setFullShortUrl(requestParam.getOriginUrl()+shortLinkDO.getShortUri());
                }
                shortLinkDO.setDomain(originShortLink.getDomain());
                shortLinkDO.setShortUri(originShortLink.getShortUri());
                shortLinkDO.setEnableStatus(1);
                shortLinkDO.setDelFlag(0);
                shortLinkDO.setCreateTime(DateTime.now());
                shortLinkDO.setUpdateTime(DateTime.now());
                save(shortLinkDO);
            }
            catch (Exception e){
                throw new ClientException(URI_UPDATE_ERROR);
            }
        }
    }

    /**
     * 短链接跳转
     * @param shortUri
     * @param request
     * @param response
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl="https://"+serverName+"/"+shortUri;
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoService.query().eq("full_short_url", fullShortUrl).one();
        if(shortLinkGotoDO==null){
            throw new ClientException(URI_RESTORE_ERROR);
        }
        ShortLinkDO shortLinkDO = query()
                .eq("gid", shortLinkGotoDO.getGid())
                .eq("enable_status",1)
                .eq("del_flag",0)
                .eq("short_uri",shortUri)
                .one();
        if(shortLinkDO!=null){
            ((HttpServletResponse)response).sendRedirect(shortLinkDO.getOriginUrl());
        }
    }

    /**
     * 生成短链接编码
     * @param originUrl
     * @param domain
     * @return
     */
    public String generateSuffix(String originUrl,String domain){
        int generateCount=0;
        String uri;
        while(true){
            if(generateCount>10){
                return null;
            }
            String hashUrl=originUrl+System.currentTimeMillis();
            uri=HashUtil.hashToBase62(hashUrl);
            if(!shortLinkCreateCachePenetrationBloomFilter.contains(domain+"/"+uri)){
                shortLinkCreateCachePenetrationBloomFilter.add(domain+"/"+uri);
                break;
            }
            generateCount++;
        }
        return uri;
    }
}

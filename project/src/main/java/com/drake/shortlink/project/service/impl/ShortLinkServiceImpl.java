package com.drake.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.project.common.convention.exception.ClientException;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import com.drake.shortlink.project.dao.mapper.ShortLinkMapper;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.project.service.ShortLinkService;
import com.drake.shortlink.project.util.HashUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.URI_CREATE_ERROR;
import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.URL_HAS_EXIST;

@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Resource
    private RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

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
        ShortLinkDO shortLinkDO=new ShortLinkDO();
        BeanUtil.copyProperties(requestParam,shortLinkDO);
        shortLinkDO.setShortUri(suffix);
        shortLinkDO.setFullShortUrl(requestParam.getDomain()+"/"+suffix);
        shortLinkDO.setCreateTime(DateTime.now());
        shortLinkDO.setUpdateTime(DateTime.now());
        try {
            save(shortLinkDO);
        }
        //解决高并发环境下的线程安全问题，可能多个线程同时获取了同一个合法短链接
        catch (DuplicateKeyException e){
            log.error("短链接已存在！{}",suffix);
            shortLinkCreateCachePenetrationBloomFilter.add(requestParam.getDomain()+"/"+suffix);
            throw new ClientException(URL_HAS_EXIST);
        }
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .fullShortUrl(requestParam.getDomain()+"/"+suffix).build();
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

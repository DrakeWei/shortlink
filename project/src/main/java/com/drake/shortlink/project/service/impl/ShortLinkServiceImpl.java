package com.drake.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.project.common.convention.exception.ClientException;
import com.drake.shortlink.project.common.convention.exception.ServiceException;
import com.drake.shortlink.project.common.enums.ValidateTypeEnum;
import com.drake.shortlink.project.dao.entity.*;
import com.drake.shortlink.project.dao.mapper.*;
import com.drake.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.project.dto.resp.*;
import com.drake.shortlink.project.handler.CustomBlockHandler;
import com.drake.shortlink.project.service.LinkStatsTodayService;
import com.drake.shortlink.project.service.ShortLinkGotoService;
import com.drake.shortlink.project.service.ShortLinkService;
import com.drake.shortlink.project.util.HashUtil;
import com.drake.shortlink.project.util.LinkUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.drake.shortlink.project.common.constant.RedisCacheConstant.*;
import static com.drake.shortlink.project.common.convention.errorcode.BaseErrorCode.*;

@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Resource
    private RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    @Resource
    private ShortLinkGotoService shortLinkGotoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private LinkAccessStatsMapper linkAccessStatsMapper;

    @Resource
    private LinkLocaleStatsMapper linkLocaleStatsMapper;

    @Resource
    private LinkOsStatsMapper linkOsStatsMapper;

    @Resource
    private LinkBrowserStatsMapper linkBrowserStatsMapper;

    @Resource
    private LinkAccessLogsMapper linkAccessLogsMapper;

    @Resource
    private LinkDeviceStatsMapper linkDeviceStatsMapper;

    @Resource
    private LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Resource
    private LinkStatsTodayMapper linkStatsTodayMapper;

    @Resource
    private LinkStatsTodayService linkStatsTodayService;

    @Resource
    private ShortLinkGotoMapper shortLinkGotoMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // static 静态变量无法通过 @Value 注解从配置文件中读取数据！！！！！！
    @Value("${short-link.stats.locale.amap-key}")
    private String amapKey;

    /**
     * 创建短链接
     *
     * @param requestParam
     * @return
     */
    @Override
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO create(ShortLinkCreateReqDTO requestParam) throws IOException {
        // TODO 禁止生成非法网站的短链接
        String suffix = generateSuffix(requestParam.getOriginUrl(), requestParam.getDomain());
        if (suffix == null) {
            throw new ClientException(URI_CREATE_ERROR);
        }
        /* Warning!!! 此处传参 domain 应为 nurl.ink (做了本地host映射的domain) 而不是 originUrl 自己的 domain;
           因为短链接跳转要让请求打到本机服务器!!!   */
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        BeanUtil.copyProperties(requestParam, shortLinkDO);
        shortLinkDO.setShortUri(suffix);
        shortLinkDO.setFullShortUrl("nurl.ink:8001/" + suffix);
        shortLinkDO.setCreateTime(DateTime.now());
        shortLinkDO.setUpdateTime(DateTime.now());
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setDelFlag(0);
        shortLinkDO.setFavicon(getFavicon(requestParam.getFavicon()));
        shortLinkDO.setDomain("nurl.ink:8001");
        try {
            save(shortLinkDO);
        }
        //解决高并发环境下的线程安全问题，可能多个线程同时获取了同一个合法短链接
        catch (DuplicateKeyException e) {
            log.error("短链接已存在！{}", suffix);
            shortLinkCreateCachePenetrationBloomFilter.add("nurl.ink:8001/" + suffix);
            throw new ClientException(URL_HAS_EXIST);
        }
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl("nurl.ink:8001/" + suffix)
                .build();
        shortLinkGotoService.save(shortLinkGotoDO);
        stringRedisTemplate.opsForValue().set(LINK_GOTO_KEY + shortLinkGotoDO.getFullShortUrl(),
                shortLinkDO.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
        shortLinkCreateCachePenetrationBloomFilter.add("nurl.ink:8001/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .fullShortUrl("nurl.ink:8001/" + suffix).build();
    }

    /**
     * 短链接分页查询
     *
     * @param requestParam
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 查询短链接分组数量
     *
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
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> list = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(list, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 修改短链接信息
     *
     * @param requestParam
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 查询原纪录
        ShortLinkDO originShortLink = query()
                .eq("gid", requestParam.getOriginGid())
                .eq("enable_status", 0)
                .eq("del_flag", 0).one();
        if (originShortLink == null) {
            throw new ClientException(URI_UPDATE_ERROR);
        }
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        BeanUtil.copyProperties(requestParam, shortLinkDO);
        // 需判断是否修改 gid;  若未修改，则可直接修改原纪录 ;否则gid失效，需将原纪录逻辑删除并新增记录;
        if (Objects.equals(requestParam.getOriginGid(), requestParam.getGid())) {
            // 域名无法修改，由服务器决定
            try {
                shortLinkDO.setUpdateTime(DateTime.now());
                // 需判断是否将有效期设置为永久;  若设置为永久，validDate字段设置为空
                update().eq("gid", shortLinkDO.getGid()).update(shortLinkDO);
                UpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.update(new ShortLinkDO())
                        .eq("gid", shortLinkDO.getGid());
                if (Objects.equals(requestParam.getValidDateType(), ValidateTypeEnum.PERMANENT.type)) {
                    updateWrapper.set("valid_date", null);
                }
                baseMapper.update(shortLinkDO, updateWrapper);
            } catch (Exception e) {
                throw new ClientException(URI_UPDATE_ERROR);
            }
        } else {
            update().eq("gid", originShortLink.getGid())
                    .eq("full_short_url", originShortLink.getFullShortUrl())
                    .set("del_flag", 1)
                    .set("del_time", DateTime.now()).update();
            /* 如果修改了短链接分组，则还需要迁移相关业务表数据，此时可能会造成读写进程并发造成的数据不一致问题。
               设置读写锁，读写锁是一种用于管理对共享资源的访问的同步机制，允许多个线程同时读取共享资源，但在写入时保证独占访问，以确保数据的一致性和完整性。
               这种锁的使用场景适用于读操作频繁，写操作较少的情况。*/
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LINK_UPDATE_LOCK + originShortLink.getFullShortUrl());
            RLock lock = readWriteLock.writeLock();
            boolean isLock = lock.tryLock();
            if (!isLock) {
                throw new ServiceException("短链接正在被修改！");
            }
            try {
                shortLinkDO.setDomain(originShortLink.getDomain());
                shortLinkDO.setShortUri(originShortLink.getShortUri());
                shortLinkDO.setEnableStatus(0);
                shortLinkDO.setDelFlag(0);
                shortLinkDO.setCreateTime(DateTime.now());
                shortLinkDO.setUpdateTime(DateTime.now());
                shortLinkDO.setTotalPv(originShortLink.getTotalPv());
                shortLinkDO.setTotalUv(originShortLink.getTotalUv());
                shortLinkDO.setTotalUip(originShortLink.getTotalUip());
                save(shortLinkDO);
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, originShortLink.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, originShortLink.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, originShortLink.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, originShortLink.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } catch (Exception e) {
                log.error("error", e);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 短链接跳转
     *
     * @param shortUri
     * @param request
     * @param response
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // TODO 将Redis命令进行管道处理
        String serverName = request.getServerName();
        String fullShortUrl = serverName + ":8001/" + shortUri;
        // 添加缓存,提高跳转效率,缓解数据库压力
        String originalLink = stringRedisTemplate.opsForValue().get(LINK_GOTO_KEY + fullShortUrl);
        if (StrUtil.isBlank(originalLink)) {
            // 经过布隆过滤器筛选,防止缓存穿透
            if (!shortLinkCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 再加一层缓存空值判断,若存在空值,说明数据库中不存在该短链接,否则查数据库 (双重校验) 主要是为了防止布隆过滤器误判
            String isNull = stringRedisTemplate.opsForValue().get(LINK_NULL_KEY + fullShortUrl);
            if (StrUtil.isNotBlank(isNull)) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 设置分布式锁,防止缓存击穿
            RLock lock = redissonClient.getLock(LINK_GOTO_LOCK);
            boolean isLock = lock.tryLock();
            if (isLock) {
                try {
                    // 双重校验防止多线程并发重置缓存、查询空值
                    String originalLink2 = stringRedisTemplate.opsForValue().get(LINK_GOTO_KEY + fullShortUrl);
                    if (StrUtil.isNotBlank(originalLink2)) {
                        shortLinkStats(null, originalLink2, request, response);
                        ((HttpServletResponse) response).sendRedirect(originalLink2);
                        return;
                    }
                    String isNull2 = stringRedisTemplate.opsForValue().get(LINK_NULL_KEY + fullShortUrl);
                    if (StrUtil.isNotBlank(isNull2)) {
                        ((HttpServletResponse) response).sendRedirect("/page/notfound");
                        return;
                    }
                    ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoService.query().eq("full_short_url", fullShortUrl).one();
                    if (shortLinkGotoDO == null) {
                        stringRedisTemplate.opsForValue().set(LINK_NULL_KEY + fullShortUrl, "-", 10, TimeUnit.SECONDS);
                        ((HttpServletResponse) response).sendRedirect("/page/notfound");
                        return;
                    }
                    ShortLinkDO shortLinkDO = query()
                            .eq("gid", shortLinkGotoDO.getGid())
                            .eq("enable_status", 0)
                            .eq("del_flag", 0)
                            .eq("short_uri", shortUri)
                            .one();
                    if (shortLinkDO != null) {
                        if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())) {
                            stringRedisTemplate.opsForValue().set(LINK_NULL_KEY + fullShortUrl, "-", 10, TimeUnit.SECONDS);
                            // TODO 删除过期数据
                            return;
                        }
                        stringRedisTemplate.opsForValue().set(LINK_GOTO_KEY + fullShortUrl,
                                shortLinkDO.getOriginUrl(),
                                LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
                        shortLinkStats(shortLinkDO.getGid(), shortLinkDO.getFullShortUrl(), request, response);
                        ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
                    } else {
                        stringRedisTemplate.opsForValue().set(LINK_NULL_KEY + fullShortUrl, "-", 10, TimeUnit.SECONDS);
                        ((HttpServletResponse) response).sendRedirect("/page/notfound");
                    }
                } finally {
                    lock.unlock();
                }
            }
        } else {
            shortLinkStats(null, fullShortUrl, request, response);
            ((HttpServletResponse) response).sendRedirect(originalLink);
        }
    }

    /**
     * 批量创建短链接
     *
     * @param requestParam
     * @return
     */
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = create(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    /**
     * 保存短链接访问数据
     * @param linkStatsMessage
     */
    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShortLinkStats(LinkStatsMessage linkStatsMessage) {
        // 在往日志中记录时需要先得到读锁,若此时被写锁占有,无法记录
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LINK_UPDATE_LOCK + linkStatsMessage.getFullShortUrl());
        RLock lock = readWriteLock.readLock();
        boolean isLock = lock.tryLock();
        while (!isLock) {
            Thread.sleep(200);
            isLock = lock.tryLock();
        }
        try {
            // TODO 将 Redis 中缓存 fullShortUrl 的数据结构改为 HashMap
            String gid=linkStatsMessage.getGid();
            String fullShortUrl=linkStatsMessage.getFullShortUrl();
            String ip = linkStatsMessage.getIp();
            String os = linkStatsMessage.getOs();
            String browser = linkStatsMessage.getBrowser();
            String device = linkStatsMessage.getDevice();
            String network = linkStatsMessage.getNetwork();
            String cookie = linkStatsMessage.getCookie();
            boolean firstFlag= linkStatsMessage.getFirstFlag() == 1;
            if (gid == null) {
                gid = shortLinkGotoService.query().eq("full_short_url", fullShortUrl).one().getGid();
            }
            int hour = LocalDateTime.now().getHour();
            int day = LocalDateTime.now().getDayOfWeek().getValue();
            Long ipAdd = stringRedisTemplate.opsForSet().add(LINK_STATS_IP + fullShortUrl, ip);
            boolean firstIp = (ipAdd == null || ipAdd > 0);
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .pv(1)
                    .uv(firstFlag ? 1 : 0)
                    .uip(firstIp ? 1 : 0)
                    .hour(hour)
                    .date(linkStatsMessage.getDate())
                    .weekday(day)
                    .updateTime(linkStatsMessage.getUpdateTime())
                    .delFlag(0).build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localParam = new HashMap<>();
            localParam.put("key", amapKey);
            localParam.put("ip", ip);
            String json = HttpUtil.get("https://restapi.amap.com/v3/ip", localParam);
            JSONObject jsonObject = (JSONObject) JSONUtil.parse(json);
            String infoCode = jsonObject.get("infocode", String.class);
            LinkLocaleStatsDO linkLocaleStatsDO;
            String actualProvince = "未知";
            String actualCity = "未知";
            String adcode = "未知";
            if (StrUtil.isNotBlank(infoCode) && Objects.equals(infoCode, "10000")) {
                String province = jsonObject.get("province").toString();
                boolean unknown = (province == null || Objects.equals(province, "[]"));
                if (!unknown) {
                    actualProvince = province;
                    actualCity = jsonObject.get("city").toString();
                    adcode = jsonObject.get("adcode").toString();
                }
                linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .province(actualProvince)
                        .city(actualCity)
                        .adcode(adcode)
                        .country("中国")
                        .date(DateTime.now())
                        .cnt(1)
                        .updateTime(linkStatsMessage.getUpdateTime()).build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
            }
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .os(os)
                    .cnt(1)
                    .updateTime(linkStatsMessage.getUpdateTime())
                    .date(linkStatsMessage.getDate()).build();
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .cnt(1)
                    .browser(browser)
                    .date(linkStatsMessage.getDate())
                    .updateTime(linkStatsMessage.getUpdateTime()).build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(device)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(linkStatsMessage.getDate())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(network)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(linkStatsMessage.getDate())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .ip(ip)
                    .os(os)
                    .browser(browser)
                    .network(network)
                    .device(device)
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .user(cookie)
                    .updateTime(linkStatsMessage.getUpdateTime())
                    .createTime(linkStatsMessage.getCreateTime())
                    .delFlag(0).build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            baseMapper.incrementStats(gid, fullShortUrl, 1, firstFlag ? 1 : 0, firstIp ? 1 : 0);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .todayPv(1)
                    .todayUv(firstFlag ? 1 : 0)
                    .todayUip(firstIp ? 1 : 0)
                    .date(linkStatsMessage.getDate()).build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
        } catch (Exception e) {
            throw new ClientException(STATS_UPDATE_ERROR);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 短链接流量监控
     */
    public void shortLinkStats(String gid, String fullShortUrl, ServletRequest request, ServletResponse response) throws InterruptedException {
        /* 为快速响应短链接跳转，将流量监控放至后台，通过RocketMQ消息队列进行异步处理。
           对response的处理仍需在当前线程进行处理，对request中的数据处理可异步处理。 */
        // 原子性布尔变量类型，可起到线程间隔离的作用
        AtomicBoolean firstFlag = new AtomicBoolean(false);
        // 获取请求头中的 cookies 列表
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        try {
            // 创建线程任务，用于往 response 中添加cookie
            Runnable addResponseTask = () -> {
                uv.set(UUID.randomUUID().toString());
                /* 设置 cookie 的路径参数;用来过滤哪些 cookie 可以发送给服务器，哪些不发。
                   这样对应短链接的请求创建的 cookie 只会在对应短链接的请求中发送到服务器 */
                String path = StrUtil.sub(fullShortUrl, fullShortUrl.lastIndexOf('/'), fullShortUrl.length());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setPath(path);
                // 设置 cookie 的有效期
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                // 给客户端发送 cookie
                ((HttpServletResponse) response).addCookie(uvCookie);
                // 往 Redis 中缓存 cookie 作为双重校验，防止外部恶意 cookie 攻击
                stringRedisTemplate.opsForSet().add(LINK_STATS_UV + fullShortUrl, uv.get());
                firstFlag.set(true);
            };
            // 判断请求中是否包含对应短链接的 cookie
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst().map(Cookie::getValue)
                        // 如果存在 cookie
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            // 为什么有 cookie 了还要往 Redis 中缓存数据？ 防止伪造 cookie 造成监控数据不真实
                            Long uvAdd = stringRedisTemplate.opsForSet().add(LINK_STATS_UV + fullShortUrl, each);
                            firstFlag.set(uvAdd == null || uvAdd > 0);
                        }, addResponseTask);
            } else {
                addResponseTask.run();
            }
        } catch (Throwable e) {
            throw new ClientException(COOKIES_GET_ERROR);
        }
        /* 引入延迟队列,处理数据,其中消息包括以下内容 :
        fullShortUrl + gid + ip + firstFlag + os + browser + device + network + cookie + date + createTime + updateTime */
        LinkStatsMessage linkStatsMessage = LinkStatsMessage.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .firstFlag(firstFlag.get() ? 1 : 0)
                .ip(LinkUtil.getIp((HttpServletRequest) request))
                .os(LinkUtil.getOs((HttpServletRequest) request))
                .browser(LinkUtil.getBrowser((HttpServletRequest) request))
                .network(LinkUtil.getNetwork(((HttpServletRequest) request)))
                .device(LinkUtil.getDevice(((HttpServletRequest) request)))
                .cookie(uv.get())
                .date(new Date())
                .createTime(DateTime.now())
                .updateTime(DateTime.now()).build();
        String jsonStr = JSONUtil.toJsonStr(linkStatsMessage);
        Message<String> message = MessageBuilder.withPayload(jsonStr).build();
        // 生产者发送消息
        rocketMQTemplate.asyncSend("short-link-stats", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("处理成功！");
            }

            @Override
            public void onException(Throwable throwable) {
                throw new ClientException(STATS_UPDATE_ERROR);
            }
        });
    }

    /**
     * 生成短链接编码
     *
     * @param originUrl
     * @param domain
     * @return
     */
    public String generateSuffix(String originUrl, String domain) {
        int generateCount = 0;
        String uri;
        while (true) {
            if (generateCount > 10) {
                return null;
            }
            String hashUrl = originUrl + System.currentTimeMillis() + UUID.randomUUID();
            uri = HashUtil.hashToBase62(hashUrl);
            if (!shortLinkCreateCachePenetrationBloomFilter.contains(domain + "/" + uri)) {
                shortLinkCreateCachePenetrationBloomFilter.add(domain + "/" + uri);
                break;
            }
            generateCount++;
        }
        return uri;
    }

    /**
     * 获取网站图标
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String getFavicon(String url) throws IOException {
        // TODO OSS存储图标
        if (StrUtil.isBlank(url)) {
            return null;
        }
        URL targetUrl = new URL(url);
        //打开连接
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        //禁止自动处理重定向
        connection.setInstanceFollowRedirects(false);
        //设置请求方法为GET
        connection.setRequestMethod("GET");
        //连接
        connection.connect();
        //获取响应码
        int responseCode = connection.getResponseCode();
        //如果是重定位响应码
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            //获取重定向的URL
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl != null) {
                //创建新的URL对象
                URL newUrl = new URL(redirectUrl);
                //打开新的连接对象
                connection = (HttpURLConnection) newUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                //获取新的响应码
                responseCode = connection.getResponseCode();
            }
        }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            //使用Jsou库连接到URL并获取文档对象
            Document document = Jsoup.connect(url).get();
            //选择第一个匹配的<link>标签，其rel属性包含"shortcut"或"icon"
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            return faviconLink != null ? faviconLink.attr("abs:href") : null;
        }
        return null;
    }
}

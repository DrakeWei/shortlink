package com.drake.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.remote.dto.req.*;
import com.drake.shortlink.admin.remote.dto.resp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkRemoteService {
    /**
     * 远程调用短链接分页查询服务
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        Map<String, Object>map=new HashMap<>();
        map.put("gid",requestParam.getGid());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    /**
     * 远程调用短链接创建服务
     * @param requestParam
     * @return
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam){
        String result = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSONUtil.toJsonStr(requestParam));
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 远程调用短链接分组计数服务
     * @param gid
     * @return
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listShortLinkGroup(List<String> gid){
        Map<String,Object>map=new HashMap<>();
        map.put("gid",gid);
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", map);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 远程调用短链接修改服务
     *
     * @param requestParam
     */
    default void update(ShortLinkUpdateReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSONUtil.toJsonStr(requestParam));
    }

    /**
     * 远程调用短链接获取标题服务
     * @param url
     * @return
     */
    default Result<String> getTitleByUrl(String url){
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 远程调用短链接回收服务
     * @param requestParam
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSONUtil.toJsonStr(requestParam));
    }

    /**
     * 远程调用回收站分页查询服务
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLinkBin(RecycleBinPageReqDTO requestParam){
        Map<String, Object>map=new HashMap<>();
        map.put("gidList",requestParam.getGidList());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    /**
     * 远程调用移出回收站服务
     * @param requestParam
     * @return
     */
    default void recoverShortLink(RecycleBinRecoverReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSONUtil.toJsonStr(requestParam));
    }

    /**
     * 远程调用短链接删除服务
     * @param requestParam
     */
    default void removeShortLink(RecycleBinRemoveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSONUtil.toJsonStr(requestParam));
    }

    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(String fullShortUrl, String gid, String startDate, String endDate){
        Map<String, Object>map=new HashMap<>();
        map.put("gid",gid);
        map.put("fullShortUrl",fullShortUrl);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(String gid, String startDate, String endDate){
        Map<String, Object>map=new HashMap<>();
        map.put("gid",gid);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    default Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(String fullShortUrl, String gid, String startDate, String endDate){
        Map<String, Object>map=new HashMap<>();
        map.put("gid",gid);
        map.put("fullShortUrl",fullShortUrl);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    default Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(String gid, String startDate, String endDate){
        Map<String, Object>map=new HashMap<>();
        map.put("gid",gid);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        String jsonObject = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", map);
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }

    /**
     * 远程调用批量创建短链接服务
     * @param requestParam
     * @return
     */
    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam){
        String jsonObject = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", JSONUtil.toJsonStr(requestParam));
        return JSON.parseObject(jsonObject, new TypeReference<>() {
        });
    }
}

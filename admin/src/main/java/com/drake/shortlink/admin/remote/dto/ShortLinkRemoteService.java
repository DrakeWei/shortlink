package com.drake.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

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
     * @param requestParam
     * @return
     */
    default Result<Void> update(ShortLinkUpdateReqDTO requestParam){
        String result = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSONUtil.toJsonStr(requestParam));
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }
}

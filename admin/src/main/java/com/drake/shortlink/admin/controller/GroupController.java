package com.drake.shortlink.admin.controller;

import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.drake.shortlink.admin.service.GroupService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.saveGroup(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }

    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> update(@RequestBody ShortLinkGroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> delete(@RequestParam String gid){
        groupService.delete(gid);
        return Results.success();
    }

    @PostMapping("/api/short-link/admin/v1/group/order")
    public Result<Void> sort(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam){
        groupService.sort(requestParam);
        return Results.success();
    }
}

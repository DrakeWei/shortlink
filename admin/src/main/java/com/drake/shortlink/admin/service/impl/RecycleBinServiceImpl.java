package com.drake.shortlink.admin.service.impl;

import com.drake.shortlink.admin.dao.entity.GroupDO;
import com.drake.shortlink.admin.service.GroupService;
import com.drake.shortlink.admin.service.RecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    @Resource
    private GroupService groupService;

    @Override
    public List<String> getGidList(String username) {
        List<GroupDO> groupDOS = groupService.query().eq("username", username).select("gid").list();
        return groupDOS.stream().map(GroupDO::getGid).toList();
    }
}

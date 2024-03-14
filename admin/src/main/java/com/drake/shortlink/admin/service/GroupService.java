package com.drake.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drake.shortlink.admin.dao.entity.GroupDO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO>{
    void saveGroup(ShortLinkGroupSaveReqDTO requestParam);

    List<ShortLinkGroupRespDTO> listGroup();

    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    void delete(String gid);

    void sort(List<ShortLinkGroupSortReqDTO> requestParam);
}

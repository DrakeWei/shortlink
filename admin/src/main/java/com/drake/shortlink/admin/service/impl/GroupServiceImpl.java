package com.drake.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.admin.common.biz.user.UserContext;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.dao.entity.GroupDO;
import com.drake.shortlink.admin.dao.mapper.GroupMapper;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.drake.shortlink.admin.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.GID_HAS_EXIST;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    /**
     * 新增短链接分组
     * @param requestParam
     */
    @Override
    public void saveGroup(ShortLinkGroupSaveReqDTO requestParam) {
        // TODO 需判断当前用户是否已创建过同名的短链接分组
        GroupDO hasGid = query().eq("username", UserContext.getUsername()).eq("name", requestParam.getName()).one();
        if(hasGid != null){
            throw new ClientException(GID_HAS_EXIST);
        }
        String gid;
        while(true){
            gid = RandomUtil.randomString(6);
            GroupDO groupDO = query().eq("gid", gid).one();
            if(groupDO==null){
                break;
            }
        }
        GroupDO groupDO=new GroupDO();
        groupDO.setGid(gid);
        groupDO.setName(requestParam.getName());
        groupDO.setSortOrder(0);
        groupDO.setCreateTime(DateTime.now());
        groupDO.setUpdateTime(DateTime.now());
        groupDO.setDelFlag(0);
        save(groupDO);
    }

    /**
     * 查询短链接分组
     * @return
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        List<GroupDO> groupDOS = query().eq("username", UserContext.getUsername()).eq("del_flag",0).orderByDesc(List.of("sort_order","update_time")).list();
        if(groupDOS==null||groupDOS.isEmpty()){
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
    }

    /**
     * 更新短链接分组
     * @param requestParam
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        GroupDO groupDODO=new GroupDO();
        BeanUtil.copyProperties(requestParam,groupDODO);
        groupDODO.setUpdateTime(DateTime.now());
        update().eq("gid",requestParam.getGid()).eq("del_flag",0).update(groupDODO);
    }

    /**
     * 删除短链接分组
     * @param gid
     */
    @Override
    public void delete(String gid) {
        update().eq("gid",gid).eq("del_flag",0).set("del_flag",1);
    }

    @Override
    public void sort(List<ShortLinkGroupSortReqDTO> requestParam) {
        for (ShortLinkGroupSortReqDTO reqDTO : requestParam) {
            GroupDO groupDO = GroupDO.builder()
                    .gid(reqDTO.getGid())
                    .sortOrder(reqDTO.getSortOrder())
                    .updateTime(DateTime.now())
                    .build();
            update().eq("gid",reqDTO.getGid()).update(groupDO);
        }
    }
}

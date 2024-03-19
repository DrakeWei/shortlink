package com.drake.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.admin.common.biz.user.UserContext;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.dao.entity.GroupDO;
import com.drake.shortlink.admin.dao.mapper.GroupMapper;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.drake.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.drake.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.drake.shortlink.admin.service.GroupService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.GROUP_CREATE_LOCK;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.*;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Resource
    private ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Resource
    private RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Long groupMaxNum;

    /**
     * 新增短链接分组
     * @param requestParam
     */
    @Override
    public void saveGroup(ShortLinkGroupSaveReqDTO requestParam) {
        // TODO 需判断当前用户是否已创建过同名的短链接分组
        RLock lock = redissonClient.getLock(GROUP_CREATE_LOCK + UserContext.getUsername());
        boolean isLock = lock.tryLock();
        if (isLock){
            try {
                Long groupNum = query().eq("username", UserContext.getUsername()).count();
                //限制一个用户最多有10个分组
                if(groupNum >= groupMaxNum){
                    throw new ClientException(GROUP_CREATE_ERROR);
                }
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
                groupDO.setUsername(UserContext.getUsername());
                groupDO.setSortOrder(0);
                groupDO.setCreateTime(DateTime.now());
                groupDO.setUpdateTime(DateTime.now());
                groupDO.setDelFlag(0);
                save(groupDO);
            }
            finally {
                lock.unlock();
            }
        }
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
        List<ShortLinkGroupRespDTO> groupRespDTOList = BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class); //所有分组
        List<String> gidList = groupRespDTOList.stream().map(ShortLinkGroupRespDTO::getGid).toList();  //所有分组编号
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkActualRemoteService.listGroupShortLinkCount(gidList);  //所有分组对应编号+计数
        if(listResult==null){
            throw new ClientException(GROUP_QUERY_ERROR);
        }
        for (int i = 0; i < groupRespDTOList.size(); i++) {
            groupRespDTOList.get(i).setShortLinkCount(listResult.getData().get(i).getShortLinkCount());
        }
        return groupRespDTOList;
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

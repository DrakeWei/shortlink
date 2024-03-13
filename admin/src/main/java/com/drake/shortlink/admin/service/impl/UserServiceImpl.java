package com.drake.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.dao.entity.UserDO;
import com.drake.shortlink.admin.dao.mapper.UserMapper;
import com.drake.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.drake.shortlink.admin.dto.resp.UserRespDTO;
import com.drake.shortlink.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_REGISTER_LOCK;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.USER_NAME_EXIST_ERROR;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.USER_NOT_EXIST;

/**
 * 用户接口实现层
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,UserDO> implements UserService {

    @Resource
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO = query().eq("username", username).one();
        if(userDO==null){
            throw new ClientException(USER_NOT_EXIST);
        }
        UserRespDTO userRespDTO=new UserRespDTO();
        BeanUtils.copyProperties(userDO,userRespDTO);
        return userRespDTO;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if(hasUsername(requestParam.getUsername())){
            throw new ClientException(USER_NAME_EXIST_ERROR);
        }
        RLock lock = redissonClient.getLock(USER_REGISTER_LOCK + requestParam.getUsername());
        boolean success = lock.tryLock();
        if(success){
            try {
                UserDO userDO=new UserDO();
                BeanUtil.copyProperties(requestParam,userDO);
                userDO.setCreateTime(DateTime.now());
                userDO.setUpdateTime(DateTime.now());
                userDO.setDelFlag(0);
                boolean saveSuccess = save(userDO);
                if(saveSuccess){
                    userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                }
            }
            finally {
                lock.unlock();
            }
        }
    }
}

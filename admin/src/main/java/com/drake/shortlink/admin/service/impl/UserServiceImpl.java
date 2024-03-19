package com.drake.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.dao.entity.UserDO;
import com.drake.shortlink.admin.dao.mapper.UserMapper;
import com.drake.shortlink.admin.dto.req.UserLoginReqDTO;
import com.drake.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.drake.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.drake.shortlink.admin.dto.resp.UserRespDTO;
import com.drake.shortlink.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TOKEN;
import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_REGISTER_LOCK;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.*;

/**
 * 用户接口实现层
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Resource
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名获取用户信息
     *
     * @param username
     * @return
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO = query().eq("username", username).one();
        if (userDO == null) {
            throw new ClientException(USER_NOT_EXIST);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO, userRespDTO);
        return userRespDTO;
    }

    /**
     * 判断用户名是否已存在
     *
     * @param username
     * @return
     */
    @Override
    public Boolean hasUsername(String username) {
        //为防止缓存穿透，在数据库之前加一层布隆过滤器
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     *
     * @param requestParam
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST_ERROR);
        }
        //为防止短时间内大量相同的有效注册请求攻击数据库，设置分布式锁限流
        RLock lock = redissonClient.getLock(USER_REGISTER_LOCK + requestParam.getUsername());
        boolean success = lock.tryLock();
        if (success) {
            try {
                UserDO userDO = new UserDO();
                BeanUtil.copyProperties(requestParam, userDO);
                userDO.setCreateTime(DateTime.now());
                userDO.setUpdateTime(DateTime.now());
                userDO.setDelFlag(0);
                boolean saveSuccess;
                try {
                    saveSuccess = save(userDO);
                } catch (DuplicateKeyException e) {
                    throw new ClientException(USER_NAME_EXIST_ERROR);
                }
                if (saveSuccess) {
                    userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                } else {
                    throw new ClientException(USER_REGISTER_ERROR);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 用户修改个人信息
     *
     * @param requestParam
     */
    @Override
    public void updateInfo(UserUpdateReqDTO requestParam) {
        UserDO userDO = new UserDO();
        BeanUtil.copyProperties(requestParam, userDO);
        userDO.setUpdateTime(DateTime.now());
        boolean success = update().eq("username", requestParam.getUsername()).update(userDO);
        if (!success) {
            throw new ClientException(USER_UPDATE_ERROR);
        }
    }

    /**
     * 用户登录
     *
     * @param requestParam
     * @return token
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // TODO
        boolean contains = userRegisterCachePenetrationBloomFilter.contains(requestParam.getUsername());
        if (!contains) {
            throw new ClientException(USER_NOT_EXIST);
        }
        UserDO userDO = query()
                .eq("username", requestParam.getUsername())
                .eq("password", requestParam.getPassword())
                .eq("del_flag", 0).one();
        if (userDO == null) {
            throw new ClientException(USER_LOGIN_ERROR);
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_TOKEN + requestParam.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire(USER_LOGIN_TOKEN + requestParam.getUsername(), 3, TimeUnit.HOURS);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_TOKEN + requestParam.getUsername(), token, JSONUtil.toJsonStr(userDO));
        stringRedisTemplate.expire(USER_LOGIN_TOKEN + requestParam.getUsername(), 3, TimeUnit.HOURS);
        return new UserLoginRespDTO(token);
    }

    /**
     * 判断用户是否登录
     *
     * @param username
     * @param token
     * @return 登录状态
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        Object user = stringRedisTemplate.opsForHash().get(USER_LOGIN_TOKEN + username, token);
        return user != null;
    }

    /**
     * 用户退出登录
     *
     * @param username
     * @param token
     */
    @Override
    public void logout(String username, String token) {
        Object user = stringRedisTemplate.opsForHash().get(USER_LOGIN_TOKEN + username, token);
        if (user == null) {
            throw new ClientException(USER_NOT_LOGIN);
        }
        stringRedisTemplate.delete(USER_LOGIN_TOKEN + username);
    }
}

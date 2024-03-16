package com.drake.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drake.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.drake.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.drake.shortlink.project.service.ShortLinkGotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortLinkGotoServiceImpl extends ServiceImpl<ShortLinkGotoMapper, ShortLinkGotoDO> implements ShortLinkGotoService {
}

package com.drake.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {
    /**
     * 分组id
     */
    private String gid;

    /**
     * 排序标识
     */
    private String orderTag;
}

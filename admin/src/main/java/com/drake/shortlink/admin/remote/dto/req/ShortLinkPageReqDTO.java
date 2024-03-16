package com.drake.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.admin.dao.entity.ShortLinkDO;
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
}

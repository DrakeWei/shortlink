package com.drake.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drake.shortlink.project.dao.entity.ShortLinkDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecycleBinPageReqDTO extends Page<ShortLinkDO> {
    /**
     * gid标识列表
     */
    private List<String> gidList;
}

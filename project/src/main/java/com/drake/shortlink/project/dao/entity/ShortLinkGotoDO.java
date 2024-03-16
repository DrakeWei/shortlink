package com.drake.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_link_goto")
public class ShortLinkGotoDO {
    /**
     * id
     */
    private Long id;

    /**
     * 分组标号
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}

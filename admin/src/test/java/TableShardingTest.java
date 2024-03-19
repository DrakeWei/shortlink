public class TableShardingTest {

    private final static String SQL="CREATE TABLE `t_user_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `username` varchar(256) DEFAULT NULL COMMENT '用户名',\n" +
            "  `password` varchar(512) DEFAULT NULL COMMENT '密码',\n" +
            "  `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',\n" +
            "  `phone` varchar(128) DEFAULT NULL COMMENT '手机号',\n" +
            "  `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',\n" +
            "  `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

    private final static String SQL2="create table `t_group_%d`\n" +
            "(\n" +
            "    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "    `gid`         varchar(32)  DEFAULT NULL COMMENT '分组标识',\n" +
            "    `name`        varchar(64)  DEFAULT NULL COMMENT '分组名称',\n" +
            "    `username`    varchar(256) DEFAULT NULL COMMENT '创建分组用户名',\n" +
            "    `sort_order`  int(3)        DEFAULT NULL COMMENT '分组排序',\n" +
            "    `create_time` datetime    DEFAULT NULL COMMENT '创建时间',\n" +
            "    `update_time` datetime    DEFAULT NULL COMMENT '修改时间',\n" +
            "    `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "    PRIMARY KEY (`id`),\n" +
            "    UNIQUE KEY `idx_unique_username_gid` (gid, username) USING BTREE\n" +
            ")ENGINE=InnoDB AUTO_INCREMENT=1716734146606301186 DEFAULT CHARSET=utf8mb4;";

    private final static String SQL3="CREATE TABLE `t_link_goto_%d`(\n" +
            "      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "      `gid` varchar(32) DEFAULT 'default' COMMENT  '分组标识',\n" +
            "      `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "      PRIMARY KEY (`id`))\n" +
            "      ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private final static String SQL4="alter table t_group_%d\n" +
            "    add constraint t_group_%d_pk\n" +
            "        unique (gid);\n";

    private final static String SQL5="CREATE TABLE `t_link_stats_today_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',\n" +
            "  `date` date DEFAULT NULL COMMENT '日期',\n" +
            "  `today_pv` int(11) DEFAULT '0' COMMENT '今日PV',\n" +
            "  `today_uv` int(11) DEFAULT '0' COMMENT '今日UV',\n" +
            "  `today_uip` int(11) DEFAULT '0' COMMENT '今日IP数',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`) USING BTREE\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;;";

    private final static String SQL6="alter table t_link_%d\n" +
            "    add total_pv int(11) default 0 null comment '历史pv';\n" +
            "\n" +
            "alter table t_link_%d\n" +
            "    add total_uip int(11) default 0 null comment '历史uip';\n" +
            "\n" +
            "alter table t_link_%d\n" +
            "    add total_uv int(11) default 0 null comment '历史uv';";

    private final static String SQL7="alter table t_link_stats_today_%d\n" +
            "    drop key `idx_unique_full-short-url`;\n" +
            "\n" +
            "alter table t_link_stats_today_%d\n" +
            "    add constraint idx_unique_today_stats\n" +
            "        unique (full_short_url, gid, date);";

    private final static String SQL8="alter table t_link_%d\n" +
            "    modify enable_status tinyint(1) null comment '启用标识 0：已启用 1：未启用';";

    private final static String SQL9="alter table t_link_%d\n" +
            "    add del_time date null comment '删除时间戳';\n" +
            "\n" +
            "alter table t_link_%d\n" +
            "    drop key idx_unique_full_short_url;\n" +
            "\n" +
            "alter table t_link_%d\n" +
            "    add constraint idx_unique_full_short_url\n" +
            "        unique (full_short_url, del_time);";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL9) + "%n",i,i,i);
        }
    }
}

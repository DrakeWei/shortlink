package com.drake.shortlink.admin.service;

import java.util.List;

public interface RecycleBinService {
    List<String> getGidList(String username);
}

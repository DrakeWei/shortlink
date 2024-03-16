package com.drake.shortlink.project.service;

import java.io.IOException;

public interface UrlTitleService {
    String getTitleByUrl(String url) throws IOException;
}

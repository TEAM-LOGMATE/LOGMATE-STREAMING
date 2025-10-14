package com.logmate.streaming.global.constant.opensearch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenSearchConstant {

  public final OpenSearchServer server;
  public final OpenSearchIndex index;
}

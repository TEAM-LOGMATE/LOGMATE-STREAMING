package com.logmate.streaming.global.constant.opensearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchIndex {

  @Value("${opensearch.index.prefix}")
  public String PREFIX;

  @Value("${opensearch.index.suffix}")
  public String SUFFIX;
}

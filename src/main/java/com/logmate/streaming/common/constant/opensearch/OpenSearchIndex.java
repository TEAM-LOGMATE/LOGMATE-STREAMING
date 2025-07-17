package com.logmate.streaming.common.constant.opensearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchIndex {

  @Value("${opensearch.index.log}")
  public String LOG;
}

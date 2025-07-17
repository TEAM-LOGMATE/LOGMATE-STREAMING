package com.logmate.streaming.common.constant.opensearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchServer {

  @Value("${opensearch.server.host}")
  public String HOST;

  @Value("${opensearch.server.port}")
  public Integer PORT;

  @Value("${opensearch.server.scheme}")
  public String SCHEME;

  public String URL = HOST + ":" + PORT;
}

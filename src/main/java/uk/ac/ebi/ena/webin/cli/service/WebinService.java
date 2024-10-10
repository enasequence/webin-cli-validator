/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.ena.webin.cli.service;

import org.springframework.http.HttpHeaders;

public class WebinService {
  private final String webinRestV1Uri;
  private final String webinRestV2Uri;

  private final String userName;
  private final String password;
  private final String authToken;

  public abstract static class AbstractBuilder<T> {
    protected String webinRestV1Uri;
    protected String webinRestV2Uri;
    protected String userName;
    protected String password;
    protected String authToken;

    public AbstractBuilder<T> setWebinRestV1Uri(String webinRestV1Uri) {
      this.webinRestV1Uri = webinRestV1Uri;
      return this;
    }

    public AbstractBuilder<T> setWebinRestV2Uri(String webinRestV2Uri) {
      this.webinRestV2Uri = webinRestV2Uri;
      return this;
    }

    public AbstractBuilder<T> setUserName(String userName) {
      this.userName = userName;
      return this;
    }

    public AbstractBuilder<T> setPassword(String password) {
      this.password = password;
      return this;
    }

    public AbstractBuilder<T> setCredentials(String userName, String password) {
      setUserName(userName);
      setPassword(password);
      return this;
    }

    public AbstractBuilder<T> setAuthToken(String authToken) {
      this.authToken = authToken;
      return this;
    }

    public abstract T build();
  }

  protected WebinService(AbstractBuilder<?> builder) {
    this.webinRestV1Uri = builder.webinRestV1Uri;
    this.webinRestV2Uri = builder.webinRestV2Uri;
    this.userName = builder.userName;
    this.password = builder.password;
    this.authToken = builder.authToken;
  }

  final String getWebinRestV1Uri() {
    return webinRestV1Uri;
  }

  final String getWebinRestV2Uri() {
    return webinRestV2Uri;
  }

  public String getUserName() {
    return this.userName;
  }

  public String getPassword() {
    return this.password;
  }

  public String getAuthToken() {
    return this.authToken;
  }

  public final String resolveAgainstWebinRestV1Uri(String uri) {
    return webinRestV1Uri + uri;
  }

  public final String resolveAgainstWebinRestV2Uri(String uri) {
    return webinRestV2Uri + uri;
  }

  public HttpHeaders getAuthHeader() {
    HttpHeaders headers = new HttpHeaders();
    if (getAuthToken() != null) {
      String bearerToken = "Bearer " + getAuthToken();
      headers.set("Authorization", bearerToken);
    } else if (getUserName() != null && getPassword() != null) {
      headers.setBasicAuth(getUserName(), getPassword());
    }
    return headers;
  }
}

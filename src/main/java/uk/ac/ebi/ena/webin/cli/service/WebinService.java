/*
 * Copyright 2018-2021 EMBL - European Bioinformatics Institute
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

public class
WebinService {
    private final String webinRestUri;
    private final String webinRestSubmissionUri;

    private final String userName;
    private final String password;
    private final String authToken;

    public abstract static class
    AbstractBuilder<T> {
        protected String webinRestUri;
        protected String webinRestSubmissionUri;
        protected String userName;
        protected String password;
        protected String authToken;

        public AbstractBuilder<T>
        setWebinRestUri(String webinRestUri) {
            this.webinRestUri = webinRestUri;
            return this;
        }

        public AbstractBuilder<T>
        setWebinRestSubmissionUri(String webinRestSubmissionUri) {
            this.webinRestSubmissionUri = webinRestSubmissionUri;
            return this;
        }

        public AbstractBuilder<T>
        setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public AbstractBuilder<T>
        setPassword(String password) {
            this.password = password;
            return this;
        }

        public AbstractBuilder<T>
        setCredentials(String userName, String password) {
            setUserName(userName);
            setPassword(password);
            return this;
        }

        public AbstractBuilder<T>
        setAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public abstract T build();
    }

    protected WebinService(AbstractBuilder<?> builder) {
        this.webinRestUri = builder.webinRestUri;
        this.webinRestSubmissionUri = builder.webinRestSubmissionUri;
        this.userName = builder.userName;
        this.password = builder.password;
        this.authToken = builder.authToken;
    }

    final String getWebinRestUri() {
        return webinRestUri;
    }

    final String getWebinRestSubmissionUri() {
        return webinRestSubmissionUri;
    }

    public String
    getUserName() {
        return this.userName;
    }

    public String
    getPassword() {
        return this.password;
    }

    public String
    getAuthToken() {
        return this.authToken;
    }

    final String resolveAgainstWebinRestUri(String uri) {
        return webinRestUri + uri;
    }

    final String resolveAgainstWebinRestSubmissionUri(String uri) {
        return webinRestSubmissionUri + uri;
    }

    public HttpHeaders getAuthHeader(){
        HttpHeaders headers = new HttpHeaders();
        if(getAuthToken()!=null) {
            String bearerToken = "Bearer " + getAuthToken();
            headers.set("Authorization", bearerToken);
        }else if(getUserName() !=null && getPassword() !=null){
            headers.setBasicAuth(getUserName(),getPassword());
        }
        return headers;
    }
}

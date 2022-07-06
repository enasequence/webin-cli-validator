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
    private final static String TEST_URL = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/";
    private final static String PRODUCTION_URL = "https://www.ebi.ac.uk/ena/submit/drop-box/";

    private final static String TEST_SUBMISSION_URL = "https://wwwdev.ebi.ac.uk/ena/submit/webin-v2/";
    private final static String PRODUCTION_SUBMISSION_URL = "https://www.ebi.ac.uk/ena/submit/webin-v2/";

    private final String userName;
    private final String password;
    private final String authToken;

    private final boolean test;

    final String getWebinRestUri(String uri, boolean test) {
        return (test) ?
                TEST_URL + uri :
                PRODUCTION_URL + uri;
    }

    final String getWebinRestUri(String uri) {
        return (test) ?
                TEST_URL + uri :
                PRODUCTION_URL + uri;
    }

    final String getWebinRestSubmissionUri(String uri, boolean test) {
        return (test) ?
            TEST_SUBMISSION_URL + uri :
            PRODUCTION_SUBMISSION_URL + uri;
    }

    final String getWebinRestSubmissionUri(String uri) {
        return (test) ?
            TEST_SUBMISSION_URL + uri :
            PRODUCTION_SUBMISSION_URL + uri;
    }

    public abstract static class
    AbstractBuilder<T> {
        protected String userName;
        protected String password;
        protected String authToken;
        protected boolean test;

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
        setTest(boolean test) {
            this.test = test;
            return this;
        }

        public AbstractBuilder<T>
        setAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public abstract T build();
    }

    public String
    getUserName() {
        return this.userName;
    }

    protected WebinService(AbstractBuilder<?> builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.authToken=builder.authToken;
        this.test = builder.test;
        
    }

    public String
    getPassword() {
        return this.password;
    }

    public boolean
    getTest() {
        return this.test;
    }

    public String
    getAuthToken() {
        return this.authToken;
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

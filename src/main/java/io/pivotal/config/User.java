/*
 * Copyright (C) 2018-Present Pivotal Software, Inc. All rights reserved.
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.config;

import java.util.List;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private List<String> roles;

    public User(Map<String, Object> map) {
        username = (String) map.get("username");
        password = (String) map.get("password");
        roles = (List<String>) map.get("roles");
    }

    public boolean isClusterOperator() {
        if (roles != null && roles.contains("cluster_operator")){
            return true;
        }
        return username != null && username.equals("cluster_operator");
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}

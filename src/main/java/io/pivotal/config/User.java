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

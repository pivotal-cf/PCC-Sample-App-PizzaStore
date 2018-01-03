package io.pivotal.config;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import java.util.Properties;

@SuppressWarnings("unused")
public class UserAuthInitialize implements AuthInitialize {

    private static final String USERNAME = "security-username";
    private static final String PASSWORD = "security-password";

    private LogWriter securitylog;
    private LogWriter systemlog;

    public static AuthInitialize create() {
        return new UserAuthInitialize();
    }

    @Override
    public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
        this.systemlog = systemLogger;
        this.securitylog = securityLogger;
    }

    @Override
    public Properties getCredentials(Properties props, DistributedMember server, boolean isPeer) throws AuthenticationFailedException {

        String username = props.getProperty(USERNAME);
        if (username == null) {
            throw new AuthenticationFailedException("UserAuthInitialize: username not set.");
        }

        String password = props.getProperty(PASSWORD);
        if (password == null) {
            throw new AuthenticationFailedException("UserAuthInitialize: password not set.");
        }

        Properties properties = new Properties();
        properties.setProperty(USERNAME, username);
        properties.setProperty(PASSWORD, password);
        return properties;
    }

    @Override
    public void close() {
    }
}

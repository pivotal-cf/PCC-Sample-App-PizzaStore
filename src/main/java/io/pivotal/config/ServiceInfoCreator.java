package io.pivotal.config;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import java.util.List;
import java.util.Map;

public class ServiceInfoCreator extends CloudFoundryServiceInfoCreator<org.springframework.cloud.service.ServiceInfo> {
    public ServiceInfoCreator() {
        super(new Tags("gemfire"));
    }

    @Override
    public org.springframework.cloud.service.ServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        String id = (String) serviceData.get("name");

        Map<String, Object> credentials = getCredentials(serviceData);

        List<String> locators = (List<String>) credentials.get("locators");
        List<Map<String, Object>> users = (List<Map<String, Object>>) credentials.get("users");

        return new ServiceInfo(id, locators, users);
    }

    @Override
    public boolean accept(Map<String, Object> serviceData) {
        return containsLocators(serviceData) || super.accept(serviceData);
    }

    private boolean containsLocators(Map<String, Object> serviceData) {
        Object locators = getCredentials(serviceData).get("locators");
        return locators != null;
    }
}

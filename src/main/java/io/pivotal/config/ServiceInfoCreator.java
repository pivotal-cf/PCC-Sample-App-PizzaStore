///*
// * Copyright (C) 2018-Present Pivotal Software, Inc. All rights reserved.
// * This program and the accompanying materials are made available under
// * the terms of the under the Apache License, Version 2.0 (the "License”);
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.pivotal.config;
//
//import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
//import org.springframework.cloud.cloudfoundry.Tags;
//
//import java.util.List;
//import java.util.Map;
//
//public class ServiceInfoCreator extends CloudFoundryServiceInfoCreator<org.springframework.cloud.service.ServiceInfo> {
//    public ServiceInfoCreator() {
//        super(new Tags("gemfire"));
//    }
//
//    @Override
//    public org.springframework.cloud.service.ServiceInfo createServiceInfo(Map<String, Object> serviceData) {
//        String id = (String) serviceData.get("name");
//
//        Map<String, Object> credentials = getCredentials(serviceData);
//
//        List<String> locators = (List<String>) credentials.get("locators");
//        List<Map<String, Object>> users = (List<Map<String, Object>>) credentials.get("users");
//
//        return new ServiceInfo(id, locators, users);
//    }
//
//    @Override
//    public boolean accept(Map<String, Object> serviceData) {
//        return containsLocators(serviceData) || super.accept(serviceData);
//    }
//
//    private boolean containsLocators(Map<String, Object> serviceData) {
//        Object locators = getCredentials(serviceData).get("locators");
//        return locators != null;
//    }
//}

# Sample Spring Boot Application for VMware Tanzu GemFire For VMs (TGF4VMs)

This repo demonstrates various deployment scenarios in which an app can talk to a Tanzu GemFire For VMs (TGF4VMs) service instance.

## About this sample app

- A simple Spring Boot app which uses Spring Boot for Apache Geode & Pivotal GemFire (SBDG) and talks to a service instance and does CRUD (minus the Update) operations.
- Exposes below endpoints  
    
    -  `https://APP-URL/preheatOven`  
        
        Loads pre defined Pizzas into a GemFire region.
        
    -  `https://APP-URL/pizzas` 
    
        Gets all Pizzas from GemFire region.
        
    -  `https://APP-URL/pizzas/{name}`
    
        Gets details of a given pizza.
         
    -  `https://APP-URL/pizzas/order/{name}`
    
        Orders a given pizza. 
        (example `https://APP-URL/pizzas/order/myCustomPizza?sauce=MARINARA&toppings=CHEESE,PEPPERONI,MUSHROOM`) 
   
    -  `https://APP-URL/cleanSlate` 
        
        Deletes all Pizzas from GemFire region.

### Getting started: Run the localhost dev environment

To get started, let us run this app locally, not connecting to a GemFire service instance. Since we are using the SBDG annotation [`@EnableClusterAware`](https://docs.spring.io/spring-boot-data-geode-build/current/reference/html5/#geode-configuration-declarative-annotations-productivity-enableclusteraware), when we dont configure a service instance to talk to, the annotation redirects cache operations operations to `LOCAL` regions. Thus giving an experience of a embedded cache on the client side.  
    
To run the app, execute this command `mvn spring-boot:run` (ignore the ConnectException: Connection refused)from the root of this repo after which you should be able to hit http://localhost:8080        

## [Categories of app](#categories-of-app)
For an app that is talking to a TGF4VMs service instance, depending on where the app is running, it should fall under one of the below category.

1. **Services Foundation App**

    These are apps running on the same foundation where the Tanzu GemFire service instance is running.
    
2. **Application Foundation App**

    These are apps where the service instance and the app are running on different foundations.
    Such apps are typically running on a foundation which is dedicated for applications alone and services run in a services foundation.

3. **Off-Platform App**

    These are apps which are not running on a Tanzu Platform i.e they are not running on any Cloud Foundry Foundation.
    These apps are typically running on a standalone VM or someone's desktop and talking to a TGF4VMs service instance. 
     
 
This repo demonstrates all the above 3 by use of spring profiles.

we pick each of the scenarios mentioned above[Categories of app](#categories-of-app) and demonstrate them.

## 1. When your app is running in the same foundation as the service instance (Services Foundation app)

When your app and the service instance are in the same foundation, SBDG (Spring Boot Data Geode) with CF bind experience alleviates 
the need to do any security configuration. Credentials and TLS configurations are auto applied for such application. 

##### Steps:

1. Create a TLS enabled service instance by running `cf create-service p-cloudcache <PLAN_NAME> <SERVICE_INSTANCE> -c '{"tls":true}'`. 
Optionally, you can skip the `-c '{"tls":true}` if you dont want TLS.

2. Modify the [manifest.yml](manifest.yml#L9) and provide the name of the service instance used in the above step.

3. Build the app `mvn clean install`.

4. cf push the app from the root of the project by running `cf push`.

5. Hit the api's exposed by the app. You can get the route on which app is available by looking at the output of the previous step or by running `cf app cloudcache-pizza-store`.

6. Verify by connecting to Gemfire Service instance using the GemFire CLI `gfsh`.

As the app is bound to the service instance (via the declaration in manifest.yml or by running `cf bind`)
SBDG was able to interospect the app container to get the connection details to the service instance. SBDG
then auto configured the app to talk to the service instance. Nothing extra needs to be done to talk to a TLS
enabled service instance.  
 
## 3. When your app is running <ins>off-platform</ins>

This is the case where your app is not running on a Cloud Foundary Foundation. It could be running on your local machine 
or in a VM in the cloud.

Steps:

1. Login to Cloud Foundary CLI (CF CLI) where you want the service to create the service instance and target to the org/space where you want to start the service.

2. Create a Service Instance which has a **Service Gateway**:

   Since your app is running outside the foundation, the service instance should be created with a flag (`services-gateway`), so that it can be accessed from outside the foundation. Create the service instance as below
   `cf create-service p-cloudcache <PLAN> <SERVICE_INSTANCE_NAME> -t {"tls":true, "service_gateway":true}`. the flg `tls:true` is mandatory when to use service gateway feature. `<PLAN>` can be any of the plans defined in the tile configuration (ex: `small-footprint`,`dev-plan` etc).
   
3. Create a **service key**:
   Run `cf service-key <SERVICE_INSTANCE_NAME> <KEY_NAME>`

4. **Create truststore** so for TLS. 
   Since the service instance will be TLS enabled, app has to be able to establish a TLS connection with the service instance. For this purpose the app has to have a truststore with 2 CAs in it and below is how one can get them.
   
   4.a. Get `services/tls_ca`from credhub by running `credhub get --name="/services/tls_ca" -k ca > services_ca.crt`.
   
   4.b. Get the CA from where your TLS termination occurs and store it in a `.crt` file. If your TLS terminates at gorouter then you can get the CA from `OpsManager`-> `Settings`-> `Advanced Options` -> `Download Root CA Cert`.
   
   4.c. Create a truststore which has both the above CAs
    `keytool -importcert -file services_ca.crt -keystore mytruststore.jks -storetype JKS`
    `keytool -importcert -alias root_ca -file root_ca.crt -keystore mytruststore.jks -storetype JKS`.
    
   4.d. Move the truststore to resources directory. SBDG expects the truststore to be in one of the 3 well known locations. Details are in SBDG [docs](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.3.2.RELEASE/reference/html5/#geode-security-ssl).
      
5. **Configure the app to talk to the service instance**
     By configuring details in `application-off-platform.properties` file.  

6. **Run the app** by running `mvn spring-boot:run -Dspring-boot.run.profiles=off-platform -Dspring-boot.run.jvmArguments="-Djavax.net.ssl.trustStore=/tmp/mytruststore1.jks -Djavax.net.ssl.trustStorePassword=123456"`.

7. **Interact with the app** by hitting the endpoints at http://localhost:8080           
   
# Sample application for Pivotal Cloud Cache

Sample application for Pivotal Cloud Cache

## How to run on Pivotal Cloud Foundry (PCF)

This is a Spring Boot application which can pushed to **Pivotal Cloud Foundry(PCF)** using `cf push` command that demonstrates some of Pivotal Cloud Cache’s(PCC) interesting features.

Steps. 
1. Create the Spring Boot jar file to deploy to PCF using `./gradlew build` command
1. Call `cf push` command to push the application on PCF. A PCF `manifest.yml` file already exists at the main directory
1. Bind the application with a Pivotal Cloud Cache (PCC) service instance using command `cf bind-service APP_NAME SERVICE_INSTANCE [-c PARAMETERS_AS_JSON]`. If service instance is not created create that using `cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE`
Once application is deployed, hit the REST endpoint `<url>/healthcheck` and you should see a 200 response code, if request was successful.

## About the sample application  
Pivotal Cloud Cache (PCC) is a high-performance, high-availability caching layer for Pivotal Cloud Foundry (PCF). This is a sample application which uses PCC as a provider of NoSQL store to a Spring Boot application. 
This application is a Pizza store application that can be used to create Pizzas with different sauces.
All the Pizzas are stored in PCC clusters. It uses Spring Repository abstractions.

We have 2 types of Objects that go into PCC (`regions` in PCC/Pivotal GemFire language are analogous to a `Map<>`)
 
 - Name
 - Pizza
 
Both use the Repository pattern to write to PCC.
 
The application leverages Spring controllers that expose operations you can perform over REST.

#### REST API endpoints

 1. `/healthcheck` - Does `save()` and `findById()` on Pizza region to check if everything is setup properly. It creates 2 types of Pizzas, a Plain Pizza with *red* sauce and chicken Pizza with *white* sauce.
 1. `/pestoOrder/{name}` - Creates a Pizza with chicken and `pesto` sauce. 

#### Continuous Query 

Application has a **Continuous Query** registered on Pizza region whenever there is `pesto` sauce pizza created on region application will make a entry in the `Name` region.

PCC/Pivotal GemFire supports the notion of **Continuous Query**, which means a developer can specify any OQL pattern on `regions`
and when that condition is satisfied an event would be returned with event object. For more details see [here](https://docs.spring.io/spring-data/gemfire/docs/2.0.3.RELEASE/reference/html/#bootstrap-annotation-config-continuous-queries).  



#### Known Issues
* You must run Spring Boot version 2.0.0 or above. 
    * If using the Gradle plugin, you will need to change the Spring Boot plug-in from `spring-boot` to `org.springframework.boot`
    * If using Gradle, the version must be 4.2 or above
* The Spring Data GemFire project makes it easier to build highly scalable Spring-powered applications using Pivotal GemFire as a distributed data management platform. Spring Data Gemfire 2.0.0 uses Pivotal GemFire 9.1.1. If a different version is required, you must exclude the Pivotal GemFire library from Spring Data GemFire in your build dependencies. When the Pivotal GemFire library is excluded, each required library must be specified explicitly. 

Example for build.gradle:

```
   compile("org.springframework.data:spring-data-gemfire:$springDataGemfireVersion"){
        exclude module: "io.pivotal.gemfire"
    }
    compile("io.pivotal.gemfire:geode-core:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-cq:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-wan:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-lucene:$gemfireVersion")
    
```

*  On upgrade, you may have to resolve some compile issues. 
    *  broken imports (e.g. regions class has moved to a different package)
    *  deprecated methods (e.g. findOne from CrudRepository is deprecated) # cloudcache-pizza-store







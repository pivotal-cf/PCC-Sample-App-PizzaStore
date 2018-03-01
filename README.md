# cloudcache-pizza-store

## How to run on PCF

This is a Spring Boot application which can pushed to **Cloud Foundry** using `cf push` command

Steps. 
1. Create the Spring Boot jar file to deploy to PCF using `./gradlew build` command
1. Call `cf push` command to push the application on PCF. A PCF `manifest.yml` file already exists at the main directory
1. Bind the application with a PCC service instance using command `cf bind-service APP_NAME SERVICE_INSTANCE [-c PARAMETERS_AS_JSON]`. If service instance is not created   
create that using `cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE`
Once application is deployed, hit the REST endpoint `<url>/healthcheck` you should see a 200 response code if 

## About the application  
Sample application which uses PCC as a provider of noSql store to a Spring Boot application.
This application is a Pizza store application which can be used to create Pizzas with different sauces.
All the Pizzas are stored in PCC clusters. It uses Spring Repository abstractions.

We have 2 types of Objects that go into PCC (`regions` in PCC/GemFire language analogous to a `Map<>`)
 
 - Name
 - Pizza
 
Both use Repository pattern to write to PCC.
 
We have Spring controllers which expose operations you can perform over REST.

#### REST API end points

 1. `/healthcheck` - Does `save()` and `findById()` on Pizza region to check if everything is setup properly. It 
 creates 2 types of Pizzas, a Plain Pizza with *red* sauce and chicken Pizza with white sauce.
 1. `/pestoOrder/{name}` - Creates a Pizza with chicken and `pesto` sauce. 

#### Continuous Query 

Application has a **Continuous Query** registered on Pizza region when ever their is `pesto` sauce pizza created on region
application will make a entry in the `Name` region.

PCC/GemFire supports the notion of **Continuous Query**, which means a developer can specify any OQL pattern on `regions`
and when that condition is satisfied an event would be returned with event object. For more details see [here](https://docs.spring.io/spring-data/gemfire/docs/2.0.3.RELEASE/reference/html/#bootstrap-annotation-config-continuous-queries).  

# Upgrading to Spring Data Gemfire 2.0.0 and above

## Issues
* You must run Spring Boot version 2.0.0 or above. 
	* If using the gradle plugin, you will need to change the spring boot plug-in from `spring-boot` to `org.springframework.boot`
	* If using gradle, the version must be 4.2 or above
* Spring Data Gemfire 2.0.0 uses GemFire 9.1.1. If a different version is required, you must exclude the GemFire library from Spring Data GemFire in your build dependencies. When the GemFire library is excluded, each required library must be specified explicitly. 

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

*  On upgrade, you may have to resovle some compile issues. 
	*  broken imports (e.g. regions class has moved to a different package)
	*  deprecated methods (e.g. findOne from CrudRepository is deprecated) # cloudcache-pizza-store




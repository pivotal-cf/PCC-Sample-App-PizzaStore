# Upgrading to Spring Data Gemfire 2.0.0 and above

##Issues
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
# cloudcache-pizza-store

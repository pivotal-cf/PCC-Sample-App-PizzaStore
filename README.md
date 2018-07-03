# Sample Spring Boot application for Pivotal Cloud Cache

Sample Spring Boot application for Pivotal Cloud Cache.

## How to run on Pivotal Cloud Foundry (PCF)

This is a Spring Boot application, which can pushed to **Pivotal Cloud Foundry (PCF)** using the `cf push` command.
This app demonstrates a few of **Pivotal Cloud Cache’s (PCC)** interesting features.

Steps:

1. Build the Spring Boot Executable JAR file to deploy to PCF using the `./gradlew build` command.
1. Create the regions (see below)
1. Call the `cf push --no-start` command to push the Spring Boot application to PCF. A PCF `manifest.yml` file already exists in the project root directory.
1. Bind the Spring Boot application to a *Pivotal Cloud Cache (PCC)* service instance using the command `cf bind-service APP_NAME SERVICE_INSTANCE [-c PARAMETERS_AS_JSON]`. If a PCC service instance has not yet been created, then create the service using `cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE`.
1. Start the app: `cf start pizza-store`

Once application is successfully deployed, hit the REST API endpoint `<url>/ping` and you should see a 200-OK response code with a response of "**PONG!**".

#### Creating the Regions

Before starting the Spring Boot application, you will need to create the Regions using _Gfsh_.

After standing up the PCC service and creating a service key, connect to the cluster via _Gfsh_.

`gfsh>create region --name=Pizza --type=PARTITION_REDUNDANT`
`gfsh>create region --name=Name --type=PARTITION_REDUNDANT`

## How to run locally, against a GemFire running on your own computer
1. Build the Spring Boot Executable JAR: `$ ./gradlew clean build`
1. Start the GemFire shell: `$ gfsh`
1. Start a locator: `gfsh>start locator --name=locator1`
1. Start a server: `gfsh>start server --name=server1 --server-port=40411`
1. Create the GemFire regions:
   ```
   gfsh>create region --name=Pizza --type=PARTITION_REDUNDANT`
   gfsh>create region --name=Name --type=PARTITION_REDUNDANT`
   ```
1. Set up the environment: `$ export VCAP_APPLICATION=$( cat ./VCAP_APPLICATION.json ) ; export VCAP_SERVICES=$( cat ./VCAP_SERVICES.json )`
1. Start the app: `$ java -jar ./build/libs/PCC-Sample-App-PizzaStore-1.0.0-SNAPSHOT.jar`

## About the Sample Spring Boot Application

*Pivotal Cloud Cache (PCC)* is a high-performance, high-availability caching layer for *Pivotal Cloud Foundry (PCF)*.
This is a sample Spring Boot application using PCC as a caching provider and event source.

This Spring Boot application implements a Pizza Store that can be used to order `Pizzas` with different sauces and toppings.

All `Pizza` orders are stored in the data servers running in the PCC cluster. The app uses _Spring Data Repositories_ to store,
access, or query data stored in PCC.

The application models 2 types of Objects that will be stored in PCC.

 - Name
 - Pizza

> NOTE: `Regions` in PCC/Pivotal GemFire terminology are analogous to a RDBMS table,
but are a Key/Value store implementing `java.util.concurrent.ConcurrentMap`.

Both application domain model objects use the Repository (DAO) pattern to write to,
and access data from, PCC.

The application leverages Spring Web MVC `Controllers` to expose data access operations
you can perform over REST.

#### REST API endpoints

All REST API endpoints are accessible using HTTP GET.  This is not very RESTful, but is convenient
when accessing this app from your Web browser.

 * `GET /nukeAndPave` - Removes all data from the "_Pizza_" and "_Name_" `Regions`.

 `curl -k https://cloudcache-pizza-store.cfapps.io/nukeAndPave`

 * `GET /ping` - Responds with an HTTP status code of `200 - OK` and an HTTP message body with "PONG!" if the app is running correctly.

 `curl -k https://cloudcache-pizza-store.cfapps.io/ping`

 * `GET /preheatOven` - Loads the "_Pizza_" `Region` with pre-baked pizzas that can be queried with `/pizzas`.
 This REST API endpoint calls `Repository.save()` for each pre-baked `Pizza` and verifies the pizzas
 with the `Repository.findById(..)` on the "_Pizza_" `Region` to verify that everything was setup properly.
 It creates 3 types of pizzas: a _Plain Pizza with Tomato Sauce and Cheese Topping_, an _Alfredo, Chicken, Arugula Pizza_,
 and a _Pesto Chicken Parmesan Pizza with Cherry Tomatoes_.  Hungry yet? ;-)

 * `GET /pizzas` - Lists the currently ordered pizzas, returning a JSON array containing `Pizza` objects.
 Returns "_No Pizzas Found_" if no pizzas have been baked.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas`

 * `GET /pizzas/{name}` - Returns a single `Pizza` with the given name.  Returns "_Pizza \[name\] Not Found_"
 if no `Pizza` with the given "_name_" exists.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/plain`

 * `GET /pizzas/order/{name}\[?sauce=<sauce>\[&toppings=\<topping-1>,\<topping-2>,...,\<topping-N>]] - Bakes a `Pizza`
 of the user's choosing with an optional `sauce` (defaults to `TOMATO`) and optional `toppings` (defaults to `CHEESE`)*[]:

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/order/myCustomPizza?sauce=MARINARA&toppings=CHEESE,PEPPERONI,MUSHROOM`

 * `GET /pizzas/pestoOrder/{name}` - Bakes a Pizza with Chicken, Cherry Tomatoes, Parmesan and Pesto sauce.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/pestoOrder/myPestoPizza`

#### Continuous Query

This Spring Boot application registers 2 different **Continuous Queries** on the "_Pizza_" `Region`.

When a Pizza is ordered, the event is logged to `System.err`.

When any **Pesto Pizza** is ordered, then the CQ event with the name of the Pizza is written to the "_Name_" `Region`.

PCC/Pivotal GemFire supports the notion of **Continuous Query**, which means a developer can register interest in events.
Interest is expressed via an OQL query on `Regions` containing the data of interest.  This is ideal since the developer
can use complex criteria in a OQL query predicate with the exact data they are interested in receiving notifications for.
Thus, when a data event occurs matching the conditions expressed in the CQ query predicate, an event will be returned **with
the data**.

For more details on Pivotal GemFire CQ, see the GemFire [User Guide](http://gemfire.docs.pivotal.io/95/geode/developing/continuous_querying/chapter_overview.html).

Further detail on how to use Pivotal GemFire CQ in your Spring Boot applications are [here](https://docs.spring.io/spring-data/gemfire/docs/current/reference/html/#bootstrap-annotation-config-continuous-queries).

#### Known Issues

* You must run _Spring Boot_ version 2.0.0.RELEASE or above.
    * If using the Gradle plugin, you will need to change the Spring Boot plug-in from `spring-boot` to `org.springframework.boot`.
    * If using Gradle, the version must be 4.2 or above.
* _Spring Data for Pivotal GemFire_ makes it easy and quick to build highly-scalable, Spring-powered applications using Pivotal GemFire
as a distributed data management platform. _Spring Data for GemFire_ 2.0.x uses **Pivotal GemFire 9.1.1**. If a different version is required,
you must exclude the Pivotal GemFire library from Spring Data for Pivotal GemFire in your project build dependencies.
When the Pivotal GemFire library is excluded, each required library must be explicitly specified.

Example `build.gradle`:

```
   compile("org.springframework.data:spring-data-gemfire:$springDataGemfireVersion"){
        exclude module: "io.pivotal.gemfire"
    }
    compile("io.pivotal.gemfire:geode-core:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-cq:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-wan:$gemfireVersion")
    compile("io.pivotal.gemfire:geode-lucene:$gemfireVersion")

```

* Finally, to realize the full potential and power of Pivotal GemFire or PCC in a Spring context, use the _Spring Boot for Pivotal GemFire_
project.

```
  compile "org.springframework.geode:gemfire-spring-boot-starter:$springBootDataGemFireVersion"
```


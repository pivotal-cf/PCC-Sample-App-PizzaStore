# Sample Spring Boot Application for Pivotal Cloud Cache

This example app for Pivotal Cloud Cache (PCC) is
a Spring Boot application that can be used with
a PCC service instance,
either with or without TLS enabled for communication within
the PCC service instance.

The app implements some operations of a pizza shop.
The app leverages Spring Web MVC controllers
to expose data access operations.
This REST interface permits an app user to order pizzas with a
variety of sauces and toppings.

Pizza orders are stored in the GemFire servers running within the PCC
service instance.
The app uses _Spring Data Repositories_ to store,
access, and query data stored in PCC.
There are two repositories, called _regions_ in GemFire.
See [GemFire Basics](https://docs.pivotal.io/p-cloud-cache/index.html#GFBasics) for the briefest of introductions to GemFire,
and see [Region Design](https://docs.pivotal.io/p-cloud-cache/region-design.html) for a quick tour of GemFire regions.

This app interacts with two regions:

- The `Pizza` region represents the pizzas on order at the pizza shop.
- The `Name` region  ?? has something to do with CQs and pesto pizzas.

## Prepare to Run the Pizza App

The app may be run with or without TLS enabled for communication
within the PCC service instance.
Follow the appropriate set up procedure.

### Prepare with TLS Communication

1. Create the PCC service instance with TLS enabled:

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE -c '{"tls":true}'
    ```
1. Follow the directions in [Developing an App Under TLS](https://docs.pivotal.io/p-cloud-cache/tls-enabled-app.html)
to obtain the required Java Keystore file `truststore.jks` and place
it into app's source code.
1. Build the executable JAR file:

    ```
    $ ./gradlew build
    ```
1. Correct the `path` field within the `tls_manifest.yml` file.
1. Run:

    ```
    $ cf push --no-start -f tls_manifest.yml
    ```
1. Bind the app to the PCC service instance using the command

    ```
    $ cf bind-service APP_NAME SERVICE_INSTANCE
    ```
1. Connect to the cluster via `gfsh`. Please see [this document](https://docs.pivotal.io/p-cloud-cache/1-5/accessing-instance.html) for detailed instructions on connecting to your service instance.
1. Create the regions using `gfsh`:

    ```
    gfsh>create region --name=Pizza --type=PARTITION_REDUNDANT
    gfsh>create region --name=Name --type=PARTITION_REDUNDANT
    ```

### Prepare Without TLS Communication

1. Create the PCC service instance without TLS communication:
If a PCC service instance has not yet been created, then create a non-TLS service using 

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE
    ```
1. Build the Spring Boot executable JAR file:

    ```
    $ ./gradlew build
    ```
1. Correct the `path` field within the `manifest.yml` file.
1. Run 

    ```
    $ cf push --no-start -f manifest.yml
    ```
1. Bind the app to the PCC service instance using the command:

    ```
    $ cf bind-service APP_NAME SERVICE_INSTANCE
    ```
1. Connect to the cluster via `gfsh`.
See [this document](https://docs.pivotal.io/p-cloud-cache/1-5/accessing-instance.html) for detailed instructions on connecting to your service instance.
1. Create the regions using `gfsh`:

    ```
    gfsh>create region --name=Pizza --type=PARTITION_REDUNDANT
    gfsh>create region --name=Name --type=PARTITION_REDUNDANT
    ```

## Run the Pizza App

1. Use the PCF CLI (when logged in) to start the application with 

    ```
    $ cf start APP_NAME
    ```

### REST API endpoints

All REST API endpoints are accessible using HTTP GET.  This is not very RESTful, but is convenient
when accessing this app from your Web browser.

Get your app's url with `cf apps` then try the following endpoints:

- `GET /ping`

    Responds with an HTTP status code of `200 - OK` and an HTTP message body with "PONG!" if the app is running correctly.

    ```
    $ curl -k https://cloudcache-pizza-store.cfapps.io/ping
    ```

- `GET /preheatOven` - Loads the "_Pizza_" `Region` with pre-baked pizzas that can be queried with `/pizzas`.
 This REST API endpoint calls `Repository.save()` for each pre-baked `Pizza` and verifies the pizzas
 with the `Repository.findById(..)` on "_Pizza_" `Region` to verify that everything was setup properly.
 It creates 3 types of pizzas: a Plain Pizza with Tomato Sauce and Cheese Topping, a Alfredo, Chicken, Arugula Pizza
 and a Pesto Chicken Parmesan Pizza with Cherry Tomatoes.  Hungry yet, ;-).

- `GET /pizzas` - Lists the currently ordered pizzas, returning a JSON array containing `Pizza` objects.
 Returns "_No Pizzas Found_" if no pizzas have been baked.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas`

- `GET /pizzas/{name}` - Returns a single `Pizza` with the given name.  Returns "_Pizza \[name\] Not Found_"
 if no `Pizza` with the given "_name_" exists.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/plain`

- `GET /pizzas/order/{name}\[?sauce=<sauce>\[&toppings=\<topping-1>,\<topping-2>,...,\<topping-N>]] - Bakes a `Pizza`
 of the users choosing with an optional `sauce` (defaults to `TOMATO`) and optional `toppings` (defaults to `CHEESE`)*[]:

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/order/myCustomPizza?sauce=MARINARA&toppings=CHEESE,PEPPERONI,MUSHROOM`

- `GET /pizzas/pestoOrder/{name}` - Bakes a Pizza with Chicken, Cherry Tomatoes, Parmesan and Pesto sauce.

 `curl -k https://cloudcache-pizza-store.cfapps.io/pizzas/pestoOrder/myPestoPizza`

- **DOES NOT CURRENTLY WORK** `GET /nukeAndPave`

    Removes all data from the `Pizza` and `Name` regions.

    ```
    $  curl -k https://cloudcache-pizza-store.cfapps.io/nukeAndPave
    ```

## Continuous Query

This Spring Boot application registers two continuous queries
on the `Pizza` region.

- Whenever any pizza is ordered, the event is logged to `System.err`.

- When any pesto pizza is ordered, the CQ event with the name of
the pizza is written to the `Name` region.

PCC/Pivotal GemFire supports the notion of **Continuous Query**, which means a developer can register interests in events.
Interests are expressed with an OQL query on `Regions` containing the data interests.  This is ideal since the developer
can use complex criteria in a OQL query predicate with the exact data the developer is interested in receiving notifications for.
Thus, when data event occurs matching the conditions expressed in the CQ query predicate, then an event will be returned with
the data.

For more details, see the GemFire documentation section on [Continuous Querying](http://gemfire.docs.pivotal.io/geode/developing/continuous_querying/chapter_overview.html).

For more details on how to use Pivotal GemFire CQ in your Spring Boot applications see [Configuring Continuous Queries](https://docs.spring.io/spring-data/gemfire/docs/current/reference/html/#bootstrap-annotation-config-continuous-queries).

## Known Issues

* You must run _Spring Boot_ version 2.0.0.RELEASE or above.
    * If using the Gradle plugin, you will need to change the Spring Boot plug-in from `spring-boot` to `org.springframework.boot`.
    * If using Gradle, the version must be 4.2 or above.
* _Spring Data for Pivotal GemFire_ makes it easy and quick to build highly-scalable, Spring-powered applications using Pivotal GemFire
as a distributed data management platform. _Spring Data for GemFire_ 2.0.x uses Pivotal GemFire 9.1.1. If a different version is required,
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
project...

```
  compile "org.springframework.geode:gemfire-spring-boot-starter:$springBootDataGemFireVersion"
```

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

1. **Off-Platform App**

    These are applications which are running on a standalone VM or someone's desktop and talking to a TGF4VMs service instance. These are not running on any Cloud Foundry Foundation.
     

2. **Services Foundation App**

    These are apps running on a foundation which is dedicated for services. In this case the service instance and the app are running on the same foundation.

3. **Application Foundation App**

    These are apps running on a foundation which is dedicated for applications. In this case the service instance and the app are running on different foundation.
    
This repo demonstrates all the above 3 by use of spring profiles.

we pick each of the scenarios mentioned above[Categories of app](#categories-of-app) and demonstrate them.

## 1. When your app is running <ins>off-platform</ins>

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
   
    
 


====================================================================================================
Work In Progress below this
====================================================================================================

This sample app demonstrates various ways that an app can talk to a Tanzu GemFire For VMs (TGF4VMs) service instance.


This versioned example app for VMware Tanzu GemFire is
a Spring Boot app that can be used with
a Tanzu GemFire service instance.
That service instance may be configured either with or without TLS encryption
enabled.

The app uses [Spring Boot Data Geode](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.2.6.RELEASE/reference/htmlsingle/)
(SBDG) to talk to the Tanzu GemFire service instance.
The app provides a REST interface that lets a user view pizzas, place orders, 
and view an order.
The app leverages Spring Web MVC controllers
to expose data access operations.


Pizzas are stored in the Tanzu GemFire servers running within
the Tanzu GemFire service instance.
The app uses _Spring Data Repositories_ to store,
access, and query data stored on the servers.
The app stores data in two repositories `Pizza` and `Name` (repositories are referred to as regions in Tanzu GemFire).
See [GemFire Basics](https://docs.pivotal.io/p-cloud-cache/1-11/index.html#GFBasics) for the briefest of introductions to Tanzu GemFire,
and see [Region Design](https://docs.pivotal.io/p-cloud-cache/1-11/region-design.html) for a quick tour of Tanzu GemFire regions.

This app performs operations on two regions:

- The `Pizza` region represents the pizzas on order.
Each pizza has a unique name used as the key for the region entry.
The value portion of the key/value entry is the specification
of a sauce and any toppings on a single pizza.
- The `Name` region  is populated by the unique name associated with
the pizzas with pesto as a sauce.
A Tanzu GemFire continuous query triggers a put to the `Name` region whenever
a pizza with pesto sauce is added.
See [Continuous Querying](http://gemfire.docs.pivotal.io/geode/developing/continuous_querying/chapter_overview.html) for an extensive explanation
of Tanzu GemFire continuous queries.

Pizza sauces are one of:

- ALFREDO
- BARBECUE
- HUMMUS
- MARINARA
- PESTO
- TAPENADE
- TOMATO

Pizza toppings are any of:

- ARUGULA
- BACON
- BANANA_PEPPERS
- BLACK_OLIVES
- CHEESE
- CHERRY_TOMATOES
- CHICKEN
- GREEN_OLIVES
- GREEN_PEPPERS
- JALAPENO
- MUSHROOM
- ONIONS
- PARMESAN
- PEPPERONI
- SAUSAGE

## Prepare to Run the Pizza App

The app runs with a Tanzu GemFire service instance.
This app is versioned, and branches of this git repository correspond to
the Tanzu GemFire version that this app will work with.
Check out and run the app from the branch that matches your Tanzu GemFire
tile version.
For example, if your Tanzu GemFire service instance is version 1.11,
check out this repository's `release/1.11` branch.
This is important because the procedure to run the app differs slightly,
based on whether TLS encryption is enabled or not.
Follow the appropriate setup procedure.

### Prerequisites
1. You should be logged in to your cf environment. Please see [cf login](https://cli.cloudfoundry.org/en-US/cf/login.html)
2. The CF environment should have a Tanzu Gemfire Tile installed.

### Prepare with TLS Communication

1. Create the Tanzu GemFire service instance with TLS enabled:

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE -c '{"tls":true}'
    ```
    Note the name of your `SERVICE_INSTANCE`,
    as it will be used in the `manifest.yml` file.

2. Create the regions required by the app using `gfsh`:

    Connect to the cluster via `gfsh`. Please see [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-12/accessing-instance.html) for detailed instructions on connecting to your service instance.

    ```
    gfsh>create region --name=Pizza --type=REPLICATE
    gfsh>create region --name=Name --type=REPLICATE
    ```

3. Configure the app to use SSL by adding this property to `src/main/resources/application.properties`:

    ```
    spring.data.gemfire.security.ssl.use-default-context=true
    ```

4. Modify the `manifest.yml` file such that the service instance
is no longer commented out and has the name of
your Tanzu GemFire service instance.
If your service instance had the name `dev-instance-1`,
then the `services` portion of the `manifest.yml` file would be:

    ```
      services:
       - dev-instance-1
    ```

### Prepare Without TLS Communication

The Spring Boot framework detects whether the service instance has TLS enabled or not,
so the same manifest is used when pushing the app as is shown above.

1. Create a Tanzu GemFire service instance without enabling TLS encryption:

    ```
    cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE
    ```
    Note the name of your `SERVICE_INSTANCE`,
    as it will be used in the `manifest.yml` file.

2. Create the regions required by the app using `gfsh`:

    Connect to the cluster. See [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-12/accessing-instance.html) for detailed instructions on connecting to your service instance.

    ```
    gfsh>create region --name=Pizza --type=REPLICATE
    gfsh>create region --name=Name --type=REPLICATE
    ```

3. Modify the `manifest.yml` file such that the service instance
is no longer commented out and has the name of
your Tanzu GemFire service instance.
If your service instance had the name `dev-instance-1`,
then the `services` portion of the `manifest.yml` file would be:

    ```
      services:
       - dev-instance-1
    ```

## Build and Run the Pizza App

Build and cf push the app. With current working directory of
`PCC-Sample-App-PizzaStore`:

```
$ ./mvnw clean install
$ cf push
```

## REST API endpoints

All REST API endpoints are accessible using HTTP GET.  This is not very RESTful, but is convenient
when accessing this app from your web browser.

Run the command:

```
$ cf apps
```

to acquire your app's APP-URL.
Use the APP-URL with the following endpoints:

- `GET /ping`

    Responds with an HTTP status code of `200 - OK` and an HTTP message body
    of "PONG!" if the app is running correctly.

    ```
    $ curl -k https://APP-URL/ping
    ```

- `GET /preheatOven`

    Loads the `Pizza` region with three pre-defined pizzas.
    This REST API endpoint calls `Repository.save()` for each pizza
    and verifies the pizzas with the `Repository.findById(..)` on the
    `Pizza` region to verify that everything was set up properly.
    It creates these pizzas:

    1. tomato sauce and a cheese topping
    2. Alfredo sauce, and chicken and arugula toppings
    3. pesto sauce, and chicken, cherry tomatoes and parmesan cheese toppings
 
    Responds with an HTTP message body of "OVEN HEATED!".

    ```
    $ curl -k https://APP-URL/preheatOven
    ```

- `GET /pizzas`

    Lists the current contents of the `Pizza` region, formatted as
    a JSON array containing `Pizza` objects.
    Returns "No Pizzas Found" if the region is empty.

    ```
    $ curl -k https://APP-URL/pizzas
    ```

- `GET /pizzas/{name}`
     
    Returns the pizza with the specified name.
    Returned pizza is in JSON form.
    Returns "Pizza \[name\] Not Found"
    if no pizza with the given name exists.

    ```
    curl -k https://APP-URL/pizzas/plain
    ```

- `GET /pizzas/order/{name}\[?sauce=<sauce>\[&toppings=\<topping-1>,\<topping-2>,...,\<topping-N>]]`

    Adds a pizza order for the specified name,
    with an optional `sauce` (defaults to `TOMATO`)
    and optional `toppings` (defaults to `CHEESE`).
    Changes the pizza order if the name is already present in
    the pizzas on order.

    ```
    curl -k https://APP-URL/pizzas/order/myCustomPizza?sauce=MARINARA&toppings=CHEESE,PEPPERONI,MUSHROOM
    ```

- `GET /pizzas/pestoOrder/{name}`

    Bakes a pesto sauce pizza with chicken, cherry tomatoes, and parmesan
    cheese toppings.

    ```
    curl -k https://APP-URL/pizzas/pestoOrder/myPestoPizza
    ```

- `GET /cleanSlate`

    Removes all data from the `Pizza` and `Name` regions.

    ```
    $  curl -k https://APP-URL/cleanSlate
    ```

## Continuous Queries

A Tanzu GemFire **Continuous Query** allows an app to register interest
in events.
Interest is expressed with an OQL query on regions containing the
data interests.
This is ideal, since a developer can specify
complex criteria in an OQL query predicate for the exact data the app
is interested in receiving notifications for.
Thus, when a data event occurs matching the conditions expressed
in the query predicate,
an event will be returned with the data.

This Spring Boot app registers two continuous queries
on the `Pizza` region.

- Whenever any pizza is ordered, the event is logged to `System.err`.

- When any pesto pizza is ordered, the event triggers putting the name of
the pizza in the `Name` region.

For more details, see the Tanzu GemFire documentation section on [Continuous Querying](http://gemfire.docs.pivotal.io/geode/developing/continuous_querying/chapter_overview.html).

For more details on how to use continuous queries in your Spring Boot apps see [Configuring Continuous Queries](https://docs.spring.io/spring-data/gemfire/docs/current/reference/html/#bootstrap-annotation-config-continuous-queries).


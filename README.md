# Sample Spring Boot Application for VMware Tanzu GemFire For VMs

Depending on where your application is running, it should fall under one of the below category.

1. **Off-Platform App**

    These are applications which are running on someone's desktop or a standalone VM. These are not running on any Cloud Foundry Foundation. 

2. **Services Foundation App**

    These are apps running on a foundation which is dedicated for services. In this case the service instance and the app are running on the same foundation.

3. **Application Foundation App**

    These are apps running on a foundation which is dedicated for applications. In this case the service instance and the app are running on different foundation.
    
This repo demonstrates all the above 3 by use of spring profiles.

---------------


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


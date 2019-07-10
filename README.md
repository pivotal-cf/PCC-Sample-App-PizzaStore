# Sample Spring Boot Application for Pivotal Cloud Cache

This versioned example app for Pivotal Cloud Cache (PCC) is
a Spring Boot application that can be used with
a PCC service instance,
either with or without TLS enabled for communication within
the PCC service instance.

The app implements some operations of a pizza shop.
The app leverages Spring Web MVC controllers
to expose data access operations and uses [Spring Boot For Pivotal GemFire](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.0.0.BUILD-SNAPSHOT/reference/htmlsingle/) to talk to a PCC service instance.
This REST interface permits an app user to order pizzas with a
variety of sauces and toppings.

Pizza orders are stored in the GemFire servers running within the PCC
service instance.
The app uses _Spring Data Repositories_ to store,
access, and query data stored in PCC.
There are two repositories, called _regions_ in GemFire.
See [GemFire Basics](https://docs.pivotal.io/p-cloud-cache/1-7/index.html#GFBasics) for the briefest of introductions to GemFire,
and see [Region Design](https://docs.pivotal.io/p-cloud-cache/1-7/region-design.html) for a quick tour of GemFire regions.

This app interacts with two regions:

- The `Pizza` region represents the pizzas on order at the pizza shop.
- The `Name` region  is populated by pizzas with pesto as a sauce.
A GemFire continuous query triggers a put to the `Name` region whenever
a new pizza with pesto sauce is baked.
See [Continuous Querying](http://gemfire.docs.pivotal.io/geode/developing/continuous_querying/chapter_overview.html) for an extensive explanation
of GemFire continuous queries.

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

The app may be run with or without TLS enabled for communication
within the PCC service instance.
This app is versioned, and branches of this repository represent PCC
versions.
Check out and build the app from the branch that matches your PCC service
instance's version.
For example, if your PCC service instance is version 1.7,
check out this repository's `release/1.7` branch.
Follow the appropriate setup procedure.

### Prepare with TLS Communication

1. Create the PCC service instance with TLS enabled:

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE -c '{"tls":true}'
    ```
1. Follow the directions in [Developing an App Under TLS](https://docs.pivotal.io/p-cloud-cache/1-7/tls-enabled-app.html)
to obtain the required Java Keystore file `truststore.jks` and place
it into app's source code.
1. Check out the appropriate branch to match your PCC service instance's version,
and build the executable JAR file:

    ```
    $ ./gradlew build
    ```
1. Run:

    ```
    $ cf push --no-start -f tls_manifest.yml
    ```

    Note that the output of this `cf push` command will state the app name
    (APP_NAME in other steps).

1. Bind the app to the PCC service instance using the command

    ```
    $ cf bind-service APP_NAME SERVICE_INSTANCE
    ```
1. Connect to the cluster via `gfsh`. Please see [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-7/accessing-instance.html) for detailed instructions on connecting to your service instance.
1. Create the regions using `gfsh`:

    ```
    gfsh>create region --name=Pizza --type=REPLICATE
    gfsh>create region --name=Name --type=REPLICATE
    ```

### Prepare Without TLS Communication

1. Create the PCC service instance without TLS communication:
If a PCC service instance has not yet been created, then create a non-TLS service using 

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE
    ```
1. Check out the appropriate branch to match your PCC service instance's version,
and build the Spring Boot executable JAR file:

    ```
    $ ./gradlew build
    ```
1. Run 

    ```
    $ cf push --no-start
    ```

    Note that the output of this `cf push` command will state the app name
    (APP_NAME in other steps).

1. Bind the app to the PCC service instance using the command:

    ```
    $ cf bind-service APP_NAME SERVICE_INSTANCE
    ```
1. Connect to the cluster via `gfsh`.
See [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-7/accessing-instance.html) for detailed instructions on connecting to your service instance.
1. Create the regions using `gfsh`:

    ```
    gfsh>create region --name=Pizza --type=REPLICATE
    gfsh>create region --name=Name --type=REPLICATE
    ```

## Run the Pizza App

1. Use the PCF CLI (when logged in) to start the application with 

    ```
    $ cf start APP_NAME
    ```

### REST API endpoints

All REST API endpoints are accessible using HTTP GET.  This is not very RESTful, but is convenient
when accessing this app from your Web browser.

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
    `Pizza` region to verify that everything was setup properly.
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
     
    Returns the pizza with the given name in JSON form.
    Returns "Pizza \[name\] Not Found"
    if no pizza with the given name exists.

    ```
    curl -k https://APP-URL/pizzas/plain
    ```

- `GET /pizzas/order/{name}\[?sauce=<sauce>\[&toppings=\<topping-1>,\<topping-2>,...,\<topping-N>]]`

    Bakes a pizza of the user's specification,
    with an optional `sauce` (defaults to `TOMATO`)
    and optional `toppings` (defaults to `CHEESE`):

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


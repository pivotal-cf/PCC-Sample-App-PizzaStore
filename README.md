# Sample Spring Boot Application for Pivotal Cloud Cache

This versioned example app for Pivotal Cloud Cache (PCC) is
a Spring Boot application that can be used with
a PCC service instance configured either with or without TLS enabled.

The app uses [Spring Boot Data Geode](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.2.6.RELEASE/reference/htmlsingle/) (SBDG) to talk to the PCC service instance.
The app implements some operations of a pizza shop.
The app leverages Spring Web MVC controllers
to expose data access operations.
This REST interface permits an app user to order pizzas and view them.

Pizza orders are stored in the Tanzu GemFire servers running within the PCC
service instance.
The app uses _Spring Data Repositories_ to store,
access, and query data stored in PCC.
There are two repositories, called _regions_ in GemFire.
See [GemFire Basics](https://docs.pivotal.io/p-cloud-cache/1-11/index.html#GFBasics) for the briefest of introductions to GemFire,
and see [Region Design](https://docs.pivotal.io/p-cloud-cache/1-11/region-design.html) for a quick tour of GemFire regions.

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

The app can connect to either a TLS or non-TLS enabled PCC service instance.
This app is versioned, and branches of this repository correspond to the PCC
version that this app will work with.
Check out and build the app from the branch that matches your PCC tile version.
For example, if your PCC service instance is version 1.11,
check out this repository's `release/1.11` branch.
Follow the appropriate setup procedure.

### Prepare with TLS Communication

Note: Make sure to complete the [Prepare for TLS](https://docs.pivotal.io/p-cloud-cache/1-11/prepare-TLS.html) steps from the docs before creating a TLS or non-TLS service instance.

1. Create the PCC service instance with TLS enabled:

    ```
    $ cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE -c '{"tls":true}'
    ```
1. Create the regions required by the app using `gfsh`:

    Connect to the cluster via `gfsh`. Please see [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/PCC-VERSION/accessing-instance.html) for detailed instructions on connecting to your service instance.

    ```
    gfsh>create region --name=Pizza --type=REPLICATE
    gfsh>create region --name=Name --type=REPLICATE
    ```

1. Configure the app to use SSL by adding this property in [application.properties](src/main/resources/application.properties).
    ```
    spring.data.gemfire.security.ssl.use-default-context=true
    ```

1. Point the app to the PCC service instance by adding the service in the services section of [manifest.yml](manifest.yml) file as shown below

    ```yaml
    applications:
    - name: cloudcache-pizza-store
      path: target/PCC-Sample-App-PizzaStore-1.0.0-SNAPSHOT.jar
      buildpack: java_buildpack_offline
      random-route: true
      services:
       - dev-si
    ```

1. Build and push the app
    ```sh
    mvn clean install
    cf push
    ```


### Prepare Without TLS Communication

The Spring Boot framework detects wheter the service instance has TLS enabled or not,
so the same manifest is used when pushing the app as is shown above.

1. Create a PCC service instance without enabling TLS:
    ```
    cf create-service p-cloudcache PLAN_NAME SERVICE_INSTANCE
    ```

1. Create the regions required by the app using `gfsh`:

       Connect to the cluster via `gfsh`. Please see [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/PCC-VERSION/accessing-instance.html) for detailed instructions on connecting to your service instance.
       ```
       gfsh>create region --name=Pizza --type=REPLICATE
       gfsh>create region --name=Name --type=REPLICATE
       ```
1. Build and push the app following steps 4 & 5 above for `Prepare with TLS Communication`

#### Connect using cli
Optionally you can connect using `gfsh` to look at the service instance. Follow steps from the doc
under the section [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-11/accessing-instance.html) 


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


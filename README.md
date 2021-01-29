# Example Spring Boot Application for VMware Tanzu GemFire For VMs

This branch demonstrates deployment scenarios for an app,
and how the app's location affects communication with a Tanzu GemFire For VMs
service instance.

## About the App

This Spring Boot app uses Spring Boot for Apache Geode & Pivotal GemFire (SBDG).
The app talks to a Tanzu GemFire service instance.
The app does create, read, and delete CRUD operations on data held
in a region within service instance.

The app exposes these endpoints:  
    
-  `https://APP-URL/preheatOven`  
        
    Creates three pre-defined pizzas, which adds them to the region.
        
-  `https://APP-URL/pizzas` 
    
    Gets all pizzas from the region.
        
-  `https://APP-URL/pizzas/{name}`
    
    Gets details of a pizza specified by its name.
         
-  `https://APP-URL/pizzas/order/{name}`
    
    Orders a given pizza, which does a create operation. 
    For example: `https://APP-URL/pizzas/order/myCustomPizza?sauce=MARINARA&toppings=CHEESE,PEPPERONI,MUSHROOM` orders a pizza named `myCustomPizza`,
    which has `MARINARA` sauce and three toppings. 
   
-  `https://APP-URL/cleanSlate` 
        
    Deletes all pizzas from the region.

## Try the App in a Local Environment

This Spring Boot app can run locally, 
without having a Geode or Tanzu GemFire service instance.
Uncomment the SBDG annotation [`@EnableClusterAware`](https://docs.spring.io/spring-boot-data-geode-build/current/reference/html5/#geode-configuration-declarative-annotations-productivity-enableclusteraware)
in the application soruce file `src/main/java/io/pivotal/cloudcache/app/config/PizzaConfig.java` 
to enable redirecting cache operations operations to `LOCAL` regions
when there is no service instance to talk to.
It implements an embedded cache on the client.  

To run the app in the local environment,
 
```
mvn spring-boot:run
```
Ignore the `ConnectException: Connection refused`
from the root of this repository.
Use a web browser to talk to the app at `http://localhost:8080`.

## App Location

An app that uses a Tanzu GemFire service instance may be
located in one of three locations,
as specified in [The App's Location](https://docs.pivotal.io/p-cloud-cache/1-13/architecture.html#AppLocation).

This app demonstrates all three possibilities of app location
using Spring profiles.

## Run the App in the Same Foundation as the Service Instance (Services Foundation App)

When the app and the service instance are in the same foundation,
SBDG eliminates the need to do any security configuration.
Credentials and TLS configurations are auto applied. 

##### Run the app as a services foundation app:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-13/create-instance.html#create-SI).
The service instance may be TLS-enabled or not TLS-enabled.

2. Modify the `manifest.yml` file to provide the service instance name.
Replace `SERVICE_INSTANCE` with your noted `SERVICE-INSTANCE-NAME`.
Remove the `#` character so that the line is no longer a comment.

3. Build the app:

    ```
    $ mvn clean install
    ```

4. With a current working directory of `PCC-Sample-App-PizzaStore`,
push the app to your services foundation with a `cf push` command.
Note the app's route (`APP-URL`).

##### Use the running app:

1. Interact with the running app by hitting the endpoints exposed by the app.

2. You can use the CLI interface, gfsh, to inspect the Tanzu GemFire
or Geode cluster.
Follow the instructions in [Accessing a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-13/accessing-instance.html)
to connect to the cluster using gfsh.

## Run the App in an App Foundation

Running an app foundation app requires a service gateway.
To set up a service gateway,
follow the directions in
[Configure a Service Gateway](https://docs.pivotal.io/p-cloud-cache/1-13/configure-service-gateway.html).

#### Run the app as an app foundation app:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-13/create-instance.html#create-SI).
Provide the optional parameters for enabling TLS and specifying
the creation of a service gateway.

2. Follow these instructions to
[Create Truststore for the App](https://docs.pivotal.io/p-cloud-cache/1-13/running-app.html#app-truststore).
Note the password you set for the truststore.

3. Copy the truststore to the `resources` directory within the app source code.

4. Follow these instructions to [Create a Service Key](https://docs.pivotal.io/p-cloud-cache/1-13/accessing-instance.html#create-service-key). 

5. Edit the app's `src/main/resources/application-app-foundation.properties`
file,
and specify the properties described in [Specifying Application Properties](https://docs.pivotal.io/p-cloud-cache/1-13/running-app.html#app-properties).
Find the values needed in the service key and the truststore. 

6. Edit the app's `manifest_app_foundation.yml` file to specify the
truststore password noted when you created the truststore.

7. Build the app:

    ```
    $ mvn clean install
    ```

8. Do a `cf login` that targets the app foundation's org and space.
With a current working directory of `PCC-Sample-App-PizzaStore`,
push the app to the app foundation, specifying the manifest:

    ```
    $ cf push -f manifest_app_foundation.yml
    ```
    Note the app's route (`APP-URL`).

##### Use the running app:

1. Interact with the running app by hitting the endpoints exposed by the app.

## Run the App Off Platform

Running an app that is not on any Cloud Foundry foundation
requires a service gateway.
To set up a service gateway,
follow the directions in
[Configure a Service Gateway](https://docs.pivotal.io/p-cloud-cache/1-13/configure-service-gateway.html).

#### Run the app locally, and not on any foundation:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://docs.pivotal.io/p-cloud-cache/1-13/create-instance.html#create-SI).
Provide the optional parameters for enabling TLS and specifying
the creation of a service gateway.

2. Follow these instructions to
[Create Truststore for the App](https://docs.pivotal.io/p-cloud-cache/1-13/running-app.html#app-truststore).
Note the password you set for the truststore.

3. Copy the truststore to the `resources` directory within the app source code.

4. Follow these instructions to [Create a Service Key](https://docs.pivotal.io/p-cloud-cache/1-13/accessing-instance.html#create-service-key). 

5. Edit the app's `src/main/resources/application-off-platform.properties`
file,
and specify the properties described in [Specifying Application Properties](https://docs.pivotal.io/p-cloud-cache/1-13/running-app.html#app-properties).
Find the values needed in the service key and the truststore. 

6. Build the app:

    ```
    $ mvn clean install
    ```

5. Run the app locally:

    ```
    $ mvn spring-boot:run -Dspring-boot.run.profiles=off-platform -Dspring-boot.run.jvmArguments="-Djavax.net.ssl.trustStore=/tmp/mytruststore1.jks -Djavax.net.ssl.trustStorePassword=PASSWD-HERE"
    ```
    replacing `PASSWD-HERE` with the truststore password noted when
    you created the truststore.

##### Use the running app:

1. Interact with the running app by hitting the endpoints exposed by the app
at http://localhost:8080.

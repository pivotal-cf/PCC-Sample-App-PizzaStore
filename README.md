# Example Spring Boot Application for VMware Tanzu GemFire on Cloud Foundry

This versioned example app for VMware Tanzu GemFire is
a Spring Boot app that can be used with
a Tanzu GemFire service instance.
This branch demonstrates deployment scenarios for an app,
and how the app's location affects communication with a Tanzu GemFire on Cloud Foundry
service instance.
Branches of this git repository correspond to the Tanzu GemFire version
that this app will work with.
Check out and run the app from the branch that matches
your Tanzu GemFire tile version.
For example, if your Tanzu GemFire service instance is version 2.1,
check out this repository's release/2.1 branch.

The app uses [Spring Boot for Tanzu GemFire 2.0](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/spring-boot-for-tanzu-gemfire/2-0/gf-sb/getting-started.html) to talk to the Tanzu GemFire service instance.
The app provides a REST interface that lets a user view pizzas, place orders,
and view an order.
The app leverages Spring Web MVC controllers
to expose data access operations.

Pizzas are stored in the Tanzu GemFire servers running within
the Tanzu GemFire service instance.
The app uses a Spring Data Repository to store,
access, and query data stored on the servers.
The app stores data in the `Pizza` repository (repositories are referred to as regions in Tanzu GemFire).
See [About VMware Tanzu GemFire](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire/10-1/gf/getting_started-gemfire_overview.html) for the briefest of introductions to Tanzu GemFire,
and see [Data Regions](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire/10-1/gf/basic_config-data_regions-chapter_overview.html) for a quick tour of Tanzu GemFire regions.

## Interacting With the App

The app talks to a Tanzu GemFire service instance.
The app does create, read, and delete CRUD operations on data held
in a region within service instance.

The app exposes these endpoints:  
    
-  `https://APP-URL/`  
        
    Prints the available app endpoints.
        
-  `https://APP-URL/ping`  
        
    Prints **PONG!** if the app is running.
        
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

## Prerequisites

- The [cf CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) will be used.
- Log in to your CF environment.
- The CF environment should have a Tanzu GemFire on Cloud Foundry Tile installed.

## App Location

An app that uses a Tanzu GemFire service instance may be
located in one of three locations,
as specified in [The App's Location](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-architecture.html#AppLocation).
This app demonstrates all three possibilities of app location
using Spring profiles.

In addition, the app may be run locally,
without having a Tanzu GemFire service instance.
All four running environments are described.

## Add Tanzu GemFire to this Maven Project

1. Log in to the [Broadcom Customer Support Portal](https://support.broadcom.com/) with your customer credentials.
2. Go to the [VMware Tanzu GemFire](https://support.broadcom.com/group/ecx/productdownloads?subfamily=VMware%20Tanzu%20GemFire) downloads page, select VMware Tanzu GemFire, click Show All Releases.
3. Find the release named Click **Green Token for Repository Access** and click the **Token Download** icon on the right. This opens the instructions on how to use the GemFire artifact repository. At the top, the Access Token is provided. Click **Copy to Clipboard**. You will use this Access Token as the password.
4. Add the following repository definition to your pom.xml file:
````
<repository>
  <id>gemfire-release-repo</id>
  <name>Pivotal GemFire Release Repository</name>
  <url>https://packages.broadcom.com/artifactory/gemfire/</url>
</repository>

````
5.To access these artifacts, add an entry to your .m2/settings.xml file:

````
<settings>
  <servers>
    <server>
      <id>gemfire-release-repo</id>
      <username>EXAMPLE-USERNAME</username>
      <password>MY-PASSWORD</password>
    </server>
  </servers>
</settings>

````
Where:

- EXAMPLE-USERNAME is your **support.broadcom.com** user name.
- MY-PASSWORD is the Access Token you copied in step 3 in Prerequisites.

For more details on adding Gemfire to your project refer to [Add GemFire to Your Project](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire/10-1/gf/getting_started-add_gemfire_to_project.html).

## Run the App in a Local Environment

This Spring Boot app can run locally, without having a Tanzu GemFire service instance.
Before running the app, bring up a local gemfire cluster and create a `Pizza` Region.

To run the app in the local environment,
 
```
$ ./mvnw spring-boot:run
```
Ignore the `ConnectException: Connection refused`
from the root of this repository.

##### Use the running app:

Use a web browser to talk to the app at `http://localhost:8080`.

## Run the App in the Same Foundation as the Service Instance (Services Foundation App)

When the app and the service instance are in the same foundation,
SpringBoot for Gemfire eliminates the need to do any security configuration.
Credentials and TLS configurations are auto applied. 

##### Run the app as a services foundation app:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-create-instance.html#create-SI).
The service instance may be TLS-enabled or not TLS-enabled.

2. Modify the `manifest.yml` file to provide the service instance name.
Replace `SERVICE_INSTANCE` with your noted `SERVICE-INSTANCE-NAME`.
Remove the `#` character so that the line is no longer a comment.

3. Build the app:

    ```
    $ ./mvnw clean install
    ```

4. With a current working directory of `PCC-Sample-App-PizzaStore`,
push the app to your services foundation with a `cf push` command.
Note the app's route (`APP-URL`).

##### Use the running app:

Interact with the running app by hitting the endpoints exposed by the app.

You can use gfsh to inspect the service instance.
Follow the instructions in [Accessing a Service Instance](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-accessing-instance.html)
to connect to the cluster using gfsh.

## Run the App in an App Foundation

Running an app foundation app requires a service gateway.
To set up a service gateway,
follow the directions in
[Configure a Service Gateway](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-configure-service-gateway.html).

#### Run the app as an app foundation app:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-create-instance.html#create-SI).
Provide the optional parameters for enabling TLS and specifying
the creation of a service gateway.

2. Follow these instructions to
[Create Truststore for the App](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-running-app.html#app-truststore).
Note the password you set for the truststore.

3. Copy the truststore to the `resources` directory within the app source code.

4. Follow these instructions to [Create a Service Key](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-accessing-instance.html#create-service-key). 

5. Edit the app's `src/main/resources/application-app-foundation.properties`
file,
and specify the properties described in [Specifying Application Properties](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-running-app.html#app-properties).
Find the values needed in the service key and the truststore. 

6. Edit the app's `manifest_app_foundation.yml` file to specify the
truststore password noted when you created the truststore.

7. Build the app:

    ```
    $ ./mvnw clean install
    ```

8. Do a `cf login` that targets the app foundation's org and space.
With a current working directory of `PCC-Sample-App-PizzaStore`,
push the app to the app foundation, specifying the manifest:

    ```
    $ cf push -f manifest_app_foundation.yml
    ```
    Note the app's route (`APP-URL`).

##### Use the running app:

Interact with the running app by hitting the endpoints exposed by the app.

## Run the App Off Platform

Running an app that is not on any Cloud Foundry foundation
requires a service gateway.
To set up a service gateway,
follow the directions in
[Configure a Service Gateway](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-configure-service-gateway.html).

#### Run the app locally, and not on any foundation:

1. Make note of the `SERVICE-INSTANCE-NAME` when you
[Create a Service Instance](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-create-instance.html#create-SI).
Provide the optional parameters for enabling TLS and specifying
the creation of a service gateway.

2. Follow these instructions to
[Create Truststore for the App](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-running-app.html#app-truststore).
Note the password you set for the truststore.

3. Copy the truststore to the `resources` directory within the app source code.

4. Follow these instructions to [Create a Service Key](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-accessing-instance.html#create-service-key). 

5. Edit the app's `src/main/resources/application-off-platform.properties`
file,
and specify the properties described in [Specifying Application Properties](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-gemfire-on-cloud-foundry/2-1/gf-cf/content-running-app.html#app-properties).
Find the values needed in the service key and the truststore. 

6. Build the app:

    ```
    $ ./mvnw clean install
    ```

5. Run the app locally:

    ```
    $ ./mvnw spring-boot:run -Dspring-boot.run.profiles=off-platform -Dspring-boot.run.jvmArguments="-Djavax.net.ssl.trustStore=/tmp/mytruststore1.jks -Djavax.net.ssl.trustStorePassword=PASSWD-HERE"
    ```
    replacing `PASSWD-HERE` with the truststore password noted when
    you created the truststore.

##### Use the running app:

Interact with the running app by hitting the endpoints exposed by the app
at http://localhost:8080.

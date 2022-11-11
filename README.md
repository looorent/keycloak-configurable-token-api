# Keycloak Configuration Token REST API

This Custom Keycloak REST API provides an extra endpoint to request a token that can override default configuration.

It adds an endpoint `POST ${serverDomain}/auth/realms/${realm}/configurable-token`. Its configuration is provided in the request's body.

This implementation is based on the token exchange principle, defined here: https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/token-exchange/token-exchange.adoc

## Keycloak Support

Pay attention to your Keycloak version!

* For Keycloak `3.x.x`, use version <= 0.1 of this JAR. 
* For Keycloak `4.x.x`, use version > 0.1 of this JAR.
* For Keycloak `9.x.x`, use version >= 1.0.0 of this JAR.
* For Keycloak `10.x.x`, use version >= 1.1.0 of this JAR.
* For Keycloak `11.x.x`, use version >= 1.2.0 of this JAR.
* For Keycloak `12.x.x`, use version >= 1.3.0 of this JAR.
* For Keycloak `14.x.x`, use version >= 1.4.0 of this JAR.
* For Keycloak `15.x.x` < `15.0.2`, use version >= 1.5.0 of this JAR.
* For Keycloak `>= 15.0.2`, use version >= 1.5.1 of this JAR.
* For Keycloak `>= 17.x.x`, use version >= 1.7.0 of this JAR.
* For Keycloak `>= 18.x.x`, use version >= 1.8.0 of this JAR.
* For Keycloak `>= 19.x.x`, use version >= 1.9.0 of this JAR.
* For Keycloak `>= 20.x.x`, use version >= 1.10.0 of this JAR.

## Supported features

* Ask for a short-lived lifespan
* Ask for a long-lived lifespan

## Deployment (`>= 0.3`)

### Standalone install

* Download `dist/keycloak-configurable-token-1.8.0.jar` from this repository
* Add it to `$KEYCLOAK_HOME/standalone/deployments/`

### Docker install

If you are using the official Docker image, here is a `Dockerfile` that automate the installation procedure described above:
```
FROM quay.io/keycloak/keycloak:20.0.0

COPY keycloak-configurable-token-1.10.0.jar /opt/keycloak/providers/keycloak-configurable-token.jar
```

## Deployment (`< 0.3`)

Before `0.3`, this library cannot be deployed properly as a module with dependencies without using the CLI.
Therefore, using the CLI is mandatory.

### Environment variables

| Option | Default Value | Type | Required? | Description  | Example |
| ---- | ----- | ------ | ----- | ------ | ----- |
| `KEYCLOAK_LONG_LIVED_ROLE_NAME` | `long_lived_token`| String | Optional | The realm role an exchange token must have to request a long-lived-token. | `my-custom-role-for-long-lived-tokens` |

### Standalone install

* Download `dist/keycloak-configurable-token-0.2.jar` from this repository
* Modify `$KEYCLOAK_HOME/standalone/configuration/standalone.xml` and this node in `<providers>`
    ```xml
    <provider>module:be.looorent.keycloak-configurable-token</provider>
    ```
* Run `jboss-cli` to add this module and define their dependencies:
    ```bash
       $ jboss-cli.sh --command="module add --name=be.looorent.keycloak-configurable-token --resources=keycloak-configurable-token-0.2.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.jboss.logging,javax.ws.rs.api"
    ```

### Docker install

If you are using the official Docker image, here is a `Dockerfile` that automate the installation procedure described above:
```
FROM jboss/keycloak:14.0.0.Final

COPY keycloak-configurable-token-1.3.0.jar /tmp/keycloak-configurable-token.jar
RUN /opt/jboss/keycloak/bin/jboss-cli.sh --command="module add --name=be.looorent.keycloak-configurable-token --resources=/tmp/keycloak-configurable-token.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-common,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.jboss.logging,javax.ws.rs.api"
RUN sed -i -- 's/classpath:${jboss.home.dir}\/providers\/\*/classpath:${jboss.home.dir}\/providers\/*<\/provider><provider>module:be.looorent.keycloak-configurable-token/g' /opt/jboss/keycloak/standalone/configuration/standalone.xml
```

## Response format

    ```javascript
    {
        "access_token": "...",
        "expires_in": <your-short-lived-lifespan>,
        "refresh_expires_in": 0,
        "token_type": "bearer",
        "not-before-policy": ...,
        "session_state": "...", 
        "scope": ""
    }
    ```

## Use case

### Specify a short-lived token

Request's body must be in JSON and include an attribute `tokenLifespanInSeconds` (that must be strictly positive).

Example using CURL:
```
    $ curl -X POST -d '{ "tokenLifespanInSeconds": 20}' -H "Content-Type: application/json" -H "Authorization: Bearer <user-access-token>" http://auth.service.io/auth/realms/a-realm/configurable-token
```

### Specify a long-lived token

Request's body must be in JSON and include an attribute `tokenLifespanInSeconds` (that must be strictly positive). 
A very long lifespan (limited to the Java `integer` type) can be provided. For example: `31556952` means `1 year`.

The exchanging token must include the realm role defined by the environment variable named `KEYCLOAK_LONG_LIVED_ROLE_NAME`, otherwise `tokenLifespanInSeconds` will be ignored. Pay attention this role must be present in the exchange token itself, not on the Keycloak user only.

Example using CURL:
```
    $ curl -X POST -d '{ "tokenLifespanInSeconds": 63113904}' -H "Content-Type: application/json" -H "Authorization: Bearer <user-access-token>" http://auth.service.io/auth/realms/a-realm/configurable-token
```

## Limits

* Requesting a token for a different `clientId` is not available

## Development

### Build library

```bash
    $ ./gradlew build
```
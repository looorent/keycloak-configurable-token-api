# Keycloak Configuration Token REST API

This Custom Keycloak REST API provides an extra endpoint to request a token that can override default configuration.

It adds an endpoint `POST ${serverDomain}/auth/realms/${realm}/configurable-token`. Its configuration is provided in the request's body.

This implementation is based on the token exchance principle, defined here: https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/token-exchange/token-exchange.adoc

## Supported features

* Ask for a short-lived lifespan

## Deployment

### Standalone install

* Download `dist/keycloak-configurable-token-0.1.jar` from this repository
* Modify `$KEYCLOAK_HOME/standalone/configuration/standalone.xml` and this node in `<providers>`
    ```xml
    <provider>module:be.looorent.keycloak-configurable-token</provider>
    ```
* Run `jboss-cli` to add this module and define their dependencies:
    ```bash
       $ jboss-cli.sh --command="module add --name=be.looorent.keycloak-configurable-token --resources=keycloak-configurable-token-0.1.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.jboss.logging,javax.ws.rs.api"
    ```

### Docker install

If you are using the official Docker image, here is a `Dockerfile` that automate the install procedure described above:
```
FROM jboss/keycloak:3.4.2.Final

COPY keycloak-configurable-token-0.1.jar /tmp/keycloak-configurable-token.jar
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

Request's body must be in JSON an include an attribute `tokenLifespanInSeconds` (that must be strictly positive).
If `tokenLifespanInSeconds` exceeds the default Keycloak `Token Lifespan` (usually 300 seconds), it will be ignored.

Example using CURL:
```
    $ curl -X POST -d '{ "tokenLifespanInSeconds": 20}' -H "Content-Type: application/json" -H "Authorization: Bearer <user-access-token>" http://auth.service.io/auth/realms/a-realm/configurable-token
```


## Limits

* Requesting a token for a different `clientId` is not available

## Development

### Build library

```bash
    $ gradle build
```
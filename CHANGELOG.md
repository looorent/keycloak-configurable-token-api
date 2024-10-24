# Changelog

## 26.0.0 [2024-10-09]

* Compatibility with Keycloak 26

## 25.0.1 [2024-09-20]

* Bugfix: Since Keycloak 24, some properties of the Session's client (like USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED) are required to create an access token. Therefore the destination session must be associated to a client.

## 25.0.0 [2024-06-19]

* Compatibility with Keycloak 25

## 24.0.0 [2024-03-11]

* Move to Keycloak 24
* Make use of the Cors SPI

## 23.0.3 [2023-12-22]

* Compatibility with Keycloak 23.0.3

## 23.0.0 [2023-11-24]

* Compatibility with Keycloak 23.0.0

## 22.0.0 [2023-07-11]

* Compatibility with Keycloak 22.0.0

## 21.0.1 [2023-03-21]

* Make CORS Allowed origins configurable
* Fix `21.0.0` startup issues

## 21.0.0 [2023-03-21]

* Compatibility with Keycloak 21.0.0

## 1.10.0 [2022-11-11]

* Compatibility with Keycloak 20.0.0

## 1.9.0 [2022-07-27]

* Compatibility with Keycloak 19.0.0

## 1.8.0 [2022-04-21]

* Compatibility with Keycloak 18.0.0

## 1.7.0 [2022-01-15]

* Compatibility with Keycloak 17.0.0

## 1.5.1 [2021-08-30]

* Compatibility with Keycloak 15.0.2
* Update user session's timestamp

## 1.5.0 [2021-08-24]

* Compatibility with Keycloak 15

## 1.4.0 [2021-06-18]

* Compatibility with Keycloak 14

## 1.3.0 [2020-12-19]

* Compatibility with Keycloak 12

## 1.2.0 [2020-07-22]

* Compatibility with Keycloak 11

## 1.0.1 [2020-07-01]

* Introspect access token to give more details when failures occur
* Capability to use long-term tokens are now based on user model instead of the access token content
* Compatibility with Keycloak 10

## 1.0.0 [2019-04-18]

* Compatibility with Keycloak 9

## 0.5 [2019-02-19] 

* Tested on Keycloak 4.8.3
* Long-Lived tokens are restricted depending on a realm role

## 0.4 [2019-02-13] 

* Support for long-lived tokens

## 0.3 [2018-06-19]

* Support for Keycloak 4.0.0
* Wildfly automatic deployment


# Dremio UDF for retrieving Hashicorp secrect values

[![Build Status](https://travis-ci.org/dremio-hub/dremio-internal-function-example.svg?branch=master)](https://travis-ci.org/dremio-hub/dremio-internal-function-example)

This is an example of implimenting a Dremio Java function to retrieve a Hashicorp Vault value. The example has the signature retrieve_hashicorp_secret(<varchar>, <varchar>, <varchar>, <varchar>) and returns a secret value. It can be used by copying the built jar file into the Dremio jars directory.

Example Usage:
```
SELECT retrieve_hashicorp_secret('http://192.168.2.131:8200', 'xxxxxxx', 'kv/decryption_key', 'value') AS secret_value
```

# Dremio UDF for retrieving Hashicorp secrect values


This is an example of implimenting a Dremio Java function to retrieve a Hashicorp Vault value. The example has the signature retrieve_hashicorp_secret(hostname \<varchar\>, token \<varchar\>, secret_path \<varchar\>,  key \<varchar\>, invalidate_cache \<boolean\>) and returns a secret value. It can be used by copying the built jar file into the Dremio jars directory. (/opt/dremio/jars/3rdparty) For docker, you will need to build the docker image to include the jar file. 

Example Usage:
```
SELECT retrieve_hashicorp_secret('http://127.0.0.1:8200', 'xxxxxxx', 'kv/decryption_key', 'value') AS secret_value
```

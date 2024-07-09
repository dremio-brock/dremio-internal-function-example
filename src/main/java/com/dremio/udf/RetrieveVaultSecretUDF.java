package com.dremio.udf;

import javax.inject.Inject;

import org.apache.arrow.vector.holders.VarCharHolder;

import com.dremio.exec.expr.SimpleFunction;
import com.dremio.exec.expr.annotations.FunctionTemplate;
import com.dremio.exec.expr.annotations.FunctionTemplate.NullHandling;
import com.dremio.exec.expr.annotations.Output;
import com.dremio.exec.expr.annotations.Param;
import com.dremio.exec.expr.fn.impl.StringFunctionHelpers;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;

import org.apache.arrow.memory.ArrowBuf;

import java.nio.charset.StandardCharsets;

@FunctionTemplate(
        name = "retrieve_hashicorp_secret",
        nulls = NullHandling.NULL_IF_NULL
)
public class RetrieveVaultSecretUDF implements SimpleFunction {

  @Inject
  ArrowBuf buffer;

  @Param
  VarCharHolder vaultAddress;
  @Param
  VarCharHolder token;
  @Param
  VarCharHolder secretPath;
  @Param
  VarCharHolder key;
  @Param
  VarCharHolder invalidateCache;
  @Output
  VarCharHolder out;

  @Override
  public void setup() {
  }

  @Override
  public void eval() {
    String result = "";
    try {
      // Get the input values
      String vaultAddressStr = StringFunctionHelpers.getStringFromVarCharHolder(vaultAddress);
      String tokenStr = StringFunctionHelpers.getStringFromVarCharHolder(token);
      String secretPathStr = StringFunctionHelpers.getStringFromVarCharHolder(secretPath);
      String keyStr = StringFunctionHelpers.getStringFromVarCharHolder(key);
      String invalidateCacheStr = StringFunctionHelpers.getStringFromVarCharHolder(invalidateCache);

      // Create a cache key
      String cacheKey = vaultAddressStr + "|" + secretPathStr + "|" + keyStr;

      // Invalidate the cache if requested
      if ("true".equalsIgnoreCase(invalidateCacheStr)) {
        VaultCacheManager.invalidateCache(cacheKey);
      }

      // Check if the value is already in the cache
      result = VaultCacheManager.getFromCache(cacheKey);
      if (result == null) {
        // Set up the Vault configuration
        VaultConfig config = new VaultConfig()
                .address(vaultAddressStr)
                .token(tokenStr)
                .build();

        Vault vault = Vault.create(config);

        // Read the secret from Vault
        result = vault.logical().read(secretPathStr).getData().get(keyStr);

        // Store the result in the cache
        VaultCacheManager.putInCache(cacheKey, result);
      }
    } catch (VaultException e) {
      result = "Error: " + e.getMessage();
    }

    byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
    buffer.setBytes(0, resultBytes);

    out.buffer = buffer;
    out.start = 0;
    out.end = resultBytes.length;
  }
}

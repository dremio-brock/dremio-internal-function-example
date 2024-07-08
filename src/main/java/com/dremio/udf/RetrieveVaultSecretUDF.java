/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import io.github.jopenlibs.vault.response.LogicalResponse;

import org.apache.arrow.memory.ArrowBuf;

import java.nio.charset.StandardCharsets;

@FunctionTemplate(
    name = "retrieve_hashicorp_secret",
    nulls = NullHandling.NULL_IF_NULL)

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

      // Set up the Vault configuration
      VaultConfig config = new VaultConfig()
              .address(vaultAddressStr)
              .token(tokenStr)
              .build();

      Vault vault = Vault.create(config);

      // Read the secret from Vault
      result = " " + vault.logical().read(secretPathStr).getData().toString();
    } catch (VaultException e) {
      result = "Error: " + e.getMessage();
    }

    System.out.println("Input value: " + result);
    byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
    buffer.setBytes(0, resultBytes);

    out.buffer = buffer;
    out.start = 0;
    out.end = resultBytes.length;
  }
}
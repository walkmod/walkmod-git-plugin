/* 
  Copyright (C) 2016 Raquel Pau.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.git.providers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.WalkerConfig;

public class GitDiffConfigurationProvider implements ConfigurationProvider {

   Configuration configuration;

   @Override
   public void init(Configuration configuration) {
      this.configuration = configuration;
   }

   @Override
   public void load() throws ConfigurationException {
      File file;
      try {
         file = new File(".git").getCanonicalFile();

         if (file.exists()) {

            Collection<ChainConfig> chainCfgs = configuration.getChainConfigs();
            if (chainCfgs != null) {
               for (ChainConfig cc : chainCfgs) {
                  WalkerConfig wc = cc.getWalkerConfig();
                  Map<String, Object> params = wc.getParams();
                  if (params == null) {
                     params = new HashMap<String, Object>();
                     wc.setParams(params);
                  }
                  String language = configuration.getDefaultLanguage();
                  if ("java".equals(language)) {
                     params.put("constraintProviders", "git:" + language + "-constraint-provider");
                  }
               }
            }

         }
      } catch (IOException e) {
         throw new ConfigurationException("Error configuring git constraints", e);
      }
   }

}

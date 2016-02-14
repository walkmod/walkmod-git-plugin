package org.walkmod.git.providers;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;

public class GitDiffConfigurationProviderTest {

   @Test
   public void test() throws Exception {
      GitDiffConfigurationProvider provider = new GitDiffConfigurationProvider();
      Configuration conf = new ConfigurationImpl();
      conf.setDefaultLanguage("java");
      ChainConfig cc = new ChainConfigImpl();
      cc.setWalkerConfig(new WalkerConfigImpl());
      conf.addChainConfig(cc);
      provider.init(conf);

      provider.load();

      Map<String, Object> params = cc.getWalkerConfig().getParams();

      Assert.assertTrue(params.containsKey("constraintProviders"));

   }
}

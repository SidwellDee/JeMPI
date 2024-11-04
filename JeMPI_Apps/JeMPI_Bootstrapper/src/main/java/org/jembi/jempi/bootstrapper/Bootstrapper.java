package org.jembi.jempi.bootstrapper;

//import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;

public class Bootstrapper {
   //   protected static final Logger LOGGER = BootstrapperLogger.getLogger("Jempi Bootstrapper");
   protected BootstrapperConfig loadedConfig;

   public Bootstrapper(final String configFilePath) {
      this.loadedConfig = BootstrapperConfig.create(configFilePath);
   }
}


package org.jembi.jempi.bootstrapper.data;

import org.jembi.jempi.bootstrapper.Bootstrapper;
//import org.jembi.jempi.bootstrapper.utils.BootstrapperLogger;

public abstract class DataBootstrapper extends Bootstrapper implements IDataBootstrapper {
//   protected static final Logger LOGGER = BootstrapperLogger.getChildLogger(Bootstrapper.LOGGER, "Data");
//   private static final Logger LOGGER = LogManager.getLogger(Bootstrapper.class);


   public DataBootstrapper(final String configFilePath) {
      super(configFilePath);
   }
}

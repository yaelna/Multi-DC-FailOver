package com.gigaspaces.app;

import com.gigaspaces.app.event_processing.common.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Feeder {

    private static final Logger LOG = Logger.getLogger(Feeder.class.getName());

    private static final long DEFAULT_FEED_DELAY = 2000;
    private static final String SOURCE_SPACE_URL = "jini://*/*/source-space";
    private static final String TARGET_SPACE_URL = "jini://*/*/target-space";
            
    public static void main(String[] args) {
        String locators = args.length > 0 ? args[0] : "";
        try (SpaceConfigurer sourceSpaceConfigurer = new UrlSpaceConfigurer(SOURCE_SPACE_URL).lookupLocators(locators);
                SpaceConfigurer targetSpaceConfigurer = new UrlSpaceConfigurer(TARGET_SPACE_URL).lookupLocators(locators)) {
            
            GigaSpace sourceSpace = new GigaSpaceConfigurer(sourceSpaceConfigurer).gigaSpace();
            GigaSpace targetSpace = new GigaSpaceConfigurer(targetSpaceConfigurer).gigaSpace();
            
            FeederTask feederTask = new FeederTask(10, sourceSpace, targetSpace);
            
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            LOG.info("--- STARTING FEEDER WITH CYCLE [" + DEFAULT_FEED_DELAY + "]");
            executorService.scheduleAtFixedRate(feederTask, DEFAULT_FEED_DELAY, DEFAULT_FEED_DELAY, TimeUnit.MILLISECONDS);
            try {
                if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                    executorService.shutdownNow();
                }
            } catch(InterruptedException ie) {
                executorService.shutdownNow();
            }
            LOG.info("--- FEEDER WROTE [" + feederTask.counter.get() + "] ENTITIES");
        }
   }

    public static class FeederTask implements Runnable {

        private static final AtomicLong counter = new AtomicLong(0);
        private final long numberOfTypes;
        private GigaSpace primaryProxy;
        private GigaSpace backupProxy;
        private volatile boolean fallback;
        
        public FeederTask(long numberOfTypes, GigaSpace primaryProxy, GigaSpace backupProxy) {
            this.numberOfTypes = numberOfTypes;
            this.primaryProxy = primaryProxy;
            this.backupProxy = backupProxy;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            feed(fallback ? backupProxy : primaryProxy, new Data((counter.incrementAndGet() % numberOfTypes), "FEEDER " + Long.toString(time)));
        }
        
        private void feed(GigaSpace gigaSpace, Data data) {
            try {
                gigaSpace.write(data);
                LOG.info("--- FEEDER WROTE " + data + " TO SPACE [" + gigaSpace.getName() + "]");
            } catch (Exception e) {
                LOG.info(e.getMessage());
                fallback = true;
                feed(backupProxy, data);
            }
        }
    }
}

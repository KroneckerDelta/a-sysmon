package com.ajjpj.asysmon.datasink.log;

import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class ALog4JDataSink extends ALoggingDataSink {
    private static final Logger log = Logger.getLogger(ALog4JDataSink.class);

    @Override protected void log(String s) {
        log.debug(s);
    }

    @Override protected boolean isLoggingEnabled() {
        return log.isDebugEnabled();
    }
}

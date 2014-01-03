package com.ajjpj.asysmon.config;


import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.measure.scalar.ASystemLoadMeasurer;
import com.ajjpj.asysmon.servlet.threaddump.AThreadDumpPageDefinition;

/**
 * This class evaluates the default configuration files, creating a config instance from their content.
 *
 * @author arno
 */
public class ADefaultConfigFactory {
    public ASysMonConfig getConfig() {
        //TODO evaulate config files, make this stuff configurable
        final ASysMonConfigBuilder builder =
                new ASysMonConfigBuilder("demo", "1.0", "theInstance", "#ff8000")
                .addScalarMeasurer(new ASystemLoadMeasurer())
                .addScalarMeasurer(AConnectionCounter.INSTANCE)
                .addPresentationMenuEntry("Threads", new AThreadDumpPageDefinition("com.ajjpj"));

        return builder.build();
    }
}
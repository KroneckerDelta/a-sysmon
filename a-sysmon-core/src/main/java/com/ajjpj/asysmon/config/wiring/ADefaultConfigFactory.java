package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.AConfigFactory;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.ASysMonConfigBuilder;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.AFunction0;
import com.ajjpj.asysmon.util.AOption;
import com.ajjpj.asysmon.util.AUnchecker;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;


/**
 * This class evaluates the default configuration files, creating a config instance from their content.
 *
 * @author arno
 */
public class ADefaultConfigFactory implements AConfigFactory {
    public static final String DEFAULT_CONFIG_FILE = "asysmon-default.properties";
    public static final String CONFIG_FILE = "asysmon.properties";
    public static final String SYSPROP_PREFIX = "asysmon.";

    public static final String KEY_CONFIG_FACTORY = "config-factory";
    public static final String KEY_ENV_MEASURERS = "env-measurers";
    public static final String KEY_SCALAR_MEASURERS = "scalar-measurers";
    public static final String KEY_PRESENTATION_MENUS = "presentation-menus";

    public static AConfigFactory getConfigFactory() {

        return AUnchecker.executeUnchecked(new AFunction0<AConfigFactory, Exception>() {
            @Override public AConfigFactory apply() throws Exception {
                final Properties propsRaw = getProperties();

                final ConfigPropsFile props = new ConfigPropsFile(propsRaw, new AStdOutLogger()); //TODO
                final AOption<AConfigFactory> factoryOption = props.createInstance (KEY_CONFIG_FACTORY, props.get(KEY_CONFIG_FACTORY, false), AConfigFactory.class);
                return factoryOption.getOrElse(new ADefaultConfigFactory());
            }
        });
    }

    private static Properties getProperties() {
        try {
            final Properties result = new Properties();

            result.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE));

            final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
            if(in != null) {
                result.load(in);
            }

            for(String sysProp: System.getProperties().stringPropertyNames()) {
                if(! sysProp.startsWith(SYSPROP_PREFIX)) {
                    continue;
                }
                final String reducedKey = sysProp.substring(SYSPROP_PREFIX.length());
                result.setProperty(reducedKey, System.getProperty(sysProp));
            }

            return result;
        } catch (Exception e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    //TODO aliases
    public ASysMonConfig getConfig() {
        final ConfigPropsFile props = new ConfigPropsFile(getProperties(), new AStdOutLogger()); //TODO logger

        final ASysMonConfigBuilder builder = new ASysMonConfigBuilder("app", "version", "instance", "#ff8000"); //TODO config

        for(AEnvironmentMeasurer m: props.createInstances(KEY_ENV_MEASURERS, AEnvironmentMeasurer.class)) {
            builder.addEnvironmentMeasurer(m);
        }

        for(AScalarMeasurer m: props.createInstances(KEY_SCALAR_MEASURERS, AScalarMeasurer.class)) {
            builder.addScalarMeasurer(m);
        }

        for(String menuEntryRaw: props.getAndSplit(KEY_PRESENTATION_MENUS, true)) {
            final String menuEntry = menuEntryRaw.trim();

            final List<APresentationPageDefinition> pageDefs = props.createInstances(KEY_PRESENTATION_MENUS + "." + menuEntry, APresentationPageDefinition.class);
            builder.addPresentationMenuEntry(menuEntry, pageDefs);
        }

        return builder.build();
    }
}

package com.ajjpj.asysmon.datasink.cyclicdump;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * This class cyclically dumps all scalar measurements, e.g. to Log4J.<p />
 *
 * Calling the constructor takes care of registration with ASysMon. <p />
 *
 * This is a data sink for pragmatic reasons, 'shutdown' integration in particular. It does not actually use hierarchical
 *  measurements.
 *
 * @author arno
 */
public abstract class ACyclicMeasurementDumper implements ADataSink {
    private final ScheduledExecutorService ec;
    private final ASysMon sysMon;

    private final Runnable dumper = new Runnable() {
        @Override public void run() {
            try {
                final Map<String, AScalarDataPoint> m = sysMon.getScalarMeasurements();
                for(String key: m.keySet()) {
                    dump("Scalar Measurement: " + key + " = " + m.get(key).getFormattedValue());
                }
            }
            catch(Exception exc) {
                exc.printStackTrace();
                dump(exc.toString());
            }
        }
    };

    public ACyclicMeasurementDumper(ASysMon sysMon, int frequencyInSeconds) {
        this(sysMon, 0, frequencyInSeconds);
    }

    public ACyclicMeasurementDumper(ASysMon sysMon, int initialDelaySeconds, int frequencyInSeconds) {
        ec = Executors.newSingleThreadScheduledExecutor();
        ec.scheduleAtFixedRate(dumper, initialDelaySeconds, frequencyInSeconds, TimeUnit.SECONDS);

        this.sysMon = sysMon;
        ASysMonConfigurer.addDataSink(sysMon, this);
    }

    protected abstract void dump(String s);

    @Override public void onStartedHierarchicalMeasurement() {
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
    }

    @Override public void shutdown() {
        ec.shutdown();
    }
}

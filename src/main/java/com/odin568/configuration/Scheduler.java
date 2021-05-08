package com.odin568.configuration;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Scheduler {

    private final AtomicDouble valueHeater;
    private final AtomicDouble valueOutside;

    @Value("${homematic.url}")
    private String homematicUrl;

    @Value("${heater.datapoint}")
    private int heaterDeviceId;

    @Value("${outside.datapoint}")
    private int outsideDeviceId;

    @Value("${debugHomematic:false}")
    private boolean debug;

    @Autowired
    public Scheduler(MeterRegistry meterRegistry) {
        valueHeater = new AtomicDouble(0.0);
        Gauge
            .builder("heater.degree", valueHeater, AtomicDouble::doubleValue)
            .register(meterRegistry);

        valueOutside = new AtomicDouble(0.0);
        Gauge
                .builder("outside.degree", valueOutside, AtomicDouble::doubleValue)
                .register(meterRegistry);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void setValues() {
        Double outside = getTemperatureForDevice(outsideDeviceId);
        if (outside != null)
            valueOutside.set(outside);

        Double heater = getTemperatureForDevice(heaterDeviceId);
        if (heater != null)
            valueHeater.set(heater);
    }

    private Double getTemperatureForDevice(int deviceId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = buildDataPointUrl(deviceId);
            String result = restTemplate.getForObject(url, String.class);

            if (debug)
                System.out.println("Response for " + url + " = " + result);

            result = result.substring(result.indexOf("value='") + 7);
            result = result.substring(0, result.indexOf("'"));

            Double dbl = Double.parseDouble(result);
            if (debug)
                System.out.println("Parsed value for " + deviceId + ": " + dbl);

            return dbl;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    private String buildDataPointUrl(int deviceId) {
        String url = homematicUrl;
        if (!url.endsWith("/"))
            url += "/";
        url += "addons/xmlapi/state.cgi?datapoint_id=" + deviceId;
        return url;
    }
}

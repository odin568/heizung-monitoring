package com.odin568.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Prometheus {

    @Value("${homematic.url}")
    private String homematicUrl;

    @Value("${heater.datapoint}")
    private int heaterDeviceId;

    @Value("${outside.datapoint}")
    private int outsideDeviceId;

    @Value("${debugHomematic:false}")
    private boolean debug;

    /*
# HELP outside_degree
# TYPE outside_degree gauge
outside_degree 24.6
# HELP heater_degree
# TYPE heater_degree gauge
heater_degree 26.9
 */
    @GetMapping(value = "/api/prometheus", produces = "text/plain")
    public String prometheus()
    {
        String result = "";
        Double outside = getTemperatureForDevice(outsideDeviceId);
        Double heater = getTemperatureForDevice(heaterDeviceId);

        if (outside != null) {
            result += "# HELP outside_degree" + System.lineSeparator();
            result += "# TYPE outside_degree gauge" + System.lineSeparator();
            result += "outside_degree " + String.format("%.1f", outside) + System.lineSeparator();
        }

        if (heater != null) {
            result += "# HELP heater_degree" + System.lineSeparator();
            result += "# TYPE heater_degree gauge" + System.lineSeparator();
            result += "heater_degree " + String.format("%.1f", heater) + System.lineSeparator();
        }

        return result.trim();
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

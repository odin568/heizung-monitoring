package com.odin568.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class Prometheus {

    @Value("${homematic.url}")
    private String homematicUrl;

    @Value("${homematic.sid}")
    private String homematicSid;

    @Value("${heater.flowId}")
    private int heaterFlowId;

    @Value("${heater.returnId}")
    private int heaterReturnId;

    @Value("${circuit.datapoint}")
    private int circuitDeviceId;

    @Value("${outside.datapoint}")
    private int outsideDeviceId;

    @Value("${water.datapoint}")
    private int waterDeviceId;

    /*
# HELP outside_degree
# TYPE outside_degree gauge
outside_degree 4,5
# HELP heater_degree
# TYPE heater_degree gauge
heater_degree 56,5
# HELP heater_return_degree
# TYPE heater_return_degree gauge
heater_return_degree 51,8
# HELP circuit_degree
# TYPE circuit_degree gauge
circuit_degree 51,9
# HELP water_degree
# TYPE water_degree gauge
water_degree 53,0
 */
    @GetMapping(value = "/api/prometheus", produces = "text/plain")
    public String prometheus()
    {
        String result = "";
        Double outside = getDeviceValue(outsideDeviceId);
        Double heaterFlow = getSysVarValue(heaterFlowId);
        Double heaterReturn = getSysVarValue(heaterReturnId);

        Double circuit = getDeviceValue(circuitDeviceId);
        Double water = getDeviceValue(waterDeviceId);

        if (outside != null) {
            result += "# HELP outside_degree" + System.lineSeparator();
            result += "# TYPE outside_degree gauge" + System.lineSeparator();
            result += "outside_degree " + String.format("%.1f", outside) + System.lineSeparator();
        }

        if (heaterFlow != null) {
            result += "# HELP heater_degree" + System.lineSeparator();
            result += "# TYPE heater_degree gauge" + System.lineSeparator();
            result += "heater_degree " + String.format("%.1f", heaterFlow) + System.lineSeparator();
        }

        if (heaterReturn != null) {
            result += "# HELP heater_return_degree" + System.lineSeparator();
            result += "# TYPE heater_return_degree gauge" + System.lineSeparator();
            result += "heater_return_degree " + String.format("%.1f", heaterReturn) + System.lineSeparator();
        }

        if (circuit != null) {
            result += "# HELP circuit_degree" + System.lineSeparator();
            result += "# TYPE circuit_degree gauge" + System.lineSeparator();
            result += "circuit_degree " + String.format("%.1f", circuit) + System.lineSeparator();
        }

        if (water != null) {
            result += "# HELP water_degree" + System.lineSeparator();
            result += "# TYPE water_degree gauge" + System.lineSeparator();
            result += "water_degree " + String.format("%.1f", water) + System.lineSeparator();
        }

        return result.trim();
    }

    private Double getDeviceValue(int deviceId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = buildDataPointUrl(deviceId);
            String result = restTemplate.getForObject(url, String.class);

            result = result.substring(result.indexOf("value='") + 7);
            result = result.substring(0, result.indexOf("'"));

            return Double.parseDouble(result);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private Double getSysVarValue(int iseId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = buildSysVarUrl(iseId);
            String result = restTemplate.getForObject(url, String.class);

            result = result.substring(result.indexOf("value='") + 7);
            result = result.substring(0, result.indexOf("'"));

            return Double.parseDouble(result);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String buildDataPointUrl(int deviceId) {
        String url = homematicUrl;
        if (!url.endsWith("/"))
            url += "/";
        url += "addons/xmlapi/state.cgi?datapoint_id=" + deviceId + "&sid=" + URLEncoder.encode(homematicSid, StandardCharsets.UTF_8);
        return url;
    }

    private String buildSysVarUrl(int iseId) {
        String url = homematicUrl;
        if (!url.endsWith("/"))
            url += "/";
        url += "addons/xmlapi/sysvar.cgi?ise_id=" + iseId + "&sid=" + URLEncoder.encode(homematicSid, StandardCharsets.UTF_8);
        return url;
    }
}

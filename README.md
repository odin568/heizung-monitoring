# Heizung Monitoring

Purpose is to monitor my heater engine as well as the outside temperature.  
As I am plugged to a local heating network with partial problems, I want to easily visualize also from remote what the state of the heater temperature is.  

## Implementation

Small SpringBoot Service which provides a Prometheus compatible REST API to expose the outside degree as well as the heater degree. Both values are gathered from my Raspberrymatic device.  
This dockerized service is combined with other services via docker-compose:
* Prometheus
* Grafana
* nginx

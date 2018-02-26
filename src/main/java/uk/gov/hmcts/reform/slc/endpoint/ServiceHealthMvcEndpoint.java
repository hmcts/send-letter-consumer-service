package uk.gov.hmcts.reform.slc.endpoint;

import org.springframework.boot.actuate.endpoint.mvc.ActuatorMediaTypes;
import org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.HypermediaDisabled;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@ConfigurationProperties(prefix = "endpoints.health")
public class ServiceHealthMvcEndpoint extends HealthMvcEndpoint {

    public ServiceHealthMvcEndpoint(ServiceHealthEndpoint delegate, boolean secure, List<String> roles) {
        super(delegate, secure, roles);
    }

    @GetMapping(value = "/service", produces = {
        ActuatorMediaTypes.APPLICATION_ACTUATOR_V1_JSON_VALUE,
        MediaType.APPLICATION_JSON_VALUE
    })
    @ResponseBody
    @HypermediaDisabled
    public Object serviceHealth() {
        if (!getDelegate().isEnabled()) {
            return getDisabledResponse();
        }

        return ((ServiceHealthEndpoint) getDelegate()).checkHealth();
    }
}

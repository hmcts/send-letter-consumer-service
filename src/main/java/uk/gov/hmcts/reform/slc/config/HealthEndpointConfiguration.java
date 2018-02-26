package uk.gov.hmcts.reform.slc.config;

import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.HealthMvcEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.slc.ServiceHealthIndicator;
import uk.gov.hmcts.reform.slc.endpoint.ServiceHealthEndpoint;
import uk.gov.hmcts.reform.slc.endpoint.ServiceHealthMvcEndpoint;

import java.util.Map;

@AutoConfigureBefore(EndpointWebMvcManagementContextConfiguration.class)
@ConditionalOnProperty(prefix = "management.health", name = "service.enabled")
@ManagementContextConfiguration
public class HealthEndpointConfiguration {

    @Bean
    public ServiceHealthEndpoint serviceHealthEndpoint(HealthAggregator aggregator,
                                                       Map<String, HealthIndicator> healthIndicators) {
        return new ServiceHealthEndpoint(aggregator, healthIndicators, new ServiceHealthIndicator());
    }

    /**
     * Overriding health mvc endpoint bean to be able to include custom service health endpoint.
     *
     * @param delegate Integration for endpoint call
     * @return Custom health endpoints
     */
    @Bean
    @ConditionalOnEnabledEndpoint("health")
    public HealthMvcEndpoint healthMvcEndpoint(ServiceHealthEndpoint delegate,
                                               ManagementServerProperties managementServerProperties,
                                               HealthMvcEndpointProperties healthMvcEndpointProperties) {
        HealthMvcEndpoint healthMvcEndpoint = new ServiceHealthMvcEndpoint(
            delegate,
            managementServerProperties.getSecurity().isEnabled(),
            managementServerProperties.getSecurity().getRoles()
        );

        if (healthMvcEndpointProperties.getMapping() != null) {
            healthMvcEndpoint.addStatusMapping(healthMvcEndpointProperties.getMapping());
        }

        return healthMvcEndpoint;
    }
}

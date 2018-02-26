package uk.gov.hmcts.reform.slc.endpoint;

import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.slc.ServiceHealthIndicator;

import java.util.Map;

@ConfigurationProperties(prefix = "endpoints.health")
public class ServiceHealthEndpoint extends HealthEndpoint {

    private final ServiceHealthIndicator serviceHealthIndicator;

    /**
     * Create a new {@link ServiceHealthEndpoint} instance.
     */
    public ServiceHealthEndpoint(HealthAggregator aggregator,
                                 Map<String, HealthIndicator> healthIndicators,
                                 ServiceHealthIndicator serviceHealthIndicator) {
        super(aggregator, healthIndicators);

        this.serviceHealthIndicator = serviceHealthIndicator;
    }

    public Health checkHealth() {
        return serviceHealthIndicator.health();
    }
}

package uk.gov.hmcts.reform.slc;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Never create as a bean as it will automatically include in Actuator list of health indicators.
 */
public class ServiceHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // TODO implement for smoke tests

        builder.up();
    }
}

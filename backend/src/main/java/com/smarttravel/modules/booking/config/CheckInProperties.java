package com.smarttravel.modules.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for online check-in eligibility window and operational parameters.
 */
@Configuration
@ConfigurationProperties(prefix = "smarttravel.checkin")
public class CheckInProperties {

    /**
     * Whether online check-in functionality is enabled.
     */
    private boolean enabled = true;

    /**
     * Window in hours before flight departure when online check-in opens (default: 24 hours).
     */
    private int openingHoursBeforeDeparture = 24;

    /**
     * Window in minutes before flight departure when online check-in closes (default: 60 minutes).
     */
    private int closingMinutesBeforeDeparture = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOpeningHoursBeforeDeparture() {
        return openingHoursBeforeDeparture;
    }

    public void setOpeningHoursBeforeDeparture(int openingHoursBeforeDeparture) {
        this.openingHoursBeforeDeparture = openingHoursBeforeDeparture;
    }

    public int getClosingMinutesBeforeDeparture() {
        return closingMinutesBeforeDeparture;
    }

    public void setClosingMinutesBeforeDeparture(int closingMinutesBeforeDeparture) {
        this.closingMinutesBeforeDeparture = closingMinutesBeforeDeparture;
    }
}

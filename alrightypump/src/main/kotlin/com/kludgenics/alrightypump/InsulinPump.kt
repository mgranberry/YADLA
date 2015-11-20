package com.kludgenics.alrightypump

import org.joda.time.Chronology


/**
 * A baseline interface to query an insulin pump.  This interface only provides the ability to
 * identify and verify a connection to a pump along with metadata about the pump.  An actual
 * implementation will implement other interfaces to query the device logs and
 */
interface InsulinPump {
    val supportedFeatures: Set<DeviceFeature>

    /**
     * A Joda Time [Chronolgy] constructed from pump time change records, allowing for
     * the translation of raw pump events to standard time.
     */
    val chronology: Chronology

    /**
     * Opaque list of version identifiers
     */
    val firmwareVersions: List<String>

    /**
     * Opaque list of serial numbers
     */
    val serialNumbers: List<String>

    /**
     * Test for support for a given [DeviceFeature].
     */
    fun hasFeature(deviceFeature: DeviceFeature): Boolean {
        return deviceFeature in supportedFeatures
    }
}


/**
 * These are based upon the Tidepool.org pump checklist.
 */
enum class DeviceFeature {
    // Basal Events
    BASAL_SCHEDULED,
    BASAL_SCHEDULE_NAME,
    BASAL_EVENT_MIDNIGHT_SPLIT,

    MANUAL_TEMP_BASAL,
    MANUAL_TEMP_START_DURATION_RATE,
    MANUAL_TEMP_SEGMENTS,

    TEMP_PERCENT,
    TEMP_PERCENT_START_DURATION_PERCENT,
    TEMP_PERCENT_RATE_PROVIDED,
    TEMP_PERCENT_RATE_COMPUTED,
    TEMP_PERCENT_SEGMENTS,

    FINAL_BASAL_PROVIDED,


    // Bolus events
    NORMAL_BOLUS,
    NORMAL_BOLUS_DELIVERED,
    NORMAL_BOLUS_PROGRAMMED,

    EXTEND_BOLUS,
    EXTEND_BOLUS_DURATION,
    EXTEND_BOLUS_PROGRAMMED,
    EXTENDED_BOLUS_DELIVERED,
    EXTENDED_BOLUS_MIDNIGHT_SPLIT,

    COMBO_BOLUS,
    COMBO_NORMAL_DELIVERED,
    COMBO_EXTENDED_DELIVERED,
    COMBO_EXTENDED_DURATION,
    COMBO_NORMAL_PROGRAMMED,
    COMBO_EXTENDED_PROGRAMMED,
    COMBO_DURATION_PROGRAMMED,
    COMBO_EXTENDED_MIDNIGHT_SPLIT,
    COMBO_CANCELLATIONS_SPLIT,
    COMBO_LINKS_TO_BOLUS_CALCULATOR,

    EVENT_ALARAM_LOW_INSULIN,
    EVENT_NO_INSULIN,
    EVENT_NO_INSULIN_STOPS_DELIVERY,
    EVENT_LOW_POWER,
    EVENT_NO_POWER,
    EVENT_NO_POWER_STOPS_DELIVERY,
    EVENT_OCCLUSION,
    EVENT_OCCLUSION_STOPS_DELIVERY,
    EVENT_NO_DELIVERY,
    EVENT_NO_DELIVERY_STOPS_DELIVERY,
    EVENT_AUTO_OFF,
    EVENT_AUTO_OFF_STOPS_DELIVERY,
    EVENT_OVER_LIMIT,

    EVENT_PRIME,
    EVENT_PRIME_TUBING,
    EVENT_PRIME_CANNULA,
    EVENT_PRIME_UNDIFFERENTIATED,
    EVENT_PRIME_VOLUME_UNITS,

    EVENT_RESERVOIR_CHANGE,
    EVENT_RESERVOIR_CHANGE_STOPS_DELIVERY,

    EVENT_DELIVERY_STATUS,
    EVENT_DELIVERY_STATUS_INTERVALS,
    EVENT_DELIVERY_STATUS_STARTSTOP,
    EVENT_DELIVERY_STATUS_START_AGENT,
    EVENT_DELIVERY_STATUS_STOP_AGENT,

    EVENT_TIME_CHANGE,
    EVENT_TIME_FROM_TO,
    EVENT_TIME_AGENT,
    EVENT_TIME_TIMEZONE,
    EVENT_TIME_REASON,

    EVENT_SMBG,
    EVENT_SMBG_SUBTYPE,
    EVENT_SMBG_UNITS,
    EVENT_SMBG_LOHI,
    EVENT_SMBG_LOHI_THRESHOLD,

    SETTINGS_BASAL_SCHEDULE,
    SETTINGS_BASAL_SCHEDULE_NAME,
    SETTINGS_PROFILE_NAME,
    SETTINGS_SCHEDULE_NAME_RATE_START,
    SETTINGS_ACTIVE_SCHEDULE,
    SETTINGS_UNITS,
    SETTINGS_UNITS_CARBS,
    SETTINGS_CARB_RATIO,
    SETTINGS_CARB_RATIO_NAME,
    SETTINGS_INSULIN_SENSITIVITY,
    SETTINGS_INSULIN_SENSITIVITY_NAME,
    SETTINGS_INSULIN_SENSITIVITY_SEGMENT,
    SETTINGS_BG_TARGET,
    SETTINGS_BG_TARGET_NAME,
    SETTINGS_BG_TARGET_SEGMENT,

    SETTINGS_HISTORY_FULL,

    WIZARD_RECOMMENDED_BOLUS,
    WIZARD_RECOMMENDED_CARBS,
    WIZARD_RECOMMENDED_CORR,
    WIZARD_INPUT_BG,
    WIZARD_INPUT_CARBS,
    WIZARD_IOB,
    WIZARD_CARB_RATIO,
    WIZARD_ISF,
    WIZARD_BG_TARGET,
    WIZARD_BG_LINK_ID
}

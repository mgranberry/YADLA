package com.kludgenics.cgmlogger.app.xdrip

object XdripBroadcast {
    val RECEIVER_PERMISSION = "com.kludgenics.cgmlogger.permissions.YADLA_BG_ESTIMATE";

    val ACTION_NEW_BG_ESTIMATE = "com.kludgenics.cgmlogger.app.BgEstimate";
    val ACTION_NEW_BG_ESTIMATE_NO_DATA = "com.eveningoutpost.dexdrip.BgEstimateNoData";

    val EXTRA_BG_ESTIMATE = "com.eveningoutpost.dexdrip.Extras.BgEstimate";
    val EXTRA_BG_SLOPE = "com.eveningoutpost.dexdrip.Extras.BgSlope";
    val EXTRA_BG_SLOPE_NAME = "com.eveningoutpost.dexdrip.Extras.BgSlopeName";
    val EXTRA_SENSOR_BATTERY = "com.eveningoutpost.dexdrip.Extras.SensorBattery";
    val EXTRA_TIMESTAMP = "com.eveningoutpost.dexdrip.Extras.Time";
    val EXTRA_RAW = "com.eveningoutpost.dexdrip.Extras.Raw";
}
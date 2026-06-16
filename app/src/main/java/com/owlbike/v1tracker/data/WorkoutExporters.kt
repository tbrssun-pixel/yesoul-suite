package com.owlbike.v1tracker.data

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class WorkoutExportFormat(
    val extension: String,
    val mimeType: String,
) {
    Csv("csv", "text/csv"),
    Tcx("tcx", "application/vnd.garmin.tcx+xml"),
}

object WorkoutExporters {
    private val fileNameFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC)

    fun fileName(session: WorkoutSessionEntity, format: WorkoutExportFormat): String {
        val stamp = fileNameFormatter.format(Instant.ofEpochMilli(session.startTimeMillis))
        return "owl-bike-$stamp.${format.extension}"
    }

    fun export(
        session: WorkoutSessionEntity,
        samples: List<WorkoutSampleEntity>,
        format: WorkoutExportFormat,
    ): String {
        return when (format) {
            WorkoutExportFormat.Csv -> toCsv(samples)
            WorkoutExportFormat.Tcx -> toTcx(session, samples)
        }
    }

    fun toCsv(samples: List<WorkoutSampleEntity>): String {
        return buildString {
            appendLine(
                "timestamp_iso,elapsed_seconds,speed_kmh,cadence_rpm,power_watts," +
                    "heart_rate_bpm,resistance_level,distance_meters,calories",
            )
            samples.forEach { sample ->
                appendLine(
                    listOf(
                        iso(sample.timestampMillis),
                        sample.elapsedSeconds.toString(),
                        sample.speedKmh?.decimal(3).orEmpty(),
                        sample.cadenceRpm?.decimal(1).orEmpty(),
                        sample.powerWatts?.toString().orEmpty(),
                        sample.heartRateBpm?.toString().orEmpty(),
                        sample.resistanceLevel?.decimal(1).orEmpty(),
                        sample.distanceMeters?.decimal(1).orEmpty(),
                        sample.calories?.toString().orEmpty(),
                    ).joinToString(","),
                )
            }
        }
    }

    fun toTcx(session: WorkoutSessionEntity, samples: List<WorkoutSampleEntity>): String {
        val startIso = iso(session.startTimeMillis)
        val endMillis = session.endTimeMillis
            ?: samples.lastOrNull()?.timestampMillis
            ?: session.startTimeMillis
        val totalSeconds = ((endMillis - session.startTimeMillis) / 1000).coerceAtLeast(0)
        val totalDistance = session.totalDistanceMeters ?: samples.lastOrNull { it.distanceMeters != null }?.distanceMeters
        val totalCalories = session.totalCalories ?: samples.lastOrNull { it.calories != null }?.calories

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine(
                """<TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2" """ +
                    """xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" """ +
                    """xmlns:ns3="http://www.garmin.com/xmlschemas/ActivityExtension/v2" """ +
                    """xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 """ +
                    """http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">""",
            )
            appendLine("  <Activities>")
            appendLine("""    <Activity Sport="Biking">""")
            appendLine("      <Id>$startIso</Id>")
            appendLine("""      <Lap StartTime="$startIso">""")
            appendLine("        <TotalTimeSeconds>$totalSeconds</TotalTimeSeconds>")
            totalDistance?.let { appendLine("        <DistanceMeters>${it.decimal(1)}</DistanceMeters>") }
            totalCalories?.let { appendLine("        <Calories>$it</Calories>") }
            appendLine("        <Intensity>Active</Intensity>")
            appendLine("        <TriggerMethod>Manual</TriggerMethod>")
            appendLine("        <Track>")
            samples.forEach { appendTcxTrackpoint(it) }
            appendLine("        </Track>")
            appendLine("      </Lap>")
            appendLine("      <Notes>Owl Bike V1 Tracker local export. Manual resistance is not encoded in TCX.</Notes>")
            appendLine("    </Activity>")
            appendLine("  </Activities>")
            appendLine("</TrainingCenterDatabase>")
        }
    }

    private fun StringBuilder.appendTcxTrackpoint(sample: WorkoutSampleEntity) {
        appendLine("          <Trackpoint>")
        appendLine("            <Time>${iso(sample.timestampMillis)}</Time>")
        sample.distanceMeters?.let { appendLine("            <DistanceMeters>${it.decimal(1)}</DistanceMeters>") }
        sample.heartRateBpm?.let {
            appendLine("            <HeartRateBpm>")
            appendLine("              <Value>$it</Value>")
            appendLine("            </HeartRateBpm>")
        }
        sample.cadenceRpm?.let { appendLine("            <Cadence>${it.roundToInt()}</Cadence>") }
        if (sample.powerWatts != null || sample.speedKmh != null) {
            appendLine("            <Extensions>")
            appendLine("              <ns3:TPX>")
            sample.speedKmh?.let { appendLine("                <ns3:Speed>${(it / 3.6).decimal(3)}</ns3:Speed>") }
            sample.powerWatts?.let { appendLine("                <ns3:Watts>$it</ns3:Watts>") }
            appendLine("              </ns3:TPX>")
            appendLine("            </Extensions>")
        }
        appendLine("          </Trackpoint>")
    }

    private fun iso(millis: Long): String = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(millis))

    private fun Double.decimal(decimals: Int): String = "%.${decimals}f".format(Locale.US, this)
}

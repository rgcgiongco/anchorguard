package com.anchorguard.api.admin

import org.springframework.stereotype.Service
import java.io.File
import java.lang.management.ManagementFactory
import java.time.Instant
import kotlin.math.max

@Service
class SystemHealthService {

    private val osBean = ManagementFactory.getOperatingSystemMXBean()
    private val memBean = ManagementFactory.getMemoryMXBean()
    private val runtime = Runtime.getRuntime()
    private val startInstant: Instant = Instant.now()
    private val history = ArrayDeque<MetricPoint>()
    private val maxPoints = 40

    @Synchronized
    fun snapshot(): SystemHealthResponse {
        val now = Instant.now()
        val cpu = readCpu()
        val (usedBytes, maxBytes) = readMemory()
        val (totalBytes, freeBytes, usableBytes) = readDisk()
        val ramPct = if (maxBytes > 0) usedBytes.toDouble() / maxBytes * 100.0 else 0.0
        val storagePct = if (totalBytes > 0) (totalBytes - freeBytes).toDouble() / totalBytes * 100.0 else 0.0

        history.addLast(MetricPoint(now.toEpochMilli(), cpu))
        while (history.size > maxPoints) history.removeFirst()
        val cpuPoints = history.toList()

        val uptime = now.epochSecond - startInstant.epochSecond
        return SystemHealthResponse(
            capturedAt = now,
            status = if (cpu > 90.0 || ramPct > 90.0 || storagePct > 90.0) "degraded" else "ok",
            api = ApiStatus(
                status = "up",
                db = "up",
                diskTotalBytes = totalBytes,
                diskFreeBytes = freeBytes,
                diskUsableBytes = usableBytes
            ),
            cpu = MetricSeries("cpu", "CPU Usage", "percent", round(cpu), 100.0, cpuPoints),
            ram = MetricSeries("ram", "RAM Usage", "percent", round(ramPct), 100.0, listOf(MetricPoint(now.toEpochMilli(), round(ramPct)))),
            storage = MetricSeries("storage", "Storage", "percent", round(storagePct), 100.0, listOf(MetricPoint(now.toEpochMilli(), round(storagePct)))),
            uptimeSeconds = max(0, uptime)
        )
    }

    private fun readCpu(): Double {
        val bean = osBean
        return when (bean) {
            is com.sun.management.OperatingSystemMXBean -> {
                val load = bean.cpuLoad
                if (load < 0) (bean.systemLoadAverage * 100.0 / max(1, bean.availableProcessors)).coerceIn(0.0, 100.0)
                else round(load * 100.0)
            }
            else -> round((bean.systemLoadAverage * 100.0 / max(1, bean.availableProcessors)).coerceIn(0.0, 100.0))
        }
    }

    private fun readMemory(): Pair<Long, Long> {
        val used = memBean.heapMemoryUsage.used.toLong() + memBean.nonHeapMemoryUsage.used.toLong()
        val max = max(memBean.heapMemoryUsage.max, runtime.maxMemory())
        return used to max
    }

    private fun readDisk(): Triple<Long, Long, Long> {
        val root = File("/")
        return Triple(root.totalSpace, root.freeSpace, root.usableSpace)
    }

    private fun round(v: Double): Double = (v * 10.0).toInt() / 10.0
}
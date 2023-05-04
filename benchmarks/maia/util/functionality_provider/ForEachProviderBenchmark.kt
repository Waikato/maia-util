package maia.util.functionality_provider

import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Mode
import kotlinx.benchmark.Scope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

/**
 * TODO
 */
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
open class ForEachProviderBenchmark {

    @State(Scope.Benchmark)
    open class Params {

        @Param("sync-sync", "async-sync", "async-async")
        var mode: String = ""

        @Param("1000000", "0", "-1")
        var capacity: Int = 0

    }

    @Benchmark
    fun benchmark(params: Params, blackhole : Blackhole) {
        // Create a ForEachProvider which infinitely supplies zeroes
        val provider = if (params.capacity == -1)
            ForEachProvider.fromGetIterator {
                object : Iterator<Int> {
                    private var i = 0
                    override fun hasNext() : Boolean = i < 1_000_000
                    override fun next() : Int = i++
                }
            }
        else
            ForEachProvider.fromGetFlow(
                params.capacity
            ) {
                flow {
                    var i = 0
                    while (i < 1_000_000) emit(i++)
                }
            }

        when (params.mode) {
            "sync-sync" -> provider.utiliseSync { blackhole.consume(it) }
            "async-sync" -> runBlocking { provider.utiliseSync { blackhole.consume(it) } }
            else -> runBlocking { provider.utiliseAsync { blackhole.consume(it) } }
        }

    }

}

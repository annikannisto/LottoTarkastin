import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.system.measureTimeMillis

fun main() {
    Lotto.generateGuessesMultiThreaded()
}

class Lotto {
    private val lottoNumbers: Set<Int> = generateLottoNumbers()
    private val stats = AtomicIntegerArray(8)

    init {
        println("Lotto numbers: $lottoNumbers")
    }

    private fun generateLottoNumbers(): Set<Int> {
        return generateSequence { ThreadLocalRandom.current().nextInt(1, 41) }
            .distinct()
            .take(7)
            .toSet()
    }

    fun check(numbers: List<Int>) {
        val matches = numbers.count { it in lottoNumbers }
        stats.incrementAndGet(matches)
    }

    fun getStats(): IntArray = IntArray(8) { stats[it] }

    companion object {
        private const val TOTAL_GUESSES = 13_500_000
        private val THREAD_COUNT = Runtime.getRuntime().availableProcessors()

        fun generateGuessesMultiThreaded(): IntArray {
            val lotto = Lotto()
            val threads = mutableListOf<Thread>()
            val guessesPerThread = TOTAL_GUESSES / THREAD_COUNT

            val time = measureTimeMillis {
                repeat(THREAD_COUNT) { threadIndex ->
                    val thread = Thread {
                        println("Thread ${threadIndex + 1}/$THREAD_COUNT starting for $guessesPerThread guesses")
                        repeat(guessesPerThread) {
                            val guess = generateSequence { ThreadLocalRandom.current().nextInt(1, 41) }
                                .distinct()
                                .take(7)
                                .toList()
                            lotto.check(guess)
                        }
                    }
                    threads.add(thread)
                    thread.start()
                }

                threads.forEach { it.join() }
            }
            println("All joined")
            println("Running time $time ms")

            val stats = lotto.getStats()
            val checksum = stats.sum()
            println("Checksum: $checksum should be $TOTAL_GUESSES")
            stats.forEachIndexed { index, value -> println("$index: $value") }

            return stats
        }
    }
}



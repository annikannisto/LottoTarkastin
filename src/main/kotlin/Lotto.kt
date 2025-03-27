import java.util.concurrent.ThreadLocalRandom

fun main() {
    Lotto.generateGuessesMultiThreaded()
}

class Lotto {
    private val lottoNumbers: Set<Int> = generateLottoNumbers()
    private val stats = IntArray(8)

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
        stats[matches]++
    }

    companion object {
        private const val TOTAL_GUESSES = 50_500_000
        private val THREAD_COUNT = Runtime.getRuntime().availableProcessors()

        fun generateGuessesMultiThreaded() {
            val lotto = Lotto()
            val guessesPerThread = TOTAL_GUESSES / THREAD_COUNT


            val startTime = System.currentTimeMillis()

            val threads = (1..THREAD_COUNT).map { threadIndex ->
                Thread {
                    println("Thread ${threadIndex}/$THREAD_COUNT starting for $guessesPerThread guesses")
                    repeat(guessesPerThread) {
                        val guess = generateSequence { ThreadLocalRandom.current().nextInt(1, 41) }
                            .distinct()
                            .take(7)
                            .toList()
                        lotto.check(guess)
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            val endTime = System.currentTimeMillis()

            val runningTime = endTime - startTime
            println("All joined")
            println("Running time: $runningTime ms")

            val checksum = lotto.stats.sum()
            println("Checksum: $checksum should be $TOTAL_GUESSES")


            lotto.stats.forEachIndexed { index, value -> println("$index hits: $value guesses") }
        }
    }
}

package lu.jimenez.research.mwdbtoken.utils


class MinimunEditDistance<T : Comparable<T>>(val former: Array<T>, val newer: Array<T>) {


    val minEditDistanceMatrix: Array<IntArray> = Array(newer.size + 1, { IntArray(former.size + 1) })
    val backtraceMatrix: Array<Array<MutableList<Modification>>> = Array(newer.size + 1, { Array<MutableList<Modification>>(former.size + 1, { mutableListOf() }) })

    init {
        computeEditDistance()
    }

    private fun computeEditDistance() {
        for (i in 0..newer.size) {
            minEditDistanceMatrix[i][0] = i
        }
        for (j in 0..former.size) {
            minEditDistanceMatrix[0][j] = j
        }

        for (i in 1..newer.size) {
            for (j in 1..former.size) {
                val insert = minEditDistanceMatrix[i - 1][j] + 1
                val delet = minEditDistanceMatrix[i][j - 1] + 1
                val subsame: Int
                val mod: Modification

                if (former[j - 1].compareTo(newer[i - 1]) == 0) {
                    subsame = minEditDistanceMatrix[i - 1][j - 1]
                    mod = Modification.Keep
                } else {
                    subsame = minEditDistanceMatrix[i - 1][j - 1] + 2
                    mod = Modification.Substitution
                }

                if (insert <= delet && insert <= subsame) {
                    minEditDistanceMatrix[i][j] = insert
                    backtraceMatrix[i][j].add(Modification.Insertion)
                }
                if (delet <= insert && delet <= subsame) {
                    minEditDistanceMatrix[i][j] = delet
                    backtraceMatrix[i][j].add(Modification.Suppression)
                }
                if (subsame <= insert && subsame <= delet) {
                    minEditDistanceMatrix[i][j] = subsame
                    backtraceMatrix[i][j].add(mod)
                }
            }
        }

    }

    fun editDistance(): Int {
        return minEditDistanceMatrix[newer.size][former.size]
    }

    fun path(): List<Pair<T, Modification>> {
        var i = newer.size
        var j = former.size
        val listAction = mutableListOf<Pair<T, Modification>>()
        while (backtraceMatrix[i][j].size != 0) {
            val actions = backtraceMatrix[i][j]
            if (actions.contains(Modification.Keep)) {
                listAction.add(Pair(former[j - 1], Modification.Keep))
                i -= 1
                j -= 1
            } else if (actions.contains(Modification.Substitution)) {
                listAction.add(Pair(former[j - 1], Modification.Suppression))
                listAction.add(Pair(newer[i - 1], Modification.Insertion))
                i -= 1
                j -= 1
            } else if (actions.contains(Modification.Insertion)) {
                listAction.add(Pair(newer[i - 1], Modification.Insertion))
                i -= 1
            } else {//Suppression
                listAction.add(Pair(former[j - 1], Modification.Suppression))
                j -= 1
            }
        }
        if (i != 0 && j != 0) throw RuntimeException("error in edit distance")
        if (i != 0) {
            while (i != 0) {
                listAction.add(Pair(newer[i - 1], Modification.Insertion))
                i -= 1
            }
        } else {
            while (j != 0) {
                listAction.add(Pair(former[j - 1], Modification.Suppression))
                j -= 1
            }
        }


        return listAction.reversed()
    }

    enum class Modification {
        Substitution,
        Insertion,
        Suppression,
        Keep
    }
}
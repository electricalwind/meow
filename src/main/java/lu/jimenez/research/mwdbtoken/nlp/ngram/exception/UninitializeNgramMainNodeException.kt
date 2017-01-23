package lu.jimenez.research.mwdbtoken.nlp.ngram.exception


class UninitializeNgramMainNodeException :RuntimeException("Trying to access Ngram Main node, without having initialized it before") {
}
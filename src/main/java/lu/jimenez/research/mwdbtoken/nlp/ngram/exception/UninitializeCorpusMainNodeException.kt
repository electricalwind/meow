package lu.jimenez.research.mwdbtoken.nlp.ngram.exception


class UninitializeCorpusMainNodeException :RuntimeException("Trying to access Corpus Main node, without having initialized it before") {
}
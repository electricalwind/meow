package lu.jimenez.research.mwdbtoken.nlp.corpus.exception


class UninitializeCorpusMainNodeException :RuntimeException("Trying to access Corpus Main node, without having initialized it before") {
}
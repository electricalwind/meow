package lu.jimenez.research.mwdbtoken.exception


class UnitializeVocabularyException : RuntimeException("Trying to access vocabulary node, without having initialized it before") {}
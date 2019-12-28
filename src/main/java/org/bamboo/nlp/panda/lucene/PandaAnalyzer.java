package org.bamboo.nlp.panda.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

/**
 * the lucene {@link Analyzer} impl
 * 
 * @author xuen
 *
 */
public class PandaAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer iktokenizer = new PandaTokenizer();
		return new TokenStreamComponents(iktokenizer);
	}

}

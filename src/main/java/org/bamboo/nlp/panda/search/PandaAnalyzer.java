package org.bamboo.nlp.panda.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.bamboo.nlp.panda.PandaConf;

/**
 * the lucene {@link Analyzer} impl
 * 
 * @author xuen
 *
 */
public class PandaAnalyzer extends Analyzer {
	private final PandaConf conf;
	

	public PandaAnalyzer(PandaConf conf) {
		super();
		this.conf = conf;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer iktokenizer = new PandaTokenizer(conf);
		return new TokenStreamComponents(iktokenizer);
	}

}

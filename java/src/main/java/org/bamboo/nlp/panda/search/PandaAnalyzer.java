package org.bamboo.nlp.panda.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.json.JSONObject;

/**
 * the lucene {@link Analyzer} impl
 * 
 * @author xuen
 *
 */
public class PandaAnalyzer extends Analyzer {
	private final JSONObject conf;
	

	public PandaAnalyzer(JSONObject conf) {
		super();
		this.conf = conf;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer iktokenizer = new PandaTokenizer(conf);
		return new TokenStreamComponents(iktokenizer);
	}

}

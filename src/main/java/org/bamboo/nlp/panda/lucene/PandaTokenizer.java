package org.bamboo.nlp.panda.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.bamboo.nlp.panda.StreamSegment;
import org.bamboo.nlp.panda.Token;

/**
 * a lucene {@link Tokenizer} impl
 * 
 * @author xuen
 *
 */
public final class PandaTokenizer extends Tokenizer {
	/**
	 * token text
	 */
	private final CharTermAttribute termAtt;
	/**
	 * offset
	 */
	private final OffsetAttribute offsetAtt;
	/**
	 * the type from panda
	 */
	private final TypeAttribute typeAtt;
	/**
	 * segment
	 */
	private final StreamSegment segment;
	/**
	 * last pos
	 */
	private int endPosition;

	/**
	 * Tokenizer for Lucene 4.0
	 *
	 */
	public PandaTokenizer() {
		offsetAtt = addAttribute(OffsetAttribute.class);
		termAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		this.segment = new StreamSegment(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		Token xToken = this.segment.next();
		if (xToken != null) {
			termAtt.append(xToken.getText());
			termAtt.setLength(xToken.getLength());
			offsetAtt.setOffset(xToken.getBeginPosition(), xToken.getEndPosition());
			endPosition = xToken.getEndPosition();
			typeAtt.setType(xToken.getType());
			return true;
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		segment.reset(input);
	}

	@Override
	public final void end() throws IOException {
		super.end();
		int finalOffset = correctOffset(this.endPosition);
		offsetAtt.setOffset(finalOffset, finalOffset);
	}
}

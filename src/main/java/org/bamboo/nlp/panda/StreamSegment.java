package org.bamboo.nlp.panda;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bamboo.nlp.panda.core.PosTagger;
import org.bamboo.nlp.panda.core.WordCell;

/**
 * a stream spliter
 * 
 * @author xuen
 *
 */
public class StreamSegment implements Closeable{
	private static final int MAX_SENTENCE_LEN = 100;
	private static final char[] SENTENCE_END_CHARS = "；。，！？《》…：”“\",;\n\t\r:!?".toCharArray();
	static {
		Arrays.sort(SENTENCE_END_CHARS);
	}

	/**
	 * data
	 */
	protected Reader input;
	private final LinkedList<Token> buf;
	private final StringBuilder builder;
	private final SentenceSegment segment;
	private final PosTagger posTagger;

	/**
	 * initialize the segment
	 * @param input the input sentence
	 * @param conf the segment configuration
	 */
	public StreamSegment(Reader input, PandaConf conf) {
		super();
		this.input = input;
		this.buf = new LinkedList<Token>();
		this.builder = new StringBuilder();
		this.segment = new SentenceSegment(conf);
		this.posTagger = makeTagger(conf);
	}

	private PosTagger makeTagger(PandaConf conf2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * split data
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized Token next() throws IOException {
		if (!buf.isEmpty())// poll buf first
			return buf.pollFirst();

		this.builder.setLength(0);
		int ch;
		while ((ch = this.input.read()) >= 0) {
			char chc = (char) ch;
			this.builder.append(chc);
			if (isEnd(chc) || this.builder.length() > MAX_SENTENCE_LEN)
				break;
		}

		if (buf.isEmpty())
			return null;
		split_sentence();
		return buf.pollFirst();
	}

	private void split_sentence() throws IOException {
		// TODO cut mothod
		List<WordCell> list = this.segment.smart_cut(this.builder.toString());
		this.posTagger.tag(list);
		for (WordCell cell : list) {
			this.buf.add(new Token(cell.word, posTagger.explain(cell.getFeature())));
		}
		list.clear();
	}

	private boolean isEnd(char chc) {
		return Arrays.binarySearch(SENTENCE_END_CHARS, chc) >= 0;
	}

	public synchronized void reset(Reader input) {
		this.input = input;
		this.buf.clear();
		this.builder.setLength(0);
	}

	public void close() throws IOException {
		this.segment.close();
	}

}

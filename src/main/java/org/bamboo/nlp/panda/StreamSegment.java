package org.bamboo.nlp.panda;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * a stream spliter
 * @author xuen
 *
 */
public class StreamSegment {
	private static final int MAX_SENTENCE_LEN = 100;
	private static final char[] SENTENCE_END_CHARS = "；。，：”“\",;\n\t\r:".toCharArray();
	static {
		Arrays.sort(SENTENCE_END_CHARS);
	}
	
	private final PandaConf conf;

	/**
	 * data
	 */
	protected Reader input;
	private final LinkedList<Token> buf;
	private final StringBuilder builder;

	public StreamSegment(Reader input,PandaConf conf) {
		super();
		this.input = input;
		this.buf = new LinkedList<Token>();
		this.builder = new StringBuilder();
		this.conf=conf;
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

	private void split_sentence() {
		// TODO Auto-generated method stub

	}

	private boolean isEnd(char chc) {
		return Arrays.binarySearch(SENTENCE_END_CHARS, chc) >= 0;
	}

	public synchronized void reset(Reader input) {
		this.input = input;
		this.buf.clear();
		this.builder.setLength(0);
	}

}

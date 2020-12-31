package org.bamboo.nlp.panda;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bamboo.nlp.panda.core.PosTagger;
import org.bamboo.nlp.panda.core.WordCell;
import org.json.JSONObject;

/**
 * a stream spliter
 * 
 * @author xuen
 *
 */
public class StreamSegment implements Closeable {

	/**
	 * segment split mode
	 * 
	 * @author xuen
	 *
	 */
	private static enum SPLIT_MODE {
		MAX, SMART
	}

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
	private final SPLIT_MODE mode;

	/**
	 * initialize the segment
	 * 
	 * @param input the input sentence
	 * @param conf  the segment configuration
	 */
	public StreamSegment(Reader input, JSONObject conf) {
		super();
		this.input = input;
		this.buf = new LinkedList<Token>();
		this.builder = new StringBuilder();
		this.segment = new SentenceSegment(conf.getJSONObject("segment"));
		this.posTagger = makeTagger(conf.getJSONObject("tagger"));
		this.mode = SPLIT_MODE.valueOf(conf.getString("split.mode").toUpperCase());
	}

	private PosTagger makeTagger(JSONObject conf) {
		String cls = conf.getString("conf.class");
		try {
			if (conf.keySet().size() == 1)
				return Class.forName(cls).asSubclass(PosTagger.class).newInstance();// empty
			return Class.forName(cls).asSubclass(PosTagger.class).getConstructor(JSONObject.class).newInstance(conf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
		if (this.mode == SPLIT_MODE.SMART) {
			List<WordCell> list = this.segment.smart_cut(this.builder.toString());
			this.posTagger.tag(list);
			for (WordCell cell : list) {
				this.buf.add(new Token(cell.word, posTagger.explain(cell.getFeature())));
			}
			list.clear();
		} else if (this.mode == SPLIT_MODE.MAX) {
			List<WordCell> list = this.segment.max_cut(this.builder.toString());
			this.posTagger.tag(list);
			for (WordCell cell : list) {
				this.buf.add(new Token(cell.word, posTagger.explain(cell.getFeature())));
			}
			list.clear();
		}
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

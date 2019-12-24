package org.bamboo.nlp.panda.core;

import java.util.Collection;

/**
 * 基本单词单元
 * @author xuen
 *
 */

public class WordCell implements HtmlVisually {
	/**
	 * image str
	 */
	public final Atom word;

	/**
	 * the start in cell list
	 */
	public final int pos;

	/**
	 * the end position of this string in cell list
	 */
	public final int end;

	/**
	 * the embedding of this str
	 */
	private float[] embeding;

	public WordCell(Atom word, int pos, int end) {
		super();
		this.word = word;
		this.pos = pos;
		this.end = end;
	}

	public float[] getEmbeding() {
		return embeding;
	}

	public void setEmbeding(float[] embeding) {
		this.embeding = embeding;
	}

	public Collection<CellType> getTypes() {
		return word.getTypes();
	}

	public boolean hasType(CellType type) {
		return word.hasType(type);
	}

	public void addType(CellType type) {
		this.word.addType(type);
	}

	@Override
	public String toHtml() {
		StringBuilder b = new StringBuilder();
		b.append("<div class=\"cell\" title=\"").append("{st=").append(pos).append(",end=").append(end).append("types=")
				.append(getTypes()).append("}\">").append(word.image).append("</div>");
		return b.toString();
	}

}

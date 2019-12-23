package org.bamboo.nlp.panda.core;

import java.util.Collection;

/**
 * 基本单词单元
 * @author xuen
 *
 */

import java.util.Set;
import java.util.TreeSet;

public class WordCell implements HtmlVisually {
	/**
	 * image str
	 */
	public final CharSequence image;

	/**
	 * the start in base string
	 */
	public final int start;

	/**
	 * the end position of this string in origin string
	 */
	public final int end;

	/**
	 * the embedding of this str
	 */
	private float[] embeding;

	/**
	 * the type of this image
	 */
	private Set<CellType> types;

	public WordCell(CharSequence image, int start, int end) {
		super();
		this.image = image;
		this.start = start;
		this.end = end;
	}

	public float[] getEmbeding() {
		return embeding;
	}

	public void setEmbeding(float[] embeding) {
		this.embeding = embeding;
	}

	public Collection<CellType> getTypes() {
		return types;
	}

	public boolean hasType(CellType type) {
		if (types != null && types.contains(type))
			return true;
		return false;
	}

	public void addType(CellType type) {
		if (this.types == null)
			this.types = new TreeSet<CellType>();
		this.types.add(type);
	}

	@Override
	public String toHtml() {
		StringBuilder b = new StringBuilder();
		b.append("<div class=\"cell\" title=\"").append("{st=").append(start).append(",end=").append(end)
				.append("types=").append(types).append("}\">").append(image).append("</div>");
		return b.toString();
	}

}

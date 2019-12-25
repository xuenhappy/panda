package org.bamboo.nlp.panda.core;

import java.util.Set;
import java.util.TreeSet;

/**
 * 基本的单元
 * 
 * @author xuen
 *
 */
public final class Atom implements HtmlVisually {
	/**
	 * string of data
	 */
	public final String image;
	/**
	 * string start in ori data
	 */
	public final int begin;
	/**
	 * string end in ori data
	 */
	public final int end;

	/**
	 * the type of this image
	 */
	private Set<CellType> types;

	

	@Override
	public String toString() {
		return "Atom [image=" + image + ", begin=" + begin + ", end=" + end + ", types=" + types + "]";
	}

	public Atom(String image, int pos, int end) {
		super();
		this.types = new TreeSet<CellType>();
		this.image = image;
		this.begin = pos;
		this.end = end;
	}

	public void addType(CellType type) {
		this.types.add(type);
	}

	public Set<CellType> getTypes() {
		return types;
	}

	public boolean hasType(CellType type) {
		return types.contains(type);
	}

	@Override
	public String toHtml() {
		StringBuilder b = new StringBuilder();
		b.append("<div class=\"atom\" title=\"").append("{types=").append(getTypes()).append("}\">").append(image)
				.append("</div>");
		return b.toString();
	}
}

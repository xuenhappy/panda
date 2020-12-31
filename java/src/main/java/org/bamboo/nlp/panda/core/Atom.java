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

	/**
	 * init the atom
	 * 
	 * @param image
	 * @param pos
	 * @param end
	 * @param type  at least one type
	 */
	public Atom(String image, int pos, int end, CellType type) {
		this(image, pos, end);
		this.types.add(type);
	}

	/**
	 * init the atom
	 * 
	 * @param image
	 * @param pos
	 * @param end
	 */
	public Atom(String image, int pos, int end) {
		this.types = new TreeSet<CellType>();
		this.image = image;
		this.begin = pos;
		this.end = end;
	}

	/**
	 * add types
	 * 
	 * @param type
	 */
	public void addType(CellType[] types) {
		for (CellType c : types)
			this.types.add(c);
	}

	/**
	 * add type
	 * 
	 * @param type
	 */
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

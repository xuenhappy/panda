package org.bamboo.nlp.panda.core;

import java.util.Iterator;

/**
 * store all word cell data
 * 
 * @author xuen
 *
 */
public class CellMap implements HtmlVisually {
	/**
	 * the cursor of this mat
	 * 
	 * @author xuen
	 *
	 */
	public static class Cursor {
		public final WordCell val;
		private Cursor next;
		private Cursor pre;
		private int index = -1;

		public Cursor(Cursor pre, WordCell val, Cursor next) {
			super();
			this.val = val;
			this.next = next;
			this.pre = pre;
		}

		/**
		 * the index of this node; before call this must call indexMap
		 * 
		 * @return
		 */
		public int getIndex() {
			return index;
		}

	}

	/**
	 * index this cmap
	 */
	public void indexMap() {
		Cursor node = head;
		int index = -1;
		while (node.next != null) {
			node = node.next;
			index++;
			node.index = index;
		}
	}

	/**
	 * data
	 */
	private final Cursor head = new Cursor(null, new WordCell(null, -1, 0), null);
	private int rownum = 0;
	private int colnum = 0;
	private int elenum = 0;

	public Cursor head() {
		return head;
	}

	/**
	 * iter the data
	 * 
	 * @return
	 */
	public Iterator<Cursor> iterator() {
		return new Iterator<Cursor>() {
			private Cursor node = head;

			@Override
			public boolean hasNext() {
				return node.next != null;
			}

			@Override
			public Cursor next() {
				node = node.next;
				return node;
			}
		};
	}

	/**
	 * iter the row of data from a given pos
	 * 
	 * @param node
	 * @param row
	 * @return
	 */

	public Iterator<Cursor> iteratorRowFrom(Cursor node, int row) {
		return new Iterator<Cursor>() {
			private Cursor _node = node;
			private int _row = row;

			@Override
			public boolean hasNext() {
				while (_node != null && _node.val.begin < _row)
					_node = _node.next;
				if (_node != null && _node.val.begin == _row)
					return true;
				return false;
			}

			@Override
			public Cursor next() {
				Cursor m = _node;
				_node = _node.next;
				return m;
			}
		};
	}

	/**
	 * 
	 * @param cell
	 */
	public void addCell(WordCell cell) {
		addNext(head, cell);
	}

	/**
	 * add a new pos
	 * 
	 * @param now
	 * @param cell
	 */

	public Cursor addCell(Cursor pos, WordCell cell) {
		rownum = Math.max(rownum, cell.begin);
		colnum = Math.max(colnum, cell.end);
		if (pos.val.begin < cell.begin || (pos.val.begin == cell.begin && pos.val.end <= cell.end))
			return addNext(pos, cell);
		return addPre(pos, cell);
	}

	private Cursor addPre(Cursor pre, WordCell cell) {
		while (pre.pre != head) {
			Cursor n = pre.pre;
			if (n.val.begin > cell.begin) {// continue next to row
				pre = n;
				continue;
			}
			if (n.val.begin == cell.begin && n.val.end > cell.end) {// continue next to col
				pre = n;
				continue;
			}

			if (n.val.begin == cell.begin && n.val.end == cell.end) {// join same
				n.val.getTypes().addAll(cell.getTypes());
				return n;
			}
			Cursor m = new Cursor(n, cell, pre);
			elenum++;
			pre.pre = m;
			n.next = m;
			return m;
		}
		pre.pre = new Cursor(head, cell, pre);
		elenum++;
		return pre.pre;
	}

	private Cursor addNext(Cursor pre, WordCell cell) {
		while (pre.next != null) {
			Cursor n = pre.next;
			if (n.val.begin < cell.begin) {// continue next to row
				pre = n;
				continue;
			}
			if (n.val.begin == cell.begin && n.val.end < cell.end) {// continue next to col
				pre = n;
				continue;
			}

			if (n.val.begin == cell.begin && n.val.end == cell.end) {// join same
				n.val.getTypes().addAll(cell.getTypes());
				return n;
			}
			Cursor m = new Cursor(pre, cell, n);
			elenum++;
			pre.next = m;
			n.pre = m;
			return m;
		}
		pre.next = new Cursor(pre, cell, null);
		elenum++;
		return pre.next;
	}

	@Override
	public String toHtml() {
		StringBuilder html = new StringBuilder();
		String cell_str = "<td onmouseover=\"this.style.backgroundColor='#ffff66';\"onmouseout=\"this.style.backgroundColor='#d4e3e5';\">%s</td>";
		html.append("<table class=\"hovertable\"><tr><th></th>");
		String empty_str = String.format(cell_str, "");
		for (int i = 1; i <= colnum; i++)
			html.append(String.format("<th>%d</th>", i));
		html.append("</tr>\n");
		Iterator<Cursor> it = iterator();
		int row = -1;
		int col = 0;
		while (it.hasNext()) {
			WordCell c = it.next().val;
			if (row < c.begin) {
				if (row >= 0) {// full last
					for (int j = col + 1; j <= colnum; j++)
						html.append(empty_str);
					html.append("</tr>\n");

				}
				// full skip
				for (int i = row + 1; i < c.begin; i++) {
					html.append(String.format("<tr><th>%d</th>", i));
					for (int j = 1; j <= colnum; j++)
						html.append(empty_str);
					html.append("</tr>\n");
				}
				html.append(String.format("<tr><th>%d</th>", c.begin));
				col = 0;
			}
			// full space
			for (int j = col + 1; j < c.end; j++)
				html.append(empty_str);
			html.append(String.format(cell_str, c.toHtml()));
			row = c.begin;
			col = c.end;
		}
		if (row >= 0) {// full last
			for (int j = col + 1; j <= colnum; j++)
				html.append(empty_str);
			html.append("</tr>\n");

		}
		html.append("</table>");
		return html.toString();
	}

	/**
	 * this real element number of this mat
	 * 
	 * @return
	 */
	public int elenum() {
		return this.elenum;
	}

	/**
	 * row size
	 * 
	 * @return
	 */
	public int rowSize() {
		return this.rownum;
	}

	/**
	 * col size
	 * 
	 * @return
	 */
	public int colSize() {
		return this.colnum;
	}

}

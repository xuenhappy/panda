package org.bamboo.nlp.panda.core;

import java.util.Iterator;

/**
 * store all word cell data
 * 
 * @author xuen
 *
 */
public class CellMap implements HtmlVisually {
	public static class Cursor {
		private final WordCell val;
		private Cursor next;
		private Cursor pre;

		public Cursor(Cursor pre, WordCell val, Cursor next) {
			super();
			this.val = val;
			this.next = next;
			this.pre = pre;
		}
	}

	/**
	 * data
	 */
	private final Cursor head = new Cursor(null, new WordCell(null, -1, 0), null);
	private int rownum;
	private int colnum;
	private int elenum;

	public Cursor head() {
		return head;
	}

	/**
	 * iter all those data
	 * 
	 * @return
	 */
	public Iterator<WordCell> iterator() {
		return new Iterator<WordCell>() {
			private Cursor node = head;

			@Override
			public boolean hasNext() {
				return node.next != null;
			}

			@Override
			public WordCell next() {
				node = node.next;
				return node.val;
			}
		};
	}

	/**
	 * 
	 * @param iter row of this map
	 * @return
	 */
	public Iterator<WordCell> iteratorRow(Cursor start, int row) {
		return new Iterator<WordCell>() {
			private Cursor _node = start;
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
			public WordCell next() {
				Cursor m = _node;
				_node = _node.next;
				return m.val;
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
		elenum++;
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
			pre.pre = m;
			n.next = m;
			return m;
		}
		pre.pre = new Cursor(head, cell, pre);
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
			pre.next = m;
			n.pre = m;
			return m;
		}
		pre.next = new Cursor(pre, cell, null);
		return pre.next;
	}

	@Override
	public String toHtml() {
		StringBuilder html = new StringBuilder();
		String cell_str = "<td onmouseover=\"this.style.backgroundColor='#ffff66';\"onmouseout=\"this.style.backgroundColor='#d4e3e5';\">%s</td>";
		html.append("<table class=\"hovertable\"><tr><th></th>");
		for (int i = 1; i <= colnum; i++)
			html.append(String.format("<th>%d</th>", i));
		html.append("</tr>\n");
		Iterator<WordCell> it = iterator();
		int row = -1;
		int col = 0;
		while (it.hasNext()) {
			WordCell c = it.next();
			if (row < c.begin) {
				if (row >= 0) {// full last
					for (int j = col + 1; j <= colnum; j++)
						html.append(String.format(cell_str, ""));
					html.append("</tr>\n");

				}
				// full skip
				for (int i = row + 1; i < c.begin; i++) {
					html.append(String.format("<tr><th>%d</th>", i));
					for (int j = 1; j <= colnum; j++)
						html.append(String.format(cell_str, ""));
					html.append("</tr>\n");
				}
				html.append(String.format("<tr><th>%d</th>", c.begin));
				col = 0;
			}
			// full space
			for (int j = col + 1; j < c.end; j++)
				html.append(String.format(cell_str, ""));
			html.append(String.format(cell_str, c.toHtml()));
			row = c.begin;
			col = c.end;
		}
		if (row >= 0) {// full last
			for (int j = col + 1; j <= colnum; j++)
				html.append(String.format(cell_str, ""));
			html.append("</tr>\n");

		}
		return html.toString();
	}

	public int elenum() {
		return this.elenum;
	}

}

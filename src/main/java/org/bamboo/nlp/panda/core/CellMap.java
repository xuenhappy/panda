package org.bamboo.nlp.panda.core;

import java.util.Iterator;

/**
 * store all word cell data
 * 
 * @author xuen
 *
 */
public class CellMap implements HtmlVisually {
	public static class Node {
		private final WordCell val;
		private Node next;

		public Node(WordCell val, Node next) {
			super();
			this.val = val;
			this.next = next;
		}
	}

	/**
	 * data
	 */
	private final Node head = new Node(null, null);
	private int rownum;
	private int colnum;
	private int elenum;

	public Node head() {
		return head;
	}

	/**
	 * iter all those data
	 * 
	 * @return
	 */
	public Iterator<WordCell> iterator() {
		return new Iterator<WordCell>() {
			private Node node = head;

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
	public Iterator<WordCell> iteratorRow(Node start, int row) {
		return new Iterator<WordCell>() {
			private Node _node = start;
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
				Node m = _node;
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

	public Node addNext(Node pre, WordCell cell) {
		rownum = Math.max(rownum, cell.begin);
		colnum = Math.max(colnum, cell.end);
		elenum++;
		while (pre.next != null) {
			Node n = pre.next;
			if (n.val.begin < cell.begin || n.val.end < cell.end) {// continue next
				pre = n;
				continue;
			}
			if (n.val.begin == cell.begin && n.val.end == cell.end) {// join same
				n.val.getTypes().addAll(cell.getTypes());
				return n;
			}
			Node m = new Node(cell, n);
			pre.next = m;
			return m;
		}
		pre.next = new Node(cell, null);
		return pre.next;
	}

	@Override
	public String toHtml() {
		StringBuilder html = new StringBuilder();
		String cell_str = "<td onmouseover=\"this.style.backgroundColor='#ffff66';\"onmouseout=\"this.style.backgroundColor='#d4e3e5';\">%s</td>";
		html.append("<table class=\"hovertable\"><tr><th></th>");
		for (int i = 1; i <= colnum; i++)
			html.append(String.format("<th>%d</th>", i));
		html.append("</tr>");
		Iterator<WordCell> it = iterator();
		int row = -1;
		int col = 0;
		while (it.hasNext()) {
			WordCell c = it.next();
			if (row < c.begin) {
				if (row >= 0) {// full last
					for (int j = col + 1; j <= colnum; j++)
						html.append(String.format(cell_str, ""));
					html.append("</tr>");

				}
				// full skip
				for (int i = row + 1; i < c.begin; i++) {
					html.append(String.format("<tr><th>%d</th>", i));
					for (int j = 1; j <= colnum; j++)
						html.append(String.format(cell_str, ""));
					html.append("</tr>");
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
			html.append("</tr>");

		}
		return html.toString();
	}
	
	public int elenum() {
		return this.elenum;
	}

}

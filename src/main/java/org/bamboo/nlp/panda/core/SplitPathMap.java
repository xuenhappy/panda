package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bamboo.nlp.panda.core.CellMap.Cursor;

/**
 * split path map
 * 
 * @author xuen
 *
 */
public class SplitPathMap implements HtmlVisually {
	private final static class Path {
		// path end node
		private int node;
		// path weight
		private double weight;

		public Path(int node, double weight) {
			super();
			this.node = node;
			this.weight = weight;
		}
	}

	// path data
	private final Map<Integer, List<Path>> paths;
	// ori data
	private final CellMap cellMap;
	// best path
	private final List<Integer> bestPaths;

	/**
	 * buid a split path
	 * 
	 * @param cmap
	 * @param quantizer
	 * @throws IOException
	 */
	public SplitPathMap(CellMap cmap, CellQuantizer quantizer) throws IOException {
		this.bestPaths = new LinkedList<Integer>();
		this.cellMap = cmap;
		this.paths = new HashMap<Integer, List<Path>>(cmap.elenum() + 2);
		buidPaths(quantizer);
		optim();
	}

	/**
	 * Build map
	 * 
	 * @param quantizer
	 * @throws IOException
	 */
	private void buidPaths(CellQuantizer quantizer) throws IOException {
		cellMap.indexMap();// set index
		Iterator<Cursor> it = cellMap.iterator();
		while (it.hasNext())
			quantizer.embededing(it.next().val);
		// add head
		it = cellMap.iteratorRowFrom(cellMap.head(), 0);
		List<Path> p = new LinkedList<SplitPathMap.Path>();
		while (it.hasNext())
			p.add(new Path(it.next().getIndex(), 0));
		paths.put(-1, p);

		// add mid
		it = cellMap.iterator();
		while (it.hasNext()) {
			Cursor pre = it.next();
			p = new LinkedList<SplitPathMap.Path>();
			Iterator<Cursor> nit = cellMap.iteratorRowFrom(pre, pre.val.end);
			while (nit.hasNext()) {
				Cursor next = nit.next();
				p.add(new Path(next.getIndex(), quantizer.distance(pre.val, next.val)));
			}
			if (p.isEmpty())// add last
				p.add(new Path(cellMap.elenum(), 0));
			paths.put(pre.getIndex(), p);
		}

	}

	@Override
	public String toHtml() {
		StringBuilder html = new StringBuilder();
		String cell_str = "<td onmouseover=\"this.style.backgroundColor='#ffff66';\"onmouseout=\"this.style.backgroundColor='#d4e3e5';\">%s</td>";
		String empty_cell = String.format(cell_str, "");
		String full_cell = String.format(cell_str, "<div class=\"cell\" title=\"%.4f\">%s</div>");
		html.append("<table class=\"hovertable\"><tr><th></th>");
		for (int i = 1; i <= paths.size(); i++)
			html.append(String.format("<th>%d</th>", i));
		html.append("</tr>\n");
		Iterator<Cursor> it = cellMap.iterator();
		int row = -1;
		int col = 0;
		// add head
		it = cellMap.iteratorRowFrom(cellMap.head(), 0);
		while (it.hasNext()) {
			Cursor c = it.next();
			fullSpace(html, row, col, 0, c.getIndex() + 1, empty_cell);
			html.append(String.format(full_cell, 0.0, "#ST#@" + c.val.word.image));
			row = 0;
			col = c.getIndex() + 1;
		}
		// add mid
		it = cellMap.iterator();
		while (it.hasNext()) {
			Cursor pre = it.next();
			Iterator<Cursor> nit = cellMap.iteratorRowFrom(pre, pre.val.end);
			int j = 0;
			while (nit.hasNext()) {
				j++;
				Cursor next = nit.next();
				fullSpace(html, row, col, pre.getIndex() + 1, next.getIndex() + 1, empty_cell);
				double c = getDistance(pre.getIndex(), next.getIndex());
				html.append(String.format(full_cell, c, pre.val.word.image + "@" + next.val.word.image));
				row = pre.getIndex() + 1;
				col = next.getIndex() + 1;
			}
			if (j == 0) {
				fullSpace(html, row, col, pre.getIndex() + 1, paths.size(), empty_cell);
				html.append(String.format(full_cell, 0.0, pre.val.word.image + "@#ET#"));
				row = pre.getIndex() + 1;
				col = paths.size();
			}
		}
		if (row >= 0) {// full last
			for (int j = col + 1; j <= paths.size(); j++)
				html.append(empty_cell);
			html.append("</tr>\n");
		}
		html.append("</table>");
		return html.toString();
	}

	private double getDistance(int i, int j) {
		for (Path k : paths.get(i))
			if (k.node == j)
				return k.weight;
		return 0;
	}

	/**
	 * full content table
	 * 
	 * @param html
	 * @param row
	 * @param col
	 * @param nowRow
	 * @param nowCol
	 * @param empty_cell
	 */
	private void fullSpace(StringBuilder html, int row, int col, int nowRow, int nowCol, String empty_cell) {
		if (row < nowRow) {
			if (row >= 0) {// full last
				for (int j = col + 1; j <= paths.size(); j++)
					html.append(empty_cell);
				html.append("</tr>\n");

			}
			// full skip
			for (int i = row + 1; i < nowRow; i++) {
				html.append(String.format("<tr><th>%d</th>", i));
				for (int j = 1; j <= paths.size(); j++)
					html.append(empty_cell);
				html.append("</tr>\n");
			}
			html.append(String.format("<tr><th>%d</th>", nowRow));
			col = 0;
		}
		// full space
		for (int j = col + 1; j < nowCol; j++)
			html.append(empty_cell);
	}

	/**
	 * get the best split ,before call this must call optim
	 * 
	 * @return
	 */
	public List<WordCell> bestPath() {
		Set<Integer> s = new HashSet<Integer>(bestPaths.size());
		List<WordCell> dat = new ArrayList<WordCell>(bestPaths.size());
		Iterator<Cursor> it = this.cellMap.iterator();
		while (it.hasNext()) {
			Cursor c = it.next();
			if (s.contains(c.getIndex()))
				dat.add(c.val);
		}
		return dat;
	}

	/**
	 * Calculate shortest cut path
	 * 
	 * @param quantizer
	 */
	private void optim() {

		// TODO Calculate shortest cut path
	}

	/**
	 * clear data
	 */

	public void clear() {
		paths.clear();
		bestPaths.clear();
	}

}

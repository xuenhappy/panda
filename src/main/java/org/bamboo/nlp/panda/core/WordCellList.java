package org.bamboo.nlp.panda.core;

import java.util.Iterator;

import org.bamboo.nlp.panda.tools.StrList;

/**
 * WordCellList
 * 
 * @author xuen
 *
 */
public class WordCellList implements StrList, HtmlVisually {

	private final WordCell[] cells;

	public WordCellList(WordCell[] cells) {
		super();
		this.cells = cells;
	}

	@Override
	public Iterator<CharSequence> iterator() {
		return new Iterator<CharSequence>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < cells.length;
			}

			@Override
			public CharSequence next() {
				return cells[i++].image;
			}
		};
	}

	@Override
	public int size() {
		return cells.length;
	}

	@Override
	public String toHtml() {
		String[] colors = new String[] { "#FF9933", "#99CC99", "#CCCCCC", "#CCCC99" };
		StringBuilder html = new StringBuilder("<table class=\"textsplit\"><tr>");
		html.append("<th style=\"background-color:#ABCDEF\">Index</th>");
		for (int i = 0; i < size(); i++)
			html.append(String.format("<th style=\"background-color:%s\">%d</th>", colors[i % colors.length], i));
		html.append("</tr><tr>");
		html.append("<td style=\"background-color:#ABCDEF\">Token</td>");
		for (int i = 0; i < size(); i++)
			html.append(String.format("<td style=\"background-color:%s\">%s</td>", colors[i % colors.length],
					cells[i].image));
		html.append("</tr></table>");
		return html.toString();
	}

}

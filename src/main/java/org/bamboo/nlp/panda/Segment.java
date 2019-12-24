package org.bamboo.nlp.panda;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bamboo.nlp.panda.core.CellMap;
import org.bamboo.nlp.panda.core.CellRecognizer;
import org.bamboo.nlp.panda.core.SplitPathMap;
import org.bamboo.nlp.panda.core.WordCell;
import org.bamboo.nlp.panda.source.Resource;
import org.bamboo.nlp.panda.core.Atom;
import org.bamboo.nlp.panda.core.AtomList;
import org.bamboo.nlp.panda.core.BaseToken;
import org.bamboo.nlp.panda.tools.StrTools;

/**
 * the segment
 * 
 * @author xuen
 *
 */
public class Segment {

	/**
	 * segment conf
	 */
	private final PandaConf conf;

	/**
	 * all Recognizers
	 */
	private final List<CellRecognizer> cellRecognizers;

	/**
	 * add a recognizer
	 * 
	 * @param recognizer
	 */
	public void addCellRecognizer(CellRecognizer recognizer) {
		this.cellRecognizers.add(recognizer);
	}

	public Segment(PandaConf conf) {
		super();
		this.conf = conf;
		this.cellRecognizers = new LinkedList<CellRecognizer>();
	}

	/**
	 * cut the give data
	 * 
	 * @param str
	 * @return
	 */
	public List<WordCell> cut(CharSequence str) {
		AtomList cells = makeList(str);
		CellMap cmap = buildMap(cells);
		SplitPathMap smap = new SplitPathMap(cmap);
		smap.optim();
		return smap.bestPath();
	}

	/**
	 * cut a give data adn show it to html
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public String cutShow4Html(CharSequence str) throws IOException {
		StringBuilder html = new StringBuilder();
		html.append(
				"<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>panda分词效果记录</title><style type=\"text/css\">");
		String css_str = StrTools.readfromStream(Resource.getResource(Resource.HTML_FORMAT_CSS), "utf-8");
		html.append("\n").append(css_str).append("</style></head><body>\n");
		html.append("<div class=\"title-text\">Step 1: 基本分词</div>\n");
		AtomList cells = makeList(str);
		html.append(cells.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 2: Cell识别</div>\n");
		CellMap cmap = buildMap(cells);
		html.append(cmap.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 3: 切分图构造</div>\n");
		SplitPathMap smap = new SplitPathMap(cmap);
		smap.optim();
		html.append(smap.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 4: 切词结果</div>\n");
		List<WordCell> bestpath = smap.bestPath();
		Atom[] lists = new Atom[bestpath.size()];
		int i = 0;
		for (WordCell w : bestpath)
			lists[i++] = w.word;
		bestpath.clear();
		html.append(new AtomList(lists)).append("\n<br/><br/><br/>\n");
		html.append("</body></html>");
		return html.toString();
	}

	/**
	 * make the base list
	 * 
	 * @param str
	 * @return
	 */
	private AtomList makeList(CharSequence str) {
		if (conf.isNormal_data())
			str = StrTools.full2Half(str);
		return BaseToken.splitStr(str);
	}

	/**
	 * buid the cellmap
	 * 
	 * @param cells
	 * @return
	 */
	private CellMap buildMap(AtomList cells) {
		CellMap cmap = new CellMap();
		CellMap.Node h = cmap.head();
		for (int i = 0; i < cells.size(); i++)// add base data
			h = cmap.addNext(h, new WordCell(cells.get(i), i, i + 1));
		for (CellRecognizer recognizer : this.cellRecognizers)// add other
			recognizer.read(cells, cmap);
		return cmap;
	}

	public static void main(String[] args) throws IOException {
		PandaConf conf = new PandaConf();
		Segment sg = new Segment(conf);

		System.out.println(sg.cutShow4Html("12月23日至12月25日，明年春运火车票进入销售最高峰时段。"));
	}

}

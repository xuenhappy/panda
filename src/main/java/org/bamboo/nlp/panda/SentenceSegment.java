package org.bamboo.nlp.panda;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bamboo.nlp.panda.core.CellMap;
import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.core.CellQuantizer;
import org.bamboo.nlp.panda.core.CellRecognizer;
import org.bamboo.nlp.panda.core.SplitPathMap;
import org.bamboo.nlp.panda.core.WordCell;
import org.bamboo.nlp.panda.source.Resource;
import org.bamboo.nlp.panda.core.Atom;
import org.bamboo.nlp.panda.core.AtomList;
import org.bamboo.nlp.panda.core.BaseLex;
import org.bamboo.nlp.panda.tools.StrTools;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * the segment
 * 
 * @author xuen
 *
 */
public class SentenceSegment implements Closeable {

	/**
	 * segment conf
	 */
	private final boolean is_normal_str;

	/**
	 * all Recognizers
	 */
	private final List<CellRecognizer> cellRecognizers;

	/**
	 * this cell quantizer
	 */
	private final CellQuantizer quantizer;

	/**
	 * add a recognizer
	 * 
	 * @param recognizer
	 */
	public void addCellRecognizer(CellRecognizer recognizer) {
		this.cellRecognizers.add(recognizer);
	}

	/**
	 * init segment
	 * 
	 * @param conf
	 * @param quantizer
	 */
	public SentenceSegment(boolean is_normal_str, CellQuantizer quantizer) {
		super();
		this.is_normal_str = is_normal_str;
		this.cellRecognizers = new LinkedList<CellRecognizer>();
		this.quantizer = quantizer;
	}

	/**
	 * init data from conf
	 * 
	 * @param conf
	 */
	public SentenceSegment(JSONObject conf) {
		if (conf.has("normal.str"))
			this.is_normal_str = conf.getBoolean("normal.str");
		else
			this.is_normal_str = false;
		if (conf.has("quantizer"))
			this.quantizer = initQuantizer(conf.getJSONObject("quantizer"));
		else
			this.quantizer = null;
		this.cellRecognizers = new LinkedList<CellRecognizer>();
		if (conf.has("recognizers")) {
			JSONArray arys = conf.getJSONArray("recognizers");
			for (int i = 0; i < arys.length(); i++) {
				this.cellRecognizers.add(initCellRecognizer(arys.getJSONObject(i)));
			}
		}
	}

	private CellRecognizer initCellRecognizer(JSONObject conf) {
		String cls = conf.getString("conf.class");
		try {
			if (conf.keySet().size() == 1)
				return Class.forName(cls).asSubclass(CellRecognizer.class).newInstance();// empty
			return Class.forName(cls).asSubclass(CellRecognizer.class).getConstructor(JSONObject.class)
					.newInstance(conf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private CellQuantizer initQuantizer(JSONObject conf) {
		String cls = conf.getString("conf.class");
		try {
			if (conf.keySet().size() == 1)
				return Class.forName(cls).asSubclass(CellQuantizer.class).newInstance();// empty
			return Class.forName(cls).asSubclass(CellQuantizer.class).getConstructor(JSONObject.class)
					.newInstance(conf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * cut the give data in smart mode
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public List<WordCell> smart_cut(CharSequence str) throws IOException {
		AtomList cells = makeList(str);
		if (cells.size() == 1) {// single data
			List<WordCell> result = new ArrayList<WordCell>(1);
			result.add(new WordCell(cells.get(0), 0, 1));
			return result;
		}
		CellMap cmap = buildMap(cells);
		SplitPathMap smap = new SplitPathMap(cmap, this.quantizer, cells);
		List<WordCell> c = smap.bestPath();
		smap.clear();
		return c;
	}

	/**
	 * cut the give data in max mode
	 * 
	 * @param str
	 * @return
	 */
	public List<WordCell> max_cut(CharSequence str) {
		AtomList cells = makeList(str);
		if (cells.size() == 1) {// single data
			List<WordCell> result = new ArrayList<WordCell>(1);
			result.add(new WordCell(cells.get(0), 0, 1));
			return result;
		}
		CellMap cmap = buildMap(cells);
		LinkedList<WordCell> rs = new LinkedList<WordCell>();
		Iterator<Cursor> it = cmap.iterator();
		while (it.hasNext())
			rs.add(it.next().val);
		return rs;
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
		SplitPathMap smap = new SplitPathMap(cmap, this.quantizer,cells);
		html.append(smap.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 4: 切词结果</div>\n");
		List<WordCell> bestpath = smap.bestPath();
		Atom[] lists = new Atom[bestpath.size()];
		int i = 0;
		for (WordCell w : bestpath)
			lists[i++] = w.word;
		bestpath.clear();
		html.append(new AtomList(lists).toHtml()).append("\n<br/><br/><br/>\n");
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
		if (is_normal_str)
			str = StrTools.full2Half(str).toLowerCase();
		return BaseLex.splitStr(str);
	}

	/**
	 * buid the cellmap
	 * 
	 * @param cells
	 * @return
	 */
	private CellMap buildMap(AtomList cells) {
		CellMap cmap = new CellMap();
		CellMap.Cursor h = cmap.head();
		for (int i = 0; i < cells.size(); i++)// add base data
			h = cmap.addCell(h, new WordCell(cells.get(i), i, i + 1));
		for (CellRecognizer recognizer : this.cellRecognizers)// add other
			recognizer.read(cells, cmap);
		return cmap;
	}


	@Override
	public void close() throws IOException {
		for (CellRecognizer r : this.cellRecognizers)
			r.close();
		cellRecognizers.clear();
		quantizer.close();
	}

}

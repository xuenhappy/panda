package org.bamboo.nlp.panda;

import java.io.IOException;

import org.bamboo.nlp.panda.core.CellMap;
import org.bamboo.nlp.panda.core.AtomList;
import org.bamboo.nlp.panda.tools.StrTools;

/**
 * the segment 
 * @author xuen
 *
 */
public class Segment {
	
	
	
	public String splitShow2Html(CharSequence str) throws IOException {
		StringBuilder html=new StringBuilder();
		html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>panda分词效果记录</title><style type=\"text/css\">");
		String css_str=StrTools.readfromStream(Segment.class.getResourceAsStream("htmlformat.css"), "utf-8");
		html.append("\n").append(css_str).append("</style></head><body>\n");
		html.append("<div class=\"title-text\">Step 1: 基本分词</div>\n");
		AtomList cells=makeList(str);
		html.append(cells.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 2: Cell识别</div>\n");
		CellMap cmap=buildMap(cells);
		html.append(cmap.toHtml()).append("\n<br/><br/><br/>\n");
		html.append("<div class=\"title-text\">Step 3: 切分图构造</div>\n");
		
		html.append("<div class=\"title-text\">Step 4: 切词结果</div>\n");
		html.append("</body></html>");
		return html.toString();
		
	}
	
	private AtomList makeList(CharSequence str) {
		// TODO Auto-generated method stub
		return null;
	}

	private CellMap buildMap(AtomList cells) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new Segment().splitShow2Html("12月23日至12月25日，明年春运火车票进入销售最高峰时段。"));
	}
	
	

}

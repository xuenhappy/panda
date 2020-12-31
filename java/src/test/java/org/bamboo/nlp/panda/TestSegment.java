package org.bamboo.nlp.panda;

import java.io.IOException;

import org.bamboo.nlp.panda.core.CellQuantizer;
import org.bamboo.nlp.panda.core.ChineseNumCellRecognizer;
import org.bamboo.nlp.panda.core.DictCellRecongnizer;
import org.bamboo.nlp.panda.core.ShortLenCellQuantizer;

public class TestSegment {
	public static void main(String[] args) throws IOException {
		CellQuantizer quantizer = new ShortLenCellQuantizer();
		SentenceSegment sg = new SentenceSegment(true, quantizer);
		sg.addCellRecognizer(new DictCellRecongnizer());
		// sg.addCellRecognizer(new TTCellRecognizer());
		sg.addCellRecognizer(new ChineseNumCellRecognizer());
		// String html=sg.cutShow4Html("12月23日至12月25日，明年春运火车票进入销售最高峰时段。");
		String html = sg.cutShow4Html("三分之八的整数是三");
		System.out.println(html);
		sg.close();
	}

}

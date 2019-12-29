package org.bamboo.nlp.panda.core;

import java.io.IOException;


/**
 * Digital recognizer<br/>
 * Identify such as 10.32
 * 
 * @author xuen
 *
 */
public class NumCellRecognizer implements CellRecognizer {

	@Override
	public void read(AtomList baseStr, CellMap map) {
		StringBuilder buf = new StringBuilder();
		CellMap.Cursor cursor = map.head();
		for (int i = 0; i < baseStr.size(); i++) {
			if (i + 2 >= baseStr.size())
				continue;
			Atom a = baseStr.get(i);
			if (!a.hasType(CellType.NUM))
				continue;
			Atom b = baseStr.get(i + 1);
			if (!".".equals(b.image))
				continue;
			Atom c = baseStr.get(i + 2);
			if (!c.hasType(CellType.NUM))
				continue;
			buf.setLength(0);
			buf.append(a.image).append(b.image).append(c.image);
			WordCell w = new WordCell(new Atom(buf.toString(), a.begin, c.end), i, i + 3);
			w.addType(CellType.NUM);
			cursor = map.addCell(cursor, w);
		}
	}

	@Override
	public void close() throws IOException {

	}

}

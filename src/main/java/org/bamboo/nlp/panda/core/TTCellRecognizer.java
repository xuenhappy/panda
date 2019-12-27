package org.bamboo.nlp.panda.core;

import java.io.IOException;

/**
 * 2、3中文组合构词器，这个识别器组合所有可能的长度为2和长度为3的中文词组
 * 
 * @author xuen
 *
 */
public class TTCellRecognizer implements CellRecognizer {

	@Override
	public void read(AtomList baseStr, CellMap map) {
		CellMap.Cursor cursor = map.head();
		for (int i = 0; i < baseStr.size(); i++) {
			if (!baseStr.get(i).hasType(CellType.CHW))
				continue;
			if (i + 1 < baseStr.size()) {//add two
				if (!baseStr.get(i + 1).hasType(CellType.CHW))
					continue;
				cursor = map.addCell(cursor, new WordCell(baseStr.sub(i, i + 2), i, i + 2));

				if (i + 2 < baseStr.size()) {//add three
					if (!baseStr.get(i + 2).hasType(CellType.CHW))
						continue;
					cursor = map.addCell(cursor, new WordCell(baseStr.sub(i, i + 3), i, i + 3));
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
	}

}

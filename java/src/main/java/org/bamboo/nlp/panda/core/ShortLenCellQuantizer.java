package org.bamboo.nlp.panda.core;

import java.io.IOException;

/**
 * 基于最短路径
 * @author xuen
 *
 */
public class ShortLenCellQuantizer  implements CellQuantizer{

	@Override
	public void close() throws IOException {
	}

	@Override
	public void embed(CellMap cells,AtomList context) {
	}

	@Override
	public double distance(WordCell pre, WordCell next) {
		return 1.0;
	}

}

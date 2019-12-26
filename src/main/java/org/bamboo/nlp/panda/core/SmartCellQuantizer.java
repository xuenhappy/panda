package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.util.Iterator;

import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.tools.WordVecDic;

/**
 * a smart CellQuantizer base on wordvec and neural network
 * 
 * @author xuen
 *
 */
public class SmartCellQuantizer implements CellQuantizer {
	/**
	 * the base vec
	 */
	private final WordVecDic vecDic;
	/**
	 * the measurement
	 */
	private final Measurement measurement;

	/**
	 * the embedding presenter could null
	 */
	private final CellPresenter presenter;

	public SmartCellQuantizer(WordVecDic vecDic, Measurement measurement, CellPresenter PrePresenter) {
		super();
		this.vecDic = vecDic;
		this.measurement = measurement;
		this.presenter = PrePresenter;
	}

	@Override
	public void close() throws IOException {
		if (vecDic != null)
			vecDic.close();
	}

	@Override
	public void embed(CellMap cells, AtomList context) throws IOException {
		if (this.presenter != null)
			this.presenter.embed(cells, context);
		Iterator<Cursor> it = cells.iterator();
		while (it.hasNext()) {
			WordCell cell = it.next().val;
			float[] v = this.vecDic.embeding(cell.word.image);
			float[] r = cell.getEmbeding();
			if (v != null && r != null) {
				assert v.length == r.length;
			}

			// get type embeding
			
			//join_all_info

			// TODO Auto-generated method stub
		}
	}

	@Override
	public double distance(WordCell pre, WordCell next) throws IOException {
		return measurement.measure(pre.getEmbeding(), next.getEmbeding());
	}

}

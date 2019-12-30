package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.tools.WordVecDic;
import org.jblas.FloatMatrix;

/**
 * a smart CellQuantizer base on wordvec and neural network
 * 
 * @author xuen
 *
 */
public class SmartCellQuantizer implements CellQuantizer {
	private static final String TAG_FOMAT = "<%s>";

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
		ArrayList<float[]> embedings = new ArrayList<float[]>(10);
		Set<CellType> tmp = new HashSet<CellType>(10);
		while (it.hasNext()) {
			WordCell cell = it.next().val;
			embedings.clear();
			tmp.clear();
			// ori embedding data
			float[] r = cell.getEmbeding();
			if (r != null) {
				assert vecDic.dimSize() == r.length;
				embedings.add(r);
			}
			// dict embedding data
			float[] v = this.vecDic.embeding(cell.word.image);
			if (v != null) {
				embedings.add(v);
				cell.setEmbeding(avg(embedings));
				continue;
			}
			// types embedding data
			tmp.addAll(cell.getTypes());
			assert tmp.size() > 0;
			// filter low information tag
			if (tmp.size() > 1 && tmp.contains(CellType.UNK))
				tmp.remove(CellType.UNK);
			if (tmp.size() > 1 && tmp.contains(CellType.CHW))
				tmp.remove(CellType.CHW);
			if (tmp.size() > 1 && tmp.contains(CellType.ENG))
				tmp.remove(CellType.ENG);
			for (CellType m : tmp)
				embedings.add(this.vecDic.embeding(String.format(TAG_FOMAT, m.toString())));

			cell.setEmbeding(avg(embedings));
		}
	}

	private float[] avg(ArrayList<float[]> embedings) {
		int num = embedings.size();
		if (num == 1)// single do nothing
			return embedings.get(0);
		float[] c = embedings.get(0);
		FloatMatrix out = new FloatMatrix(1, c.length, c);
		for (int i = 1; i < embedings.size(); i++) {
			float[] m = embedings.get(i);
			out.addi(new FloatMatrix(1, m.length, m));
		}
		out.divi(embedings.size());
		return out.data;
	}

	@Override
	public double distance(WordCell pre, WordCell next) throws IOException {
		return measurement.measure(pre.getEmbeding(), next.getEmbeding());
	}

}

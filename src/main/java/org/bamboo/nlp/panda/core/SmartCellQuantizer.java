package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bamboo.mkl4j.MKL;
import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.tools.WordVecDic;

/**
 * a smart CellQuantizer base on wordvec and neural network
 * 
 * @author xuen
 *
 */
public class SmartCellQuantizer implements CellQuantizer {
	private static final String TAG_FOMAT = "<%s>";
	/**
	 * the join weight
	 */
	private final float[] join_weight;

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

	public SmartCellQuantizer(WordVecDic vecDic, Measurement measurement, CellPresenter PrePresenter,
			float[] join_weight) {
		super();
		this.vecDic = vecDic;
		this.measurement = measurement;
		this.presenter = PrePresenter;
		this.join_weight = join_weight;
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
			assert r == null || vecDic.dimSize() == r.length;
			// dict embedding data
			float[] v = this.vecDic.embeding(cell.word.image);
			if (v != null) {
				cell.setEmbeding(join(r, v));
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

			cell.setEmbeding(join(r, avg(embedings)));
		}
	}

	/**
	 * unin data and ori
	 * 
	 * @param r
	 * @param v
	 * @return
	 */
	private float[] join(float[] r, float[] v) {
		if (r == null)
			return v;
		for (int i = 0; i < v.length; i++)
			v[i] = v[i] * join_weight[i] + r[i] * (1 - join_weight[i]);
		return v;
	}

	private float[] avg(ArrayList<float[]> embedings) {
		int num = embedings.size();
		if (num == 1)// single do nothing
			return embedings.get(0);
		float[] c = embedings.get(0);
		for (int i = 1; i < embedings.size(); i++)
			MKL.vsAdd(c.length, c, 0, embedings.get(i), 0, c, 0);
		MKL.vsscal(c.length, 1.0f / embedings.size(), c, 0, 1);
		return c;
	}

	@Override
	public double distance(WordCell pre, WordCell next) throws IOException {
		return measurement.measure(pre.getEmbeding(), next.getEmbeding());
	}

}

package org.bamboo.nlp.panda.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.bamboo.mkl4j.FloatMatrix;
import org.bamboo.mkl4j.TDense;
import org.bamboo.mkl4j.TGRU;
import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.tools.IOSerializable;
import org.bamboo.nlp.panda.tools.WordVecDic;

/**
 * a lstm neural network cell presenter
 * 
 * @author xuen
 *
 */
public class LstmCellPresenter implements CellPresenter, IOSerializable {
	private TGRU<FloatMatrix> gru_fw;
	private TGRU<FloatMatrix> gru_bw;
	private TDense<FloatMatrix> map;
	private final WordVecDic char_emding_dic;

	public LstmCellPresenter(TGRU<FloatMatrix> gru_fw, TGRU<FloatMatrix> gru_bw, TDense<FloatMatrix> map,
			WordVecDic char_emding_dic) {
		super();
		this.gru_fw = gru_fw;
		this.gru_bw = gru_bw;
		this.map = map;
		this.char_emding_dic = char_emding_dic;
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void embed(CellMap cells, AtomList context) throws IOException {
		FloatMatrix[] input = new FloatMatrix[context.charLen()];
		for (int i = 0; i < context.size(); i++) {// Embedding
			Atom a = context.get(i);
			for (int j = 0; j < a.image.length(); j++) {
				String ch = String.valueOf(a.image.charAt(j));
				float[] v = char_emding_dic.embeding(ch);
				input[i] = new FloatMatrix(1, v.length, v);
			}
		}

		FloatMatrix[] fw = gru_fw.runRnn(input);
		FloatMatrix[] bw = gru_bw.reverseRunRnn(input);
		Iterator<Cursor> it = cells.iterator();
		while (it.hasNext()) {// set all embedding
			WordCell cell = it.next().val;
			FloatMatrix fw_s = fw[cell.word.begin];
			FloatMatrix bw_s = bw[cell.word.begin];
			FloatMatrix fw_e = fw[cell.word.end - 1];
			FloatMatrix bw_e = bw[cell.word.end - 1];
			cell.setEmbeding(map.forward(concatHorizontally(fw_s, bw_s, fw_e, bw_e)).toArray());
		}

	}

	private static FloatMatrix concatHorizontally(FloatMatrix A, FloatMatrix B, FloatMatrix C, FloatMatrix D) {
		float[] data = new float[A.columns + B.columns + C.columns + D.columns];
		System.arraycopy(A.toArray(), 0, data, 0, A.columns);
		System.arraycopy(B.toArray(), 0, data, A.columns, B.columns);
		System.arraycopy(C.toArray(), 0, data, A.columns + B.columns, C.columns);
		System.arraycopy(D.toArray(), 0, data, A.columns + B.columns + C.columns, D.columns);
		return new FloatMatrix(1, data.length, data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(DataInputStream in) throws IOException {
		this.gru_fw = TGRU.load(in);
		this.gru_bw = TGRU.load(in);
		this.map = TDense.load(in);
	}

	@Override
	public void save(DataOutputStream out) throws IOException {
		this.gru_fw.save(out);
		this.gru_bw.save(out);
		this.map.save(out);

	}

}

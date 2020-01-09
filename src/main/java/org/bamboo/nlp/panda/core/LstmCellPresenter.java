package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.bamboo.nlp.panda.core.CellMap.Cursor;
import org.bamboo.nlp.panda.tools.IOSerializable;
import org.bamboo.nlp.panda.tools.NeuralNetwork;
import org.bamboo.nlp.panda.tools.NeuralNetwork.TDense;
import org.bamboo.nlp.panda.tools.NeuralNetwork.TGRU;
import org.bamboo.nlp.panda.tools.WordVecDic;
import org.jblas.FloatMatrix;

/**
 * a lstm neural network cell presenter
 * 
 * @author xuen
 *
 */
public class LstmCellPresenter implements CellPresenter, IOSerializable {
	private NeuralNetwork.TGRU gru_fw;
	private NeuralNetwork.TGRU gru_bw;
	private NeuralNetwork.TDense map;
	private final WordVecDic char_emding_dic;

	/**
	 * new a empty model
	 */
	public LstmCellPresenter(WordVecDic char_emding_dic) {
		this.gru_fw = new NeuralNetwork.TGRU();
		this.gru_bw = new TGRU();
		this.map = new TDense();
		this.char_emding_dic = char_emding_dic;
	}

	public LstmCellPresenter(TGRU gru_fw, TGRU gru_bw, TDense map, WordVecDic char_emding_dic) {
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
			cell.setEmbeding(map.forward(concatHorizontally(fw_s, bw_s, fw_e, bw_e)).data);
		}

	}

	private static FloatMatrix concatHorizontally(FloatMatrix A, FloatMatrix B, FloatMatrix C, FloatMatrix D) {
		float[] data = new float[A.columns + B.columns + C.columns + D.columns];
		System.arraycopy(A.data, 0, data, 0, A.columns);
		System.arraycopy(B.data, 0, data, A.columns, B.columns);
		System.arraycopy(C.data, 0, data, A.columns + B.columns, C.columns);
		System.arraycopy(D.data, 0, data, A.columns + B.columns + C.columns, D.columns);
		return new FloatMatrix(1, data.length, data);
	}

	@Override
	public void load(InputStream in) throws IOException {
		this.gru_fw.load(in);
		this.gru_bw.load(in);
		this.map.load(in);
	}

	@Override
	public void save(OutputStream out) throws IOException {
		this.gru_fw.save(out);
		this.gru_bw.save(out);
		this.map.save(out);
	}

}

package org.bamboo.nlp.panda.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.jblas.FloatMatrix;

/**
 * the pos tagger base on crf model
 * 
 * @author xuen
 *
 */
public class CrfPosTagger implements PosTagger {
	/**
	 * tag trans param
	 */
	private final FloatMatrix trans;
	/**
	 * tag embedding map
	 */
	private final FloatMatrix map;
	/**
	 * tags explain
	 */
	private final String[] tags;

	public CrfPosTagger(String conf_data) throws IOException {
		ObjectInputStream ins = new ObjectInputStream(new FileInputStream(conf_data));
		try {
			this.trans = new FloatMatrix((float[][]) ins.readObject());
			this.map = new FloatMatrix((float[][]) ins.readObject());
			this.tags = (String[]) ins.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("data not for model :" + e.getMessage());
		} finally {
			ins.close();
		}
	}

	public CrfPosTagger(float[][] trans, float[][] map, String[] tags) {
		super();
		this.trans = new FloatMatrix(trans);
		this.map = new FloatMatrix(map);
		this.tags = tags;
	}

	/**
	 * save this model
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void save(OutputStream out) throws IOException {
		ObjectOutputStream o = new ObjectOutputStream(out);
		o.writeObject(trans.toArray2());
		o.writeObject(map.toArray2());
		o.writeObject(tags);
	}

	@Override
	public void tag(List<WordCell> tokens) {
		float[][] datas = new float[tokens.size()][];
		int i = 0;
		for (WordCell cell : tokens)
			datas[i++] = cell.getEmbeding();
		FloatMatrix score = new FloatMatrix(datas).mmul(map);
		int[] tags = viterbi_decode(score);
		i = 0;
		for (WordCell cell : tokens)
			cell.setFeature(tags[i++]);
	}

	@Override
	public String explain(int feature) {
		return tags[feature];
	}

	/**
	 * decode the data
	 */
	private int[] viterbi_decode(FloatMatrix score) {
		FloatMatrix[] trellis = new FloatMatrix[score.rows];
		int[][] backpointers = new int[score.rows][];
		trellis[0] = score.getRow(0);
		for (int t = 1; t < score.rows; t++) {
			FloatMatrix v = trans.addRowVector(trellis[t - 1]);
			trellis[t] = score.getRow(t).addi(v.columnMaxs());
			backpointers[t] = v.columnArgmaxs();
		}

		int[] viterbi = new int[score.rows];
		viterbi[viterbi.length - 1] = trellis[viterbi.length - 1].argmax();
		for (int j = viterbi.length - 2; j >= 0; j--)
			viterbi[j] = backpointers[j + 1][viterbi[j + 1]];
		// float viterbi_score=trellis[trellis.length-1].max();
		return viterbi;
	}

}

package org.bamboo.nlp.panda.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.bamboo.mkl4j.CRF;
import org.bamboo.mkl4j.FloatMatrix;
import org.bamboo.nlp.panda.tools.IOSerializable;

/**
 * the pos tagger base on crf model
 * 
 * @author xuen
 *
 */
public class CrfPosTagger implements PosTagger, IOSerializable {

	/**
	 * model
	 */
	private CRF<FloatMatrix> crf;
	/**
	 * tags explain
	 */
	private String[] tags;


	public CrfPosTagger(float[][] trans, float[][] map, String[] tags) {
		this.tags = tags;
		assert tags.length == map[0].length;
		this.crf = new CRF<FloatMatrix>(new FloatMatrix(trans), new FloatMatrix(map));
	}

	@Override
	public void tag(List<WordCell> tokens) {
		float[][] datas = new float[tokens.size()][];
		int i = 0;
		for (WordCell cell : tokens)
			datas[i++] = cell.getEmbeding();
		int[] tags = crf.viterbi_decode(new FloatMatrix(datas));
		i = 0;
		for (WordCell cell : tokens)
			cell.setFeature(tags[i++]);
	}

	@Override
	public String explain(int feature) {
		return tags[feature];
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(DataInputStream in) throws IOException {
		this.crf = (CRF<FloatMatrix>) CRF.load(in);
		this.tags = in.readUTF().split("\\s+");
	}

	@Override
	public void save(DataOutputStream out) throws IOException {
		this.crf.save(out);
		out.writeUTF(String.join("\n", tags));
	}

}

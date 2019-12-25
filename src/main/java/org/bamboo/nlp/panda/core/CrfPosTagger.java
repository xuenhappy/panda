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

	public CrfPosTagger(String  conf_data) throws IOException {
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
		o.writeObject(trans);
		o.writeObject(map);
		o.writeObject(tags);
	}

	@Override
	public void tag(List<WordCell> tokens) {
		float[][] datas = new float[tokens.size()][];
		int i = 0;
		for (WordCell cell : tokens)
			datas[i++] = cell.getEmbeding();
		FloatMatrix score = new FloatMatrix(datas).mmul(map);
		int[] tags=viterbi_decode(score);
		i=0;
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
		/**
		 *  trellis = np.zeros_like(score)
		    backpointers = np.zeros_like(score, dtype=np.int32)
		    trellis[0] = score[0]
		    
		    for t in xrange(1, score.shape[0]):
		        v = np.expand_dims(trellis[t - 1], 1) + transition_params
		        trellis[t] = score[t] + np.max(v, 0)
		        backpointers[t] = np.argmax(v, 0)
		    
		    viterbi = [np.argmax(trellis[-1])]
		    for bp in reversed(backpointers[1:]):
		        viterbi.append(bp[viterbi[-1]])
		    viterbi.reverse()
		    
		    viterbi_score = np.max(trellis[-1])
		    return viterbi, viterbi_score
		 */
		//TODO imp viterbi decode
		
		
		return null;
	}

}

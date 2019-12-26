package org.bamboo.nlp.panda.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

/**
 * 一些常见的神经网络结构
 * 
 * @author xuen
 *
 */
public final class NeuralNetwork {

	private interface AFC {
		/**
		 * 执行激活函数
		 * 
		 * @param val
		 * @return
		 */
		public FloatMatrix dothis(FloatMatrix val);
	}

	/**
	 * 常用的激活函数
	 * 
	 * @author xuen
	 *
	 */
	public static enum Activation {
		/**
		 * tanh激活
		 */
		Tanh(new AFC() {
			@Override
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.tanhi(val);
			}
		}),

		/**
		 * sigmoid激活
		 */
		Sigmoid(new AFC() {
			@Override
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.tanhi(val.divi(2)).addi(1).divi(2);
			}
		}),

		/**
		 * exp激活
		 */
		Exp(new AFC() {
			public FloatMatrix dothis(FloatMatrix val) {
				return MatrixFunctions.expi(val);
			}
		});

		private final AFC act;

		private Activation(AFC act) {
			this.act = act;
		}
	}

	/**
	 * dense层for torch
	 * 
	 * @author xuen
	 *
	 */
	public static final class TDense implements IOSerializable {
		private Activation activation;
		private FloatMatrix weight;
		private FloatMatrix blas;

		public TDense() {
		}

		public TDense(FloatMatrix weight, FloatMatrix blas, Activation activation) {
			this.activation = activation;
			this.weight = weight.transpose();
			this.blas = blas;
		}

		public FloatMatrix forward(FloatMatrix input) {
			FloatMatrix val = input.mmul(weight);
			if (blas != null)
				val = val.addiRowVector(blas);
			if (activation != null)
				val = activation.act.dothis(val);
			return val;
		}

		@Override
		public void load(InputStream in) throws IOException {
			ObjectInputStream ins = new ObjectInputStream(in);
			try {
				float[][] w = (float[][]) ins.readObject();
				float[][] v = (float[][]) ins.readObject();
				this.activation = (Activation) ins.readObject();
				this.weight = new FloatMatrix(w);
				if (v != null)
					this.blas = new FloatMatrix(v);
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}

		}

		@Override
		public void save(OutputStream out) throws IOException {
			ObjectOutputStream outs = new ObjectOutputStream(out);
			outs.writeObject(weight.toArray2());
			if (blas != null)
				outs.writeObject(blas.toArray2());
			else
				outs.writeObject(null);
			outs.writeObject(activation);
		}

	}

	/**
	 * 基础GRU层
	 * 
	 * @author xuen
	 *
	 */
	public static final class TGRU implements IOSerializable {
		private FloatMatrix wIh;
		private FloatMatrix wHh;
		private FloatMatrix bIh;
		private FloatMatrix bHh;

		/**
		 * 
		 * @return
		 */
		public FloatMatrix zeroState(int batchSize) {
			return FloatMatrix.zeros(batchSize, this.wHh.rows);
		}

		private FloatMatrix linear(FloatMatrix args, FloatMatrix weights, FloatMatrix biases) {
			FloatMatrix x = args.mmul(weights);
			if (biases != null)
				x = x.addiRowVector(biases);
			return x;
		}

		public FloatMatrix forward(FloatMatrix state, FloatMatrix input) {
			FloatMatrix gi = linear(input, wIh, bIh);
			FloatMatrix gh = linear(state, wHh, bHh);
			int len = gi.columns / 3;
			FloatMatrix i_r = gi.getRange(0, gi.rows, 0, len);
			FloatMatrix i_i = gi.getRange(0, gi.rows, len, len * 2);
			FloatMatrix i_n = gi.getRange(0, gi.rows, len * 2, len * 3);
			len = gh.columns / 3;
			FloatMatrix h_r = gh.getRange(0, gh.rows, 0, len);
			FloatMatrix h_i = gh.getRange(0, gh.rows, len, len * 2);
			FloatMatrix h_n = gh.getRange(0, gh.rows, len * 2, len * 3);

			FloatMatrix resetgate = Activation.Sigmoid.act.dothis(i_r.addi(h_r));
			FloatMatrix inputgate = Activation.Sigmoid.act.dothis(i_i.addi(h_i));
			FloatMatrix newgate = Activation.Tanh.act.dothis(i_n.addi(resetgate.muli(h_n)));
			FloatMatrix hy = newgate.addi(inputgate.muli(state.subi(newgate)));
			return hy;
		}

		public TGRU(FloatMatrix wIh, FloatMatrix wHh, FloatMatrix bIh, FloatMatrix bHh) {
			this.wHh = wHh.transpose();
			this.wIh = wIh.transpose();
			this.bHh = bHh;
			this.bIh = bIh;
		}

		public TGRU() {
		}

		public int GetStateSize() {
			return this.wHh.rows;
		}

		/**
		 * input size is [time_step,1,vec_size]
		 * 
		 * @param input
		 * @return
		 */
		public FloatMatrix[] runRnn(FloatMatrix[] input) {
			FloatMatrix[] outputs = new FloatMatrix[input.length];
			FloatMatrix state = this.zeroState(1);
			for (int i = 0; i < input.length; i++) {
				state = forward(input[i], state);// update state
				outputs[i] = state.dup();// copy state data
			}
			return outputs;
		}

		/**
		 * reverse run rnn cell input size is [time_step,1,vec_size]
		 * 
		 * @param input
		 * @return
		 */
		public FloatMatrix[] reverseRunRnn(FloatMatrix[] input) {
			FloatMatrix[] outputs = new FloatMatrix[input.length];
			FloatMatrix state = this.zeroState(1);
			for (int i = input.length - 1; i >= 0; i--) {
				state = forward(input[i], state);// update state
				outputs[i] = state.dup();// copy state data
			}
			return outputs;
		}

		@Override
		public void load(InputStream in) throws IOException {
			ObjectInputStream ins = new ObjectInputStream(in);
			try {
				float[][] w1 = (float[][]) ins.readObject();
				float[][] w2 = (float[][]) ins.readObject();
				float[][] b1 = (float[][]) ins.readObject();
				float[][] b2 = (float[][]) ins.readObject();

				this.wHh = new FloatMatrix(w1);
				this.wIh = new FloatMatrix(w2);
				if (b1 != null)
					this.bHh = new FloatMatrix(b1);
				if (b2 != null)
					this.bIh = new FloatMatrix(b2);
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}

		}

		@Override
		public void save(OutputStream out) throws IOException {
			ObjectOutputStream outs = new ObjectOutputStream(out);
			outs.writeObject(wHh.toArray2());
			outs.writeObject(wIh.toArray2());
			if (bHh != null)
				outs.writeObject(bHh.toArray2());
			else
				outs.writeObject(null);
			if (bIh != null)
				outs.writeObject(bIh.toArray2());
			else
				outs.writeObject(null);
		}
	}

}

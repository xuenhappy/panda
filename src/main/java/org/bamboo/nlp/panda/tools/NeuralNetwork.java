package org.bamboo.nlp.panda.tools;

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
	public static final class TDense {
		private final Activation activation;
		private final FloatMatrix weight;
		private final FloatMatrix blas;

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

	}

	/**
	 * 基础GRU层
	 * 
	 * @author xuen
	 *
	 */
	public static final class TGRU {
		private final FloatMatrix wIh;
		private final FloatMatrix wHh;
		private final FloatMatrix bIh;
		private final FloatMatrix bHh;

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
			FloatMatrix inputgate = Activation.Sigmoid.act.dothis(i_i.add(h_i));
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

		public int GetStateSize() {
			return this.wHh.rows;
		}

		/**
		 * input size is [time_step,vec_size]
		 * 
		 * @param input
		 * @return
		 */
		public FloatMatrix runRnn(FloatMatrix input) {
			FloatMatrix outputs = new FloatMatrix(input.rows, this.GetStateSize());
			FloatMatrix state = this.zeroState(1);
			for (int i = 0; i < input.rows; i++) {
				state = forward(input.getRow(i), state);// update state
				outputs.putRow(i, state);// copy state data
			}
			return outputs;
		}
	}

}

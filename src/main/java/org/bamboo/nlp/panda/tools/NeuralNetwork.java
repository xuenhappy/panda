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
			};
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
			FloatMatrix val = input.mul(weight);
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
			FloatMatrix x = args.mul(weights);
			if (biases != null)
				x = x.addiRowVector(biases);
			return x;
		}

		public FloatMatrix forward(FloatMatrix state, FloatMatrix input) {
			FloatMatrix gi =linear(input, wIh, bIh);
	        FloatMatrix gh =linear(state,wHh, bHh);
	        i_r, i_i, i_n = np.split(gi, 3, axis=1);
	        h_r, h_i, h_n = np.split(gh, 3, axis=1);
	        resetgate= sigmoid(i_r + h_r);
	     
	        inputgate = sigmoid(i_i + h_i);
	        		FloatMatrix newgate =Activation.Tanh.act.dothis( (i_n + resetgate * h_n));
	        FloatMatrix hy = newgate.addi( inputgate * (state - newgate))
			
			
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

		public FloatMatrix runGru(FloatMatrix input) {
			/**
			 * input: (timeSteps, tokenSize)
			 */
			FloatMatrix outputs = new FloatMatrix(input.rows, this.GetStateSize());
			FloatMatrix state = this.zeroState(1);
			for (int i = 0; i < input.rows; i++) {
				state = forward(input.getRow(i), state);
				outputs.putRow(i, state.dup());
			}
			return outputs;
		}
	}

	public static void main(String[] args) {
		FloatMatrix f1 = new FloatMatrix(new float[][] { { 1, 0, 3, 4 } });
		FloatMatrix f2 = new FloatMatrix(new float[][] { { 8, 0, 3, 4 }, { 2, 3, 4, 5 } });
		f2.addiRowVector(f1);
		System.out.println(f2);
	}

}

#!/usr/bin/env python3  
# -*- coding: utf-8 -*- 
'''
Created on Jun 4, 2017
基于numpy的tensorflow,pytorch结构的重写
@author: xuen
'''
import numpy as np
# import affinity
# import multiprocessing
# affinity.set_process_affinity_mask(0,2**multiprocessing.cpu_count()-1)

from numpy.lib.stride_tricks import as_strided
import sys


def conv1d(X, W, b=None, padding=1, stride=1):
    """
    X is [batch,in_time,in_channels]
    W is [kernel_size,in_channels, out_channels]
    return [batch_size,out_time,out_channels]
    """
    pad_X = X
    if padding > 0:
        pad_X = np.pad(pad_X, ((0, 0), (padding, padding), (0, 0)), 'constant')
    batch, in_time, in_channels = np.shape(pad_X)
    kernel_size, in_channels, _ = np.shape(W)
    out_time = (in_time - kernel_size) // stride + 1
    s = (batch, out_time, kernel_size, in_channels)
    (a1, a2, a3) = pad_X.strides
    d = (a1, a2 * stride, a2, a3)
    subM = as_strided(pad_X, shape=s, strides=d)
    v = np.einsum('lkn,ijlk->ijn', W, subM)
    if b is not None:
        return np.add(v, b, out=v)
    return v


def viterbi_decode(score, transition_params):
    """Decode the highest scoring sequence of tags outside of TensorFlow.
    
    This should only be used at test time.
    
    Args:
      score: A [seq_len, num_tags] matrix of unary potentials.
      transition_params: A [num_tags, num_tags] matrix of binary potentials.
    
    Returns:
      viterbi: A [seq_len] list of integers containing the highest scoring tag
          indices.
      viterbi_score: A float containing the score for the Viterbi sequence.
    """
    trellis = np.zeros_like(score)
    backpointers = np.zeros_like(score, dtype=np.int32)
    trellis[0] = score[0]

    for t in range(1, score.shape[0]):
        v = np.expand_dims(trellis[t - 1], 1) + transition_params
        trellis[t] = score[t] + np.max(v, 0)
        backpointers[t] = np.argmax(v, 0)

    viterbi = [np.argmax(trellis[-1])]
    for bp in reversed(backpointers[1:]):
        viterbi.append(bp[viterbi[-1]])
    viterbi.reverse()

    viterbi_score = np.max(trellis[-1])
    return viterbi, viterbi_score


def relu(x):
    return np.maximum(x, 0, x)


def tanh(x):
    return np.tanh(x, x)

def softmax(x, mask=None, dim=0):
    max_v = x.max(axis=dim, keepdims=True)
    x = x-max_v
    np.exp(x, out=x)
    if mask is not None:
        np.multiply(x, mask, out=x)
    sum_v = np.sum(x, axis=dim, keepdims=True)
    np.maximum(sum_v, 1e-12, out=sum_v)
    return np.divide(x, sum_v, x)



def sigmoid(x):
    x1 = np.negative(x)
    x2 = np.exp(x1, x1)
    x3 = np.add(x2, 1.0, x2)
    return np.reciprocal(x3, x3)


"""
Torch net
"""


class EmbeddingTorch:
    def __init__(self, weights, unk_vector=None):
        self.num_embeddings = weights.shape[0]
        self.embedding_dim = weights.shape[1]
        self.unk_vector = np.mean(weights, axis=0) if unk_vector is None else unk_vector

        self.weights = np.concatenate((weights, [self.unk_vector]), axis=0)  # shape(5737,50)

    def __call__(self, inputs):
        np.place(inputs, inputs >= self.num_embeddings, [self.num_embeddings + 1])
        return np.take(self.weights, inputs, axis=0)


class PositionalEmbeddingTorch:
    def __init__(self, weights):
        self.num_embeddings = weights.shape[0]
        self.embedding_dim = weights.shape[1]
        self.weights = weights

    def __call__(self, inputs):
        return np.take(self.weights, inputs % self.num_embeddings, axis=0)


class Conv1dTorch:
    def __init__(self, weights, biases=None):
        self.out_channels = weights.shape[0]
        self.in_channels = weights.shape[1]
        self.kernel_size = weights.shape[2]
        assert self.kernel_size % 2 == 1, "kernel_size only support odd number now"
        self.padding = (self.kernel_size - 1) // 2  # 被2整除

        # self.weights = weights
        self.weights = np.transpose(weights, axes=(2, 1, 0))
        self.biases = biases

    def __call__(self, inputs, stride=1):
        """
        X is [batch,time,in_channels]
        W is [kernel_size,in_channels, out_channels]
        """
        X = np.transpose(inputs, axes=(0, 2, 1))
        pad_X = X
        if self.padding > 0:
            pad_X = np.pad(X, ((0, 0), (self.padding, self.padding), (0, 0)), 'constant')
        batch, in_time, in_channels = np.shape(pad_X)
        kernel_size, in_channels, out_channels = np.shape(self.weights)
        out_time = (in_time - kernel_size) // stride + 1
        s = (batch, out_time, kernel_size, in_channels)
        (a1, a2, a3) = pad_X.strides
        d = (a1, a2 * stride, a2, a3)
        subM = as_strided(pad_X, shape=s, strides=d)

        # v: (batch_size, tokens_size, token_size)
        v = np.einsum('lkn,ijlk->ijn', self.weights, subM)
        if self.biases is not None:
            v = np.add(v, self.biases, out=v)

        # outputs: (batch_size, token_size, tokens_size)
        outputs = np.transpose(v, axes=(0, 2, 1))
        return outputs


class CrfTorch:
    def __init__(self, weights, tag2idx, start_tag, stop_tag):
        self.transitions = weights
        self.tag2idx = tag2idx
        self.tag_size = len(self.tag2idx)
        self.start_tag = start_tag
        self.stop_tag = stop_tag

    def __call__(self, features):
        backpointers = []

        # Initialize the viterbi variables in log space
        init_vvars = np.full((1, self.tag_size), -10000.)
        init_vvars[0][self.tag2idx[self.start_tag]] = 0

        # forward_var at step i holds the viterbi variables for step i-1
        forward_var = init_vvars
        for feature in features:
            bptrs_t = []  # holds the backpointers for this step
            viterbivars_t = []  # holds the viterbi variables for this step

            for next_tag in range(self.tag_size):
                # next_tag_var[i] holds the viterbi variable for tag i at the
                # previous step, plus the score of transitioning
                # from tag i to next_tag.
                # We don't include the emission scores here because the max
                # does not depend on them (we add them in below)
                next_tag_var = forward_var + self.transitions[next_tag]
                best_tag_id = np.argmax(next_tag_var)
                bptrs_t.append(best_tag_id)
                viterbivars_t.append(next_tag_var[0][best_tag_id].reshape(1))
            # Now add in the emission scores, and assign forward_var to the set
            # of viterbi variables we just computed
            forward_var = (np.concatenate(viterbivars_t) + feature).reshape(1, -1)
            backpointers.append(bptrs_t)

        # Transition to stop_tag
        terminal_var = forward_var + self.transitions[self.tag2idx[self.stop_tag]]
        best_tag_id = np.argmax(terminal_var)
        path_score = terminal_var[0][best_tag_id]

        # Follow the back pointers to decode the best path.
        best_path = [best_tag_id]
        for bptrs_t in reversed(backpointers):
            best_tag_id = bptrs_t[best_tag_id]
            best_path.append(best_tag_id)
        # Pop off the start tag (we dont want to return that to the caller)
        start = best_path.pop()
        assert start == self.tag2idx[self.start_tag]  # Sanity check
        best_path.reverse()  # 5441个1组成的列表
        return path_score, best_path


class SelfAttentionTorch:
    def __init__(self, query_weight, key_weight, value_weight):
        self.query_weight = query_weight
        self.key_weight = key_weight
        self.value_weight = value_weight

        self.sqrt_key_weight_size = np.sqrt(self.key_weight.shape[1])

    @staticmethod
    def softmax(inputs):
        """
        Compute the softmax function for each row of the input x.

        Arguments:
        inputs -- A N dimensional vector or M x N dimensional numpy matrix.

        Return:
        outputs -- A N dimensional vector or M x N dimensional numpy matrix.
        """
        exp = np.apply_along_axis(lambda x: np.exp(x - np.max(x)), -1, inputs)
        denominator = np.apply_along_axis(lambda x: 1.0 / np.sum(x), -1, exp)

        outputs = exp * np.repeat(
            denominator.reshape(*denominator.shape, -1),
            inputs.shape[-1], axis=-1
        )
        return outputs

    def __call__(self, inputs, mask=None):
        # inputs: (..., tokens_size, token_size)
        queries = np.matmul(inputs, self.query_weight)
        keys = np.matmul(inputs, self.key_weight)
        values = np.matmul(inputs, self.value_weight)

        scores = np.matmul(
            queries, keys.swapaxes(-1, -2)
        ) / self.sqrt_key_weight_size

        if mask is not None:
            scores = np.ma.array(scores, mask=mask, fill_value=float('-inf')).filled()

        attention_weights = SelfAttentionTorch.softmax(scores)

        if mask is not None:
            attention_weights = np.ma.array(attention_weights, mask=mask, fill_value=0.).filled()

        outputs = np.matmul(attention_weights, values), attention_weights

        return outputs


class RnnLayer():
    def zero_state(self, batch_size, dtype=np.float32):
        raise NotImplementedError()

    def mask_state(self, mask, new_state, pre_state):
        raise NotImplementedError()


class Dense_Torch():
    def __init__(self, weights, biases=None, activation=None):
        self._activation = activation
        self._weights = np.transpose(weights)
        self._biases = biases

    def __call__(self, inputs):
        res = np.dot(inputs, self._weights)
        if self._biases is not None:
            res = np.add(res, self._biases)
        if self._activation is not None:
            res = self._activation(res)
        return res


class LSTMCell_Torch(RnnLayer):
    def __init__(self, w_ih, w_hh, b_ih=None, b_hh=None):
        self.w_ih = np.transpose(w_ih)
        self.w_hh = np.transpose(w_hh)
        self.b_ih = b_ih
        self.b_hh = b_hh
        self._state_size = (np.shape(self.w_hh)[0] // 4, np.shape(self.w_hh)[0] // 4)

    def zero_state(self, batch_size, dtype=np.float32):
        return [np.zeros((batch_size, o), dtype=dtype) for o in self._state_size]

    def mask_state(self, mask, new_state, pre_state):
        return (np.where(mask, a, b) for a, b in (new_state, pre_state))

    def _linear(self, args, weights, biases):
        x = np.dot(args, weights)
        if biases is not None:
            x = np.add(x, biases, x)
        return x

    def __call__(self, inputs, state):
        hx, cx = state
        gates = self._linear(inputs, self.w_ih, self.b_ih) + self._linear(hx, self.w_hh, self.b_hh)
        ingate, forgetgate, cellgate, outgate = np.split(gates, 4, axis=1)
        ingate, forgetgate, cellgate, outgate = sigmoid(ingate), sigmoid(forgetgate), np.tanh(cellgate), sigmoid(
            outgate)
        cy = (forgetgate * cx) + (ingate * cellgate)
        hy = outgate * np.tanh(cy)
        return hy, (hy, cy)


class GRUCell_Torch(RnnLayer):
    def __init__(self, w_ih, w_hh, b_ih=None, b_hh=None):
        self.w_ih = np.transpose(w_ih)
        self.w_hh = np.transpose(w_hh)
        self.b_ih = b_ih
        self.b_hh = b_hh
        self._state_size = np.shape(self.w_hh)[0]

    def zero_state(self, batch_size, dtype=np.float32):
        return np.zeros((batch_size, self._state_size), dtype=dtype)

    def mask_state(self, mask, new_state, pre_state):
        return np.where(mask, new_state, pre_state)

    def _linear(self, args, weights, biases):
        x = np.dot(args, weights)
        if biases is not None:
            x = np.add(x, biases, x)
        return x

    def __call__(self, inputs, state):
        gi = self._linear(inputs, self.w_ih, self.b_ih)
        gh = self._linear(state, self.w_hh, self.b_hh)
        # i_r, i_i, i_n = np.split(gi, 3, axis=1)
        # h_r, h_i, h_n = np.split(gh, 3, axis=1)
        [i_r, i_i, i_n] = split(gi, 3, axis=1)
        [h_r, h_i, h_n] = split(gh, 3, axis=1)
        resetgate, inputgate = sigmoid(i_r + h_r), sigmoid(i_i + h_i)
        newgate = np.tanh(i_n + resetgate * h_n)
        hy = newgate + inputgate * (state - newgate)
        return hy, hy


"""
TF net
"""


class Dense_TF():
    def __init__(self, weights, biases=None, activation=None):
        self._activation = activation
        self._weights = weights
        self._biases = biases

    def __call__(self, inputs):
        res = np.dot(inputs, self._weights)
        if self._biases is not None:
            res = np.add(res, self._biases)
        if self._activation is not None:
            res = self._activation(res)
        return res


class BasicRNNCell_TF(RnnLayer):
    def __init__(self, g_weights, g_biases, activation=np.tanh):
        self._g_weights = g_weights
        self._g_biases = g_biases
        self._num_units = np.shape(g_weights)[1]
        self._activation = activation

    def _linear(self, args, weights, biases):
        c = np.dot(np.concatenate(args, axis=1), weights)
        return np.add(c, biases, c)

    def zero_state(self, batch_size, dtype=np.float32):
        return np.zeros((batch_size, self._num_units), dtype=dtype)

    def mask_state(self, mask, new_state, pre_state):
        return np.where(mask, new_state, pre_state)

    def __call__(self, inputs, state):
        d1 = self._linear([inputs, state], self._g_weights, self._g_biases)
        new_h = self._activation(d1)
        return new_h, new_h


class GRUCell_TF(RnnLayer):
    def __init__(self, g_weights, g_biases, c_weights, c_biases, activation=np.tanh):
        self._g_weights = g_weights
        self._g_biases = g_biases
        self._c_weights = c_weights
        self._c_biases = c_biases
        self._num_units = np.shape(g_weights)[1] // 2
        self._activation = activation

    def _linear(self, args, weights, biases):
        res = np.dot(np.concatenate(args, axis=1), weights)
        return np.add(res, biases, res)

    def zero_state(self, batch_size, dtype=np.float32):
        return np.zeros((batch_size, self._num_units), dtype=dtype)

    def mask_state(self, mask, new_state, pre_state):
        return np.where(mask, new_state, pre_state)

    def __call__(self, inputs, state):
        d1 = self._linear([inputs, state], self._g_weights, self._g_biases)
        r, u = np.split(d1, 2, axis=1)
        r, u = sigmoid(r), sigmoid(u)
        d2 = self._linear([inputs, r * state], self._c_weights, self._c_biases)
        c = self._activation(d2)
        new_h = u * state + (1 - u) * c
        return new_h, new_h


class JANet(RnnLayer):
    def __init__(self, g_weights, g_biases, c_weights, c_biases):
        self._g_weights = g_weights
        self._g_biases = g_biases
        self._c_weights = c_weights
        self._c_biases = c_biases
        self._num_units = np.shape(g_weights)[1]

    def _linear(self, arg, weights, biases):
        res = np.dot(arg, weights)
        return np.add(res, biases, res)

    def zero_state(self, batch_size, dtype=np.float32):
        return np.zeros((batch_size, self._num_units), dtype=dtype)

    def mask_state(self, mask, new_state, pre_state):
        return np.where(mask, new_state, pre_state)

    def __call__(self, inputs, state):
        arg = np.concatenate([inputs, state], axis=1)
        d1 = self._linear(arg, self._g_weights, self._g_biases)
        u = sigmoid(d1)
        d2 = self._linear(arg, self._c_weights, self._c_biases)
        c = tanh(d2)
        new_h = u * state + (1 - u) * c
        return new_h, new_h


class LSTMCell_TF(RnnLayer):
    def __init__(self, weights, biases, peepholes_tuple=None, \
                 project_weights=None, forget_bias=1.0, activation=np.tanh):
        """
        peepholes_tuple=(w_f_diag, w_i_diag, w_o_diag)
        """
        self._forget_bias = forget_bias
        self._activation = activation
        self._weights = weights
        self._biases = biases
        self._project_weights = project_weights
        self._peepholes_tuple = peepholes_tuple
        num_units = np.shape(weights)[1] // 4
        if project_weights is not None:
            num_proj = np.shape(project_weights)[1]
            self._state_size = (num_units, num_proj)
            self._output_size = num_proj
        else:
            self._state_size = (num_units, num_units)
            self._output_size = num_units

    def _linear(self, args, weights, biases=None):
        if len(args) == 1:
            res = np.dot(args[0], weights)
        else:
            res = np.dot(np.concatenate(args, axis=1), weights)
            if biases is not None:
                res = np.add(res, biases, res)
        return res

    def zero_state(self, batch_size, dtype=np.float32):
        return [np.zeros((batch_size, o), dtype=dtype) for o in self._state_size]

    def mask_state(self, mask, new_state, pre_state):
        return (np.where(mask, a, b) for a, b in (new_state, pre_state))

    def __call__(self, inputs, state):
        (c_prev, m_prev) = state
        lstm_matrix = self._linear((inputs, m_prev), self._weights, self._biases)
        i, j, f, o = np.split(lstm_matrix, 4, axis=1)
        if self._peepholes_tuple is not None:
            w_f_diag, w_i_diag, w_o_diag = self._peepholes_tuple
            c = (sigmoid(f + self._forget_bias + w_f_diag * c_prev) * c_prev + sigmoid(
                i + w_i_diag * c_prev) * self._activation(j))
            m = sigmoid(o + w_o_diag * c) * self._activation(c)
        else:
            c = (sigmoid(f + self._forget_bias) * c_prev + sigmoid(i) * self._activation(j))
            m = sigmoid(o) * self._activation(c)
        if self._project_weights is not None:
            m = self._linear([m], self._project_weights)

        new_state = (c, m)
        return m, new_state


"""
other
"""


def split(X, num, axis=0, byte=4):
    if num > X.shape[axis] or X.shape[axis] % num != 0:
        print('num is error!')
        raise ValueError
    shapes = list(X.shape)
    strides = list(X.strides)
    shapes[axis] = int(shapes[axis] / num)
    shapes.insert(0, num)
    strides.insert(0, shapes[-1] * byte)
    x = np.lib.stride_tricks.as_strided(X, shape=shapes, strides=strides)
    return x


def static_rnn(rnn_cell, _input, init_state=None, batch_length=None):
    """
    _input: (time_steps, batch_size, token_size)
    input_lens: ([batch_size])or (batch_size)
    """
    outputs = []
    x = _input[0]
    shapes = np.shape(x)
    state = rnn_cell.zero_state(shapes[0], np.float32)
    if init_state is not None:
        state = init_state
    if batch_length:
        batch_length = np.reshape(batch_length, (-1, 1))
        zeros_out = None
        for t, x in enumerate(_input):
            output, nstate = rnn_cell(x, state)
            cond = t < batch_length
            if zeros_out is None:
                zeros_out = np.zeros_like(output)
            outputs.append(np.where(cond, output, zeros_out))
            state = rnn_cell.mask_state(cond, nstate, state)
    else:
        for x in _input:
            output, state = rnn_cell(x, state)
            outputs.append(output)

    return (outputs, state)


def load_variable(filepath, debug=False):
    r = np.load("%s.npz" % filepath)
    names = dict(r.items())
    if debug:
        for k, v in names.items():
            print("%s:%s:%s" % (k, v.dtype, np.shape(v)), file=sys.stderr)
    return names

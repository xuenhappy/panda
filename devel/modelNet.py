#!/usr/bin/env python
# -*- coding: utf-8 -*-

'''
@author: enxu
'''
import utils
import numpy as np
import torch
import torch.nn as nn
from torch.nn import functional as F


class SMelmo(nn.Module):
    def __init__(self, token_size):
        super().__init__()
        self.embeding = nn.Embedding(token_size, 150)
        self.drop = nn.Dropout(0.5)
        self.rnn = nn.GRU(150, 180, batch_first=True, bidirectional=True)
        self.map = nn.Linear(180*2, 150)
        self.bd = nn.BatchNorm1d(150)

    def forward(self, input, seq_lengths, input_add=None):
        """
        input is a long tensor and shape is (N,T),
        seq_lengths is a long tensor and shape is (N,)
        seq_length and inpt must be in same device
        """
        embeding = self.drop(self.embeding(input))
        if input_add is not None:
            embeding += input_add
        out = self.map(run_rnn(self.rnn, embeding, seq_lengths))+embeding
        return self.bd(out.view(-1, out.size(2))).view(out.shape)


def init_param(model):
    for p in model.parameters():
        if len(p.shape) == 2:
            nn.init.xavier_uniform_(p)
            continue
        nn.init.zeros_(p)


def batch_select(tensor, index):
    return tensor.gather(1, index.view(-1, 1, 1).expand(tensor.size(0), 1, tensor.size(2))).squeeze(1)


def abs_max(x, dim):
    # return max positive num or min negtive num or if both max+min
    max_, _ = x.max(dim)
    min_, _ = x.min(dim)
    max_ = max_ * (max_ > 0).float()
    min_ = min_ * (min_ < 0).float()
    return max_ + min_


class SentenceEncoder(nn.Module):
    """
    init the sentence
    """

    def __init__(self, word_num, out_dim):
        nn.Module.__init__(self)
        self.embeds = nn.Embedding(word_num, 100)
        self.encrnn = nn.GRU(
            input_size=100,
            hidden_size=200,
            num_layers=1,
            batch_first=True,
            bidirectional=True
        )
        self.weight = torch.Parameter(torch.Tensor(out_dim, 800))
        init_param(self.encrnn)
        init_param(self.weight)

    def forward(self, batch_sentence, batch_sentence_length, keep_prop):
        """
        batch_sentence shape:batch_size×word_size
        batch_sentence_length:batch_size
        """
        batch_titles_emb = self.embeds(batch_titles)
        batch_titles_emb = F.dropout(batch_titles_emb, 1 - keep_prop, keep_prop < 1.0)
        title_vecs = RunRnn(self.encrnn, batch_titles_conv, batch_title_length)
        return F.linear(title_vecs, self.weight[:, :400], None), F.linear(title_vecs, self.weight[:, 400:], None)


class Quantizer(nn.Module):

    def __init__(self, input_size):
        nn.Module.__init__(self)
        self.Kmap = nn.Linear(input_size, 50)
        self.Vmap = nn.Linear(input_size, 50)
        self.bnorm = nn.BatchNorm1d(1)

    def distance(self, x, y):
        dist = (self.Kmap(x)*self.Vmap(y)).sum(-1)
        return F.selu(self.bnorm(dist))


class SentencePredict(nn.Module):
    """
    init the sentence
    """

    def __init__(self, word_num, emb_size, tags_weight, nsampled):
        super(SentencePredict, self).__init__()
        self.predictor = SentenceEncoder(word_num, emb_size)
        self.embedd = utils.SampledSoftMaxCrossEntropy(emb_size, tags_weight, nsampled)

    def forward(self, batch_sentence, batch_tags_with_idx, keep_prop):
        """
        batch_sentence shape:batch_size×word_size
        batch_tags_with_idx:list((start,end,tag))
        """
        batch_sentence_length = []
        batch_indices_st = []
        batch_indices_et = []
        batch_tags = []

        for i, batch in enumerate(batch_tags_with_idx):
            offset = i*batch_sentence.shape[1]
            for s, e, tag in batch:
                batch_indices_st.append(offset+s)
                batch_indices_et.append(offset+e)
                batch_tags.append(batch_tags)
            batch_sentence_length.append(batch[-1][1])

        batch_sentence = torch.from_numpy(batch_sentence)
        batch_sentence_length = torch.LongTensor(batch_sentence_length)
        batch_indices_st = torch.LongTensor(batch_indices_st)
        batch_indices_et = torch.LongTensor(batch_indices_et)
        batch_tags = torch.LongTensor(batch_tags)

        if torch.cuda.is_available():
            batch_sentence = batch_sentence
            batch_sentence_length = batch_sentence_length.cuda()
            batch_indices_st = batch_indices_st.cuda()
            batch_indices_et = batch_indices_et.cuda()
            batch_tags = batch_tags.cuda()

        batch_sentence_fw_encoding, batch_sentence_bw_encoding = self.predictor(batch_sentence, batch_sentence_length, keep_prop)
        word_embs_st = batch_sentence_fw_encoding.reshape(-1, emb_size).index_select(0, batch_indices_st)
        word_embs_et = batch_sentence_bw_encoding.reshape(-1, emb_size).index_select(0, batch_indices_et)
        word_embs = word_embs_st + word_embs_et
        return self.embedd(word_embs, batch_tags).mean()

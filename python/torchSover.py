# coding=utf-8
'''
File: torchsover.py
Project: workAbout
File Created: Thursday, 18th June 2020 4:57:40 pm
Author: enxu (xuen@mokar.com)
-----
Last Modified: Thursday, 18th June 2020 4:57:44 pm
Modified By: enxu (xuen@mokahr.com)
-----
Copyright 2021 - 2020 Your Company, Moka
'''
import sys
import os
import time
import numpy as np
import torch
from torch import optim
import math


def save_torch_model(model, out_dir, epoch_num):
    if not os.path.exists(out_dir):
        os.makedirs(out_dir)
    # save model for torch
    torch.save(model, os.path.join(out_dir, "model-%d.pkl" % epoch_num))
    # save model for numpy
    state_dict = dict((k, v.cpu().numpy()) for (k, v) in model.state_dict().items())
    np.savez(os.path.join(out_dir, "model-%d" % epoch_num), **state_dict)
    print("Write model to {}".format(out_dir))


def load_init_model(model, modeldir):
    model_path = os.path.join(modeldir, "model-init.npz")
    if not os.path.exists(model_path):
        return
    param_dict = dict((k, torch.from_numpy(v)) for k, v in np.load(model_path).items())
    model.load_state_dict(param_dict)
    print("Load model from {}".format(model_path))


class TeachSolver():
    """
    有监督分类模型求解器
    """

    def __init__(self, model, train_iter, test_iter=None, conf={}):
        self.model = model
        if torch.cuda.is_available():
            self.model = self.model.cuda()
        self.train_iter = train_iter
        self.test_iter = test_iter
        for (k, v) in conf.items():
            setattr(self, k, v)
        learn_rate = 0.001 if not hasattr(self, "lrate") else self.lrate
        self.lrate = learn_rate
        weight_p, bias_p = [], []
        for name, p in self.model.named_parameters():
            if 'bias' in name:
                bias_p.append(p)
            else:
                weight_p.append(p)

        self.optimizer = optim.RMSprop([
            {'params': weight_p, 'weight_decay': 1e-5},
            {'params': bias_p, 'weight_decay': 0}
        ], lr=learn_rate, alpha=0.9)

        modeldir = os.path.abspath(os.path.join(os.path.curdir, self.model_outdir))
        self.out_dir = os.path.join(modeldir, str(int(time.time())))
        load_init_model(self.model, modeldir)

    def solve(self):
        step, epoch_num, sample_num = 0, 0, 0
        losses = []
        while epoch_num < self.epoch_num:  # 全部样本的迭代次数
            epoch_num += 1
            print("start %d ecoph iter train" % epoch_num)
            for td in self.train_iter:
                sample_num += 1
                iter_num = 0
                while iter_num < self.iter_num:
                    iter_num += 1
                    step += 1
                    self.optimizer.zero_grad()
                    loss = self.model(*td)
                    losses.append(loss)
                    if len(losses) >= 50:
                        all_loss = sum(losses)/len(losses)
                        losses = []
                        all_loss.backward()
                        # torch.nn.utils.clip_grad_norm_(self.model.parameters(), 1, 'inf')
                        # for p in self.model.parameters():
                        #     print(p.grad, p.shape)
                        self.optimizer.step()
                        print('train [epoch|sample|step]:[%d|%d|%d] loss:%g' % (epoch_num, sample_num, step, float(all_loss)))
                if sample_num % 200000 == 0:
                    save_torch_model(self.model, self.out_dir, epoch_num)

            save_torch_model(self.model, self.out_dir, epoch_num)
            self.train_iter.reset()

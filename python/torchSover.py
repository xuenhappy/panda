#!/usr/bin/env python  
# -*- coding: utf-8 -*- 
'''
Created on Jan 4, 2018
基于pytorch的求解器
@author: xuen
'''
import os
import time
import numpy as np
import torch
from torch import optim
import math



def plot_lr_decay_pic(start_lr=4e-2,across_step=200000,across_value=2e-3,max_step=500000):
    try:
        import matplotlib.pyplot as plt
        plt.style.use('ggplot')
        decay_rate=math.log(across_value/start_lr)/across_step
        x=np.arange(1,max_step).astype(np.float32)
        y=start_lr*np.exp(decay_rate*x)
        plt.plot(x,y)
        plt.title("$dr=%.5f$"%decay_rate)
        plt.grid(c='g')
        plt.savefig('lr.png', format='png',dpi=800)
        print ("decay_rate=%f"%decay_rate)
    except:
        pass
    

    
    
def save_torch_model(model, out_dir, epoch_num):
    # save model for torch
    torch.save(model, os.path.join(out_dir, "model-%d.pkl" % epoch_num))
    # save model for numpy
    state_dict = dict((k, v.cpu().numpy()) for (k, v) in model.state_dict().items())
    np.savez(os.path.join(out_dir, "model-%d" % epoch_num), **state_dict)


class TeachSolver():
    """
    有监督分类模型求解器
    """

    def __init__(self, model, train_iter, test_iter=None, conf={}):
        if torch.cuda.is_available():
            self.model = model.cuda()
        else:
            self.model = model
        self.train_iter = train_iter
        self.test_iter = test_iter
        for (k, v) in conf.items():
            setattr(self, k, v)    
        
    def build(self):
        learn_rate = 5e-2 if not hasattr(self, "lrate") else self.lrate
        self.lrate = learn_rate
        weight_p, bias_p = [], []
        for name, p in self.model.named_parameters():
            if 'bias' in name:
                bias_p += [p]
            else:
                weight_p += [p]

        self.optimizer = optim.SGD([
            {'params': weight_p, 'weight_decay':1e-5},
            {'params': bias_p, 'weight_decay':0}
        ], lr=learn_rate)

        timestamp = str(int(time.time()))
        out_dir = os.path.abspath(os.path.join(os.path.curdir, self.model_outdir, timestamp))
        print ("Write model to {}".format(out_dir))
        if not os.path.exists(out_dir):
            os.makedirs(out_dir)
        self.out_dir = out_dir

    def adjust_learning_rate(self, epoch):
        """Sets the learning rate to the initial LR decayed by 10 every 30 epochs"""
        decay_rate=-8.04718956217e-06
        lr = self.lrate*math.exp(decay_rate*epoch)
        lr=max(2e-4,lr)
        for param_group in self.optimizer.param_groups:
            param_group['lr'] = lr

    def solve(self):
        step, epoch_num, sample_num = 0, 0, 0
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
                    loss = self.model.loss_func(*td)
                    loss.backward()
                    # torch.nn.utils.clip_grad_norm_(self.model.parameters(),1,'inf')
#                     for p in self.model.parameters():
#                         print(p.grad.max())
                    self.optimizer.step()
                    print('train [epoch|sample|step]:[%d|%d|%d] loss:%g' % (epoch_num, sample_num, step, float(loss)))
                if sample_num % 200000 == 0:
                    save_torch_model(self.model, self.out_dir, epoch_num)
                self.adjust_learning_rate(sample_num)

            print("\t")
            save_torch_model(self.model, self.out_dir, epoch_num)
            self.train_iter.reset()
           

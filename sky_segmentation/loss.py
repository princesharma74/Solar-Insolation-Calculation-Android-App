# -*- coding: utf-8 -*-
"""
Created on Tue Mar 24 21:32:12 2020

@author: vivek
"""
import torch
import utils

def BCE_loss(input, target):
    crit = torch.nn.BCEWithLogitsLoss()
    return crit(input,target)

def BCE_loss_batch(inputs, targets):
    batch_size = inputs.size(0)
    BCEs = []
    for i in range(0, batch_size):
        input, target = inputs[i][None,...], targets[i][None,...]
        BCE = BCE_loss(input, target)
        BCEs.append(BCE)
        mean_BCE = sum(BCEs)/len(BCEs)
    return mean_BCE, BCEs

def DICEScore(input, target, smooth):
    input = torch.sigmoid(input)
    iflat = input.view(-1)
    tflat = target.view(-1)
    intersection = (iflat * tflat).sum()
    return ((2.0 * intersection + smooth) / (iflat.sum() + tflat.sum() + smooth))

def DICEScore_batch(inputs, targets,smooth):
    batch_size = inputs.size(0)
    dices = []
    for i in range(0, batch_size):
        input, target = inputs[i], targets[i]
        DICE = DICEScore(input, target, smooth)
        dices.append(DICE)
        mean_DICEScore = sum(dices)/len(dices)
    return mean_DICEScore, dices

def DICELoss(input, target, smooth):
    score = DICEScore(input, target, smooth)
    loss = 1 - score
    return loss

def DICELoss_batch(inputs, targets, smooth):
    mean_DICESCore , scores = DICEScore_batch(inputs, targets, smooth)
    mean_DICELoss = 1 - mean_DICESCore
    losses = [1 - score for score in scores]
    return mean_DICELoss, losses

def IoUScore(input, target,smooth):
    input = torch.sigmoid(input)
    intersection = (input * target).sum()
    total = (input + target).sum()
    union = total - intersection
    IoU = (intersection + smooth) / (union + smooth)
    return IoU

def IoUScore_batch(inputs, targets,smooth):
    batch_size = inputs.size(0)
    ious = []
    for i in range(0, batch_size):
        input, target = inputs[i].view(-1), targets[i].view(-1)
        IoU = IoUScore(input, target, smooth)
        ious.append(IoU)
        mean_IoUScore = sum(ious)/len(ious)
    return mean_IoUScore, ious

def IoULoss(input, target, smooth):
    score = IoUScore(input, target, smooth)
    loss = 1 - score
    return loss

def IoULoss_batch(inputs, targets, smooth):
    mean_IoUScore, scores = IoUScore_batch(inputs, targets, smooth)
    mean_IoULoss = 1 - mean_IoUScore
    losses = [1 - score for score in scores]
    return mean_IoULoss, losses
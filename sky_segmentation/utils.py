# -*- coding: utf-8 -*-
"""
Created on Tue Mar 24 21:30:15 2020

@author: vivek
"""
import numpy as np
import matplotlib.image as mpimg
import torch
import matplotlib.pyplot as plt
import cv2

def expand_target(image, opp = False):
    """
        Expand the shape of Target : -1 , C , H , W --> -1 , C* , H , W

    :param image: Batch {Tensor}
    :param opp: If True , Do the reverse of expand
    :return: Expanded Shape {Tensor}
    """

    if opp:
        return image[:,0:1,:,:]
    else:
        image_0 = image
        image_1 = torch.where(image == 1 , torch.zeros_like(image) , torch.ones_like(image))
        image = torch.cat([image_0, image_1] , dim = 1)
    return image

def correct_shape(sample):
    """" Image from HWC to CHW.
        and Mask from HW to CHW """

    image = np.transpose(sample["image"] , (2,0,1))
    mask = sample["mask"][None,...]
    return {"image":image,
            "mask":mask}

def correct_binary(image , opp = False):
    """
    Replace 255 with 1 and reverse when opp is True
    """

    if opp:
        image = np.where(image == 1, 255, 0)
    else:
        image = np.where(image == 255 , 1 ,0)
    return image

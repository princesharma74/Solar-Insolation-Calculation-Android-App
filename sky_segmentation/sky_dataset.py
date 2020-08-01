# -*- coding: utf-8 -*-
"""
Created on Tue Mar 24 21:25:46 2020

@author: vivek
"""
import cv2
import os
import torch
import pandas as pd
import numpy as np
from pathlib import Path
from torch.utils.data import Dataset


def collate(batch):
    batch_size = len(batch)

    imgs = []
    msks = []
    for b in batch:
        if b is None:
            continue
        imgs.append(b["image"])
        msks.append(b["mask"])

    imgs = torch.cat(imgs)
    msks = torch.cat(msks)

    return {"images": imgs,
            "masks": msks}

class dataset(Dataset):
    def __init__(self, pathlist , images_dir,transform = None):
        self.pathlist = self.clean_pathlist(pathlist)
        self.transform = transform
        self.images_dir = images_dir

    def __len__(self):
        return len(self.pathlist)

    def clean_pathlist(self, pathlist):
        pathlist_copy = pathlist.copy()
        for path in pathlist_copy:
            if not os.path.exists(path[0]):
                pathlist.remove(path)
        return pathlist

    def __getitem__(self,idx):
        img_path, msk_path = self.pathlist[idx][0] , self.pathlist[idx][1]

        if not os.path.exists(img_path):
            return None
        
        img = cv2.imread(str(img_path))


        if img is None:
            return None

        msk = cv2.imread(str(msk_path),0)
        
        if self.transform is not None:
            transformed = self.transform({"image": img,
                                     "mask": msk})
        else:
            transformed = {"image":img,
                               "mask":msk}
        return transformed

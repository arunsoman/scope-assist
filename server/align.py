import astroalign as aa
from PIL import Image
from matplotlib import image
import numpy as np
import matplotlib.pyplot as plt

from scipy.ndimage import rotate


THRESHOLD_VALUE = 100

a = Image.open('i1.png')
aBw = a.convert('L')
imgData = np.asarray(aBw)
aBin = (imgData > THRESHOLD_VALUE) * 1.0

#plt.imshow(thresholdedData)
#plt.show()

a = Image.open('i2.png')
aBw = a.convert('L')
imgData = np.asarray(aBw)
bBin = (imgData > THRESHOLD_VALUE) * 1.0

p, (pos_img, pos_img_rot) = aa.find_transform(aBin, bBin)
print("\nTranslation: (x, y) = ({:.2f}, {:.2f})".format(*p.translation))
print("Rotation: {:.2f} degrees".format(p.rotation * 180.0 / np.pi))
print("\nScale factor: {:.2f}".format(p.scale))

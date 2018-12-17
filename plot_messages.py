#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import sys

message_file = open(sys.argv[1], "r")
times = message_file.read().splitlines()

print(len(times))

plt.plot(times)
plt.show()

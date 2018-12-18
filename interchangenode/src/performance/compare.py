#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import sys
import glob

# A script that plots the files passed as arguments in the same plot. 
# Run with ./compare [file, label]

i = 2
for arg in sys.argv[1::2]:
	file = open(arg, "r")
	latencies = file.read().splitlines()
	latencies = map(int, latencies)
	latencies.sort()
	plt.plot(latencies, label=sys.argv[i])
	i += 2

plt.xlabel('Number of messages', fontsize=14)
plt.ylabel('Latency in milliseconds', fontsize=14)

plt.legend(loc='upper left')
plt.show()

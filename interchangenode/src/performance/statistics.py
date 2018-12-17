#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import sys

latency_file = open(sys.argv[1], "r")
latencies = latency_file.read().splitlines()

latencies = map(int, latencies)

print("min: " + str(min(latencies)) + " ms")
print("max: "+ str(max(latencies)) + " ms")

avg = sum(latencies) / float(len(latencies))
print("avg: " + str(avg) + " ms")

median = np.median(np.array(latencies))
print("median: " + str(median)+ " ms")

print(len(latencies))

plt.plot(latencies)
plt.xlabel('Number of messages', fontsize=14)
plt.ylabel('Latency in milliseconds', fontsize=14)
plt.show()

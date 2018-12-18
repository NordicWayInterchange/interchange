#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import sys


# A script that outputs basic statistics about the file passed as argument. 
# Run with ./statistics.py [filename] 



latency_file = open(sys.argv[1], "r")
latencies = latency_file.read().splitlines()

latencies = map(int, latencies)

print("min: " + str(min(latencies)) + " ms")
print("max: "+ str(max(latencies)) + " ms")

avg = sum(latencies) / float(len(latencies))
print("avg: " + str(avg) + " ms")

median = np.median(np.array(latencies))
print("median: " + str(median)+ " ms")

print("message loss: " + str(2000-len(latencies)) )

print(len(latencies))

plt.plot(latencies)
plt.xlabel('Number of messages', fontsize=18)
plt.ylabel('Latency in milliseconds', fontsize=18)
plt.show()

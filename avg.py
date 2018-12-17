#! /usr/bin/env python

import numpy as np
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

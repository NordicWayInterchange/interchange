#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import sys


# A script that outputs basic statistics about the file passed as argument. 
# Run with ./statistics.py [filename]

latency_file = open(sys.argv[1], "r")
latencies = latency_file.read().splitlines()

latency_tuples = []
for message in latencies:
    latency_tuples.append(tuple(filter(None, message.split())))

int_tuples=[]
for elem in latency_tuples:
    int_tuples.append(tuple(map(int, elem)))

sorted_tuples = sorted(int_tuples, key=lambda tup: tup[1])


sorted_latencies = [i[0] for i in sorted_tuples]


print("min: " + str(min(sorted_latencies)) + " ms")
print("max: " + str(max(sorted_latencies)) + " ms")

avg = sum(sorted_latencies) / float(len(sorted_latencies))
print("avg: " + str(avg) + " ms")

median = np.median(np.array(sorted_latencies))
print("median: " + str(median)+ " ms")

print("message loss: " + str(2000 - len(sorted_latencies)))

print(len(sorted_latencies))

plt.plot(sorted_latencies)
plt.xlabel('Number of messages', fontsize=18)
plt.ylabel('Latency in milliseconds', fontsize=18)
plt.show()
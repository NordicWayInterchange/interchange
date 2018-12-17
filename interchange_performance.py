#! /usr/bin/env python

import re
import numpy as np
from datetime import datetime
import sys

#logfile = open("test_interchange_log.txt", "r")
logfile = open(sys.argv[1], "r")
loglines = logfile.read().splitlines()

split_lines = []
for line in loglines:
	split_lines.append(line.split(" "))

timestamps = []
for elem in split_lines:
	if len(elem) > 6:
		timestamps.append((elem[1], elem[6]))


new_list = filter(lambda item: item[1].startswith("ID"), timestamps)
item_map = {}

for elem in new_list:
	if elem[1] in item_map:
		item_map[elem[1]].append(elem[0])
	else:
		item_map[elem[1]] = [elem[0]]

times= []
for key in item_map:
	sorted_times = sorted(item_map[key])
	time1 = datetime.strptime(sorted_times[-1], "%H:%M:%S.%f")
	time2 = datetime.strptime(sorted_times[0], "%H:%M:%S.%f")
	#print((time1-time2).microseconds)
	time_microseconds = (time1-time2).microseconds
	times.append(time_microseconds/1000)

print("min: " + str(min(times)) + " ms")
print("max: "+ str(max(times)) + " ms")

avg = sum(times) / float(len(times))
print("avg: " + str(avg) + " ms")

median = np.median(np.array(times))
print("median: " + str(median)+ " ms")



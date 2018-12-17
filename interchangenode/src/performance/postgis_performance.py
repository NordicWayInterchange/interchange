#! /usr/bin/env python

import re
import numpy as np
from datetime import datetime
import sys

logfile = open(sys.argv[1], "r")
loglines = logfile.read().splitlines()

split_lines = []
for line in loglines:
	split_lines.append(line.split(" "))


def is_float(number):
	try: 
		float_number = float(number)
		return True
	except ValueError:
		return False


lookups = []
for line in split_lines: 
	if len(line) > 7 and is_float(line[7]):
		lookups.append(line[7])


lookups = map(float, lookups)

print("Number of loglines: " + str(len(lookups)))
print("min: " + str(min(lookups)) + " ms")
print("max: "+ str(max(lookups)) + " ms")

avg = sum(lookups) / float(len(lookups))
print("avg: " + str(avg) + " ms")

median = np.median(np.array(lookups))
print("median: " + str(median)+ " ms")



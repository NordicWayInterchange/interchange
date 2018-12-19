#! /usr/bin/env python2.7

# A script that takes a number of data files as input, and outputs a data file where each line is the
# average of the same line in the other files


# For each file
# For each line in the file, ordered by message number
# Add value on line to a list of values, at the line number index
# divide each element in the list of values on the number of files


import sys
from operator import add
import argparse

parser = argparse.ArgumentParser()

# Command line arguments
parser.add_argument('--output_destination', help='The output destination for the file of average latencies')
parser.add_argument('--file', help='The datafile that will be plotted', action='append')

args = parser.parse_args()

values = [0]*2000

for filename in args.file:
    data_file = open(filename, "r")
    latencies = data_file.read().splitlines()

    latency_tuples = []
    for line in latencies:
        latency_tuples.append(tuple(filter(None, line.split())))

    int_tuples = []
    for elem in latency_tuples:
        int_tuples.append(tuple(map(int, elem)))

    sorted_tuples = sorted(int_tuples, key=lambda tup: tup[1])
    sorted_latencies = [i[0] for i in sorted_tuples]

    x = 0

    values = map(add, values, sorted_latencies)


avg_values = []

for value in values:
    avg_values.append(round(value / float(len(args.file)),1))

with open(args.output_destination, 'w') as f:
    for value in avg_values:
        f.write("%s\n" % value)
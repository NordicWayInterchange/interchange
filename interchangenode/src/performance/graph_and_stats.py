#! /usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import argparse

# A script that plots the files passed as arguments in the same plot. 
# Run with ./compare --file [filename] --label [corresponding plot label] --num_messages [int] [--tuple]

parser = argparse.ArgumentParser()

# Command line arguments
parser.add_argument('--sorted', help='The data file has entries in the (latency, message_nr) format', action='store_true')
parser.add_argument('--file', help='The datafile that will be plotted', action='append')
parser.add_argument('--label', help='The plot label of the corresponding data file', action='append')
parser.add_argument('--expected_num_messages', help='The number of expected messages in the datafile', type=int)
parser.add_argument('--plot_label', help='The title of the plot')

args = parser.parse_args()

if not args.file and not args.label and not args.expected_num_messages:
	parser.error("Missing arguments. Run with --help to see full list of arguments and usage.")



x=0
for file in args.file:
	sorted_latencies = []

	data_file = open(file, "r")
	latencies = data_file.read().splitlines()

	if args.sorted:
		latency_tuples = []
		for line in latencies:
			latency_tuples.append(tuple(filter(None, line.split())))

		int_tuples=[]
		for elem in latency_tuples:
			int_tuples.append(tuple(map(float, elem)))

		sorted_tuples = sorted(int_tuples, key=lambda tup: tup[1])
		sorted_latencies = [i[0] for i in sorted_tuples]
		plt.plot(sorted_latencies, label=args.label[x])
		plt.title('Measured latency, ordered by message number')
	else:
		latencies = map(float, latencies)
		#latencies.sort()
		sorted_latencies = list(latencies)
		plt.plot(latencies, label=args.label[x])
		if not args.plot_label:
			plt.title('Latency on received messages')
		else:
			plt.title(args.plot_label)




	print(args.file[x])
	print("min: " + str(min(sorted_latencies)) + " ms")
	print("max: " + str(max(sorted_latencies)) + " ms")

	avg = sum(sorted_latencies) / float(len(sorted_latencies))
	print("avg: " + str(avg) + " ms")

	median = np.median(np.array(sorted_latencies))
	print("median: " + str(median)+ " ms")

	print("message loss: " + str(args.expected_num_messages - len(sorted_latencies)))

	x += 1


plt.xlabel('Number of messages', fontsize=14)
plt.ylabel('Latency in milliseconds', fontsize=14)

plt.legend(loc='upper left')
plt.show()

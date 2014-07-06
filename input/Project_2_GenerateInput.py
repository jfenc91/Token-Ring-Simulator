#!/usr/bin/python

#
# CSE/IT 353: Data and Computer Communications
# Project 1: Token Ring Emulation
# GenerateInput.py: Input file generator
#
# Written by: Juston Moore
# September 28, 2009
#

import sys
import random

# Check for command-line argument
if len(sys.argv) != 2:
	nodes = 5
#	print "Usage: GenerateInput.py [Number of Nodes]"
#	sys.exit()

if len(sys.argv) == 2:
	nodes = int(sys.argv[1])

# Make sure that the specified number of nodes is correct
if nodes < 2 or nodes > 254:
	print "You must have at least 2 nodes and no more than 254 nodes in your token ring."
	sys.exit()

# Create entries for each node
for node in range(1, nodes+1):
	file = open("input-file-" + str(node), 'w')
	
	# Generate a random number of entries for each node, from 1 to 1000
	entries = random.randint(1, 1000)
	for entry in range(0, entries):
		# Randomly generate the destination node NOT the same as the source
		dest = node
		while dest == node:
			dest = random.randint(1, nodes)

		# Randomly generate a PDU size up to 254
		size = random.randint(1, 254)

		# Write file entry
		file.write(str(dest) + ',' + str(size) + ',')
		for data in range(0, size):
			# Write random PDU data (Uppercase ASCII)
			char = chr(random.randint(65,90))
			file.write(char)
		file.write("\n")

#!/usr/bin/python
#
# CSE/IT 353
# Project 1: Token Ring Emulation
# Grade.py: Output file testing/grading script
#
# Written by: Juston Moore
# October 9, 2009
#
# This is the script that will be run against your program's output
# files. Put all of your input-file-i's and output-file-i's in the
# same directory and run this script in that directory. It will
# generate some sorted-output-i files that show what the output
# files should contain.
#
# NOTE: DO NOT make your program output files that look like the
# sorted-output files. Your output files should look much more
# random due to the random order of the input files and the token
# holding time you choose. All of the lines in the
# sorted-output files should appear somwehre in your output
# files.
#
# This program processes all input and output files and prints
# all errors it finds.
#
# This script has been tested, but there may be errors. If you
# find any, please email me at yangwang@nmt.edu and I will
# correct them as soon as possible. Having an accurate script
# will be a good thing all around for grading!
#

import sys;
import os;
import re;

#
# Get the numbers of all input node files
#

numbers = [];
# List files in current directory
fileList = os.listdir(".")
for file in fileList:
	# Check that the file is an input file
	match = re.match("input-file-([0-9]{1,3})", file)
	if match:
		# If so, get the node number
		number = int(match.group(1))
		numbers.append(number)
numbers.sort()

# Check that the input files collected have valid numbers
for i in range(0, len(numbers)):
	if numbers[i] != i+1:
		print "Bad sequence of input files: " + str(numbers)
		exit(1)

# Infer the number of nodes and make sure the answer is reasonable
number = len(numbers)
del numbers
if number < 2 or number > 254:
	print "There should be between 2 and 254 input files. " + str(number) + " files were found."

#
# Read input files and generate correct answer
#

# answer is a list of lists, one sub list for each destination node
answer = []
for i in range(0,number):
	answer.append([])

# Read the input from each file into the appropriate list in answer
for fileNum in range(1, number+1):
	file = open("input-file-" + str(fileNum), "r")

	# Read each line
	while 1:
		line = file.readline()
		if not line:
			break

		# Get the fields from the line
		fields = line.split(",")
		if len(fields) == 3:
			# Organize the entry
			entry = {"Source": fileNum, "Dest": int(fields[0]), "Size": int(fields[1]), "Data": fields[2]}
			# Put this into the slot for the destination node
			answer[int(fields[0])-1].append(entry)
	
	file.close()

#
# Comparison function to get lists into a consistent order
#

def compare(x, y):
	if x["Source"] < y["Source"]:
		return -1
	elif x["Source"] > y["Source"]:
		return 1
	else:
		if x["Data"] < y["Data"]:
			return -1
		elif x["Data"] > y["Data"]:
			return 1
		else:
			return 0

#
# Print correct, sorted answers to sorted-output-i
#

# Sort each list and put the output to 'sorted-output-i'
for i in range(0, number):
	# Sort the entries
	answer[i].sort(compare)
	
	file = open("sorted-output-" + str(i+1), "w")

	# Look at each entry for the destination
	destAns = answer[i]
	for entry in destAns:
		# Write the correctly-formatted output line
		file.write(str(entry["Source"]) + "," + str(entry["Dest"]) + "," + str(entry["Size"]) + "," + entry["Data"])

#
# Read the student's output files and compare the answers
#

errors = 0

for fileNum in range(1, number + 1):
	# Open the output file
	try:
		file = open ("output-file-" + str(fileNum), "r")
	except:
		print "Cannot find file: output-file-" + str(fileNum)

	# Get the student answer for this destination node
	student = []
	# Read each line from the ouptput file & parse
	while 1:
		line = file.readline();
		if not line:
			break

		# Organize line into internal entry representation
		fields = line.split(",")
		if len(fields) == 4:
			entry = {"Source": int(fields[0]), "Dest": int(fields[1]), "Size": int(fields[2]), "Data": fields[3]}
			student.append(entry)
	
	file.close();

	# Put the student solution into a sorted order so that it can be compared
	# with the previously-generated answer
	student.sort(compare)

	# Ensure that the output file has the correct number of entries
	if len(student) != len(answer[fileNum-1]):
		print "ERROR in output-file-" + str(fileNum)
		print "Incorrect number of outputs. " + str(len(student)) + " were seen and " + str(len(answer[fileNum-1])) + " were expected."
		errors = errors + 1
	else:
		# Make sure that student answers have the correct contents
		for i in range(0, len(student)):
			if student[i] != answer[fileNum-1][i]:
				print "ERROR in output-file-" + str(fileNum)
				print "Student: " + str(student[i])
				print "Answer: " + str(answer[fileNum-1][i])
				errors = errors + 1

# Print result
if errors == 0:
	print "Good job! No errors found."
else:
	print str(errors) + " errors were found"

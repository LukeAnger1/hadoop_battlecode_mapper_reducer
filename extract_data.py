import sys
import re

# Define a regular expression pattern to match lines containing "dev"
pattern = re.compile(r'.*<?var.*')

# Read input from stdin or a file provided as an argument
if len(sys.argv) > 1:
    file_path = sys.argv[1]
    with open(file_path, 'r') as file:
        for line in file:
            if pattern.match(line):
                print(line, end='')
else:
    # Read from stdin if no file is provided as an argument
    for line in sys.stdin:
        if pattern.match(line):
            print(line, end='')

#!/usr/bin/env python3

# This code expects the sys.stdin to look like the below

# BotName
# Map1, Map2, Map3, Map4, ...
# var1 -> range()
# var2 -> range()
# var3 -> range()

import sys
import re

for line in sys.stdin:
    line = re.sub(r'\W+', ' ', line.strip())
    words = line.split()

    for word in words:
        print('{}\t{}'.format(word, 1))

#!/usr/bin/env python3

# This code expects the sys.stdin to look like the below

# BotName | Map1, Map2, Map3, Map4, ... | var1 -> range() | var2 -> range() | var3 -> range()
# Then we have multiple lines of this for multiple bots

# Will output
# BotName1, var1 -> value1, var2 -> value2, ... | BotName2, var1 -> value1, var2 -> value2, ... | Map1, Map2, Map3, Map4, Map5, ...

import sys
import re
import itertools
import random

# This will get a list of every possible combination
def get_all_combinations(replace_range):
    return list(itertools.product(*replace_range))

# This will pick random ones within the range
def get_rand_combinations(replace_range, number_combination):
    return random.sample(get_all_combinations(replace_range), number_combination)

if __name__ == '__main__':

    for line in sys.stdin:
        line = re.sub(r'\W+', ' ', line.strip())
        words = line.split()

        for word in words:
            print('{}\t{}'.format(word, 1))
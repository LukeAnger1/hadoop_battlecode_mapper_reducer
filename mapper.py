#!/usr/bin/env python3

# This code expects the sys.stdin to look like the below

# BotName
# Map1, Map2, Map3, Map4, ...
# var1 -> range()
# var2 -> range()
# var3 -> range()

# Or in one line
# BotName\nMap1, Map2, Map3, Map4, ...\nvar1 -> range()\nvar2 -> range()\nvar3 -> range()

import sys
import re
import itertools
import random

# Function to replace words
def replace_words(text, original, replace):
    # This will take text and two lists, and replace the original with the replacement
    for old_word, new_word in zip(original, replace):
        text = text.replace(old_word, str(new_word))
    return text

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
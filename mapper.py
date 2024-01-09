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

# TODO: The current is just for testing and should be switched later
bot_source_file_path = "/mnt/c/Users/anger/OneDrive/Desktop/bc/test_file.txt"
path_to_bot_src_folder = "/mnt/c/Users/anger/OneDrive/Desktop/bc" # This is where it puts the modified content
start_char, end_char = '<?', '?>' # These are the characters the code looks for for replacing words

# Function to replace words
def replace_words_func(text, original, replace):\
    # This is to make sure original and replace is some sort of list
    assert not isinstance(original, str) and not isinstance(replace, str)
    # This will take text and two lists, and replace the original with the replacement
    for old_word, new_word in zip(original, replace):
        text = text.replace(old_word, str(new_word))
    return text

# This function will get src file and make the one replaced, THIS ADDS THE CHARACTERS
def make_bot(input_file_path, output_file_path, original_words, replace_words):
    # this adds the characters
    original_words = [start_char + original_word + end_char for original_word in original_words]
    with open(input_file_path, 'r') as file:
        file_content = file.read()
    modified_content = replace_words_func(file_content, original_words, replace_words)
    with open(output_file_path, 'w') as file:
            file.write(modified_content)

# This will get a list of every possible combination
def get_all_combinations(replace_range):
    return list(itertools.product(*replace_range))

# This will pick random ones within the range
def get_rand_combinations(replace_range, number_combination):
    return random.sample(get_all_combinations(replace_range), number_combination)

if __name__ == '__main__':
    # testing remove later
    make_bot(bot_source_file_path, path_to_bot_src_folder + '/output.txt', ['var1', 'var2'], ['LLLLLL', 'Take the L'])
    # testing remove later
    """for line in sys.stdin:
        line = re.sub(r'\W+', ' ', line.strip())
        words = line.split()

        for word in words:
            print('{}\t{}'.format(word, 1))"""
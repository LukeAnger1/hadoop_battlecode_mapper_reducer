#!/usr/bin/env python3
"""reducer.py"""

from operator import itemgetter
import sys

current_word = None
current_count = 0
word = None

# EX
# input
"""
type the below into a terminal
echo "bot1 | map1.      map2.map3. mapy4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4] . var3->range(1, 2, 3)
bot2 | map1.      map2.mapy3. map4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4]" | python3 mapper.py | python3 reducer.py
"""

# This is the input ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    
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

if __name__ == '__main__':
    # testing for bot file writing start
    # make_bot(bot_source_file_path, path_to_bot_src_folder + '/output.txt', ['var1', 'var2'], ['LLLLLL', 'Take the L'])
    # testing for bot file writing end

    # input comes from STDIN
    for index, line in enumerate(sys.stdin):
        print(f'line {eval(line)} at index {index}')

        """
        # remove leading and trailing whitespace
        line = line.strip()

        # parse the input we got from mapper.py
        word, count = line.split('\t', 1)

        # convert count (currently a string) to int
        try:
            count = int(count)
        except ValueError:
            # count was not a number, so silently
            # ignore/discard this line
            continue

        # this IF-switch only works because Hadoop sorts map output
        # by key (here: word) before it is passed to the reducer
        if current_word == word:
            current_count += count
        else:
            if current_word:
                # write result to STDOUT
                print(f'{current_word}\t{current_count}')
            current_count = count
            current_word = word

    # do not forget to output the last word if needed!
    if current_word == word:
        print(f'{current_word}\t{current_count}')
        """

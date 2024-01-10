#!/usr/bin/env python3

# This code expects the sys.stdin to look like the below

# BotName | Map1, Map2, Map3, Map4, ... | var1 -> range() , var2 -> range() , var3 -> range()
# Then we have multiple lines of this for multiple bots

# Will output
# BotName1, var1 -> value1, var2 -> value2, ... | BotName2, var1 -> value1, var2 -> value2, ... | Map1, Map2, Map3, Map4, Map5, ...

# EX:
# input
"""
bot1 | map1.      map2.map3. mapy4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4] . var3->range(1, 2, 3)
bot2 | map1.      map2.mapy3. map4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4]

type the below into a terminal
echo "bot1 | map1.      map2.map3. mapy4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4] . var3->range(1, 2, 3)
bot2 | map1.      map2.mapy3. map4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4]" | python3 mapper.py
"""

# output
# *BIG*

import sys
import itertools
import random
from itertools import combinations
import subprocess
import os
import shutil

bot_seperator = '\n'
bot_type_sperator = '|'
bot_list_seperator = '.'
bot_var_value_seperator = '->'
worthless_info = ' '

# This will get a list of every possible combination
def get_all_combinations_one_bot(replace_range):
    return list(itertools.product(*replace_range))

# This will pick random ones within the range
def get_rand_combinations_one_bot(replace_range, number_combination):
    return random.sample(get_all_combinations_one_bot(replace_range), number_combination)
    
# TODO: The current is just for testing and should be switched later
folder_with_gradlew = "/mnt/c/Users/anger/OneDrive/Desktop/bc/bcg"
bot_source_file_folder_with_dummy_variables = "/mnt/c/Users/anger/OneDrive/Desktop/bc/Lv1" # this is the folder to look for the bots (any bot name should have a file in this folder) that need to variables to be replaced
bot_source_file_folder = "/mnt/c/Users/anger/OneDrive/Desktop/bc" # This is where it puts the modified content, this is what the game will run

# Function to replace words
def replace_words_func(text, original, replace):\
    # This is to make sure original and replace is some sort of list
    assert not isinstance(original, str) and not isinstance(replace, str)
    # This will take text and two lists, and replace the original with the replacement
    for old_word, new_word in zip(original, replace):
        text = text.replace(old_word, str(new_word))
    return text

def make_bot(input_folder_path, output_folder_path, original_words, replace_words):
    # Check if the output folder exists, if not, create it
    if not os.path.exists(output_folder_path):
        os.makedirs(output_folder_path)

    # Iterate over all files in the input folder
    for filename in os.listdir(input_folder_path):
        input_file_path = os.path.join(input_folder_path, filename)

        # Check if it's a file and not a directory
        if os.path.isfile(input_file_path):
            with open(input_file_path, 'r') as file:
                file_content = file.read()

            modified_content = replace_words_func(file_content, original_words, replace_words)

            output_file_path = os.path.join(output_folder_path, filename)
            with open(output_file_path, 'w') as file:
                file.write(modified_content)

def unmake_bot(folder_path):
    # Check if the folder exists
    if not os.path.exists(folder_path):
        print(f"Folder not found: {folder_path}")
        return

    # Iterate over all items in the folder
    for item in os.listdir(folder_path):
        item_path = os.path.join(folder_path, item)

        # Check if it's a file or directory
        try:
            if os.path.isfile(item_path):
                os.remove(item_path)
            elif os.path.isdir(item_path):
                # Recursively delete directory contents
                shutil.rmtree(item_path)
            print(f"Item {item_path} successfully deleted.")
        except OSError as e:
            print(f"Error: {item_path} : {e.strerror}")

    # Finally, delete the folder itself
    try:
        os.rmdir(folder_path)
        print(f"Folder {folder_path} successfully deleted.")
    except OSError as e:
        print(f"Error: Unable to delete folder {folder_path} : {e.strerror}")


def run_command_in_terminal(command, directory=folder_with_gradlew):
    try:
        # Run the command
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, cwd=directory)

        # Return the standard output and error
        return result.stdout, result.stderr
    except subprocess.CalledProcessError as e:
        # Return the error message if the command fails
        return e.stdout, e.stderr
    
# TODO: this is the last bit of code it is going to need to return 
def run_games(match_info):
    # This is what input should look like, not a string ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    bot1_name = match_info[0][0]
    bot2_name = match_info[1][0]
    maps = match_info[2]
    results = ""
    for map in maps:
        # print(f'/gradlew run -Pmaps={map} -PteamA={bot1_name} -PteamB={bot2_name}')
        results+=repr(run_command_in_terminal(f'{folder_with_gradlew}/gradlew run -Pmaps={map} -PteamA={bot1_name} -PteamB={bot2_name}'))
    return results

# The below is an example command to run games
# print(run_games((("Lv1", "dumby", "dumby"), ("Lv1", "dumby", "dumby"), ["DefaultSmall", "DefaultMedium", "DefaultLarge", "DefaultHuge"])))

if __name__ == '__main__':
    bots = [] # this will contain a list of the bots, not necassarily the same file name bot
    for line in sys.stdin:
        bot_name, bot_maps, bot_var_value = line.split(bot_type_sperator)

        # this is code to get rid of any unnecasry spacing and to convert it to a list
        def quick_strip(stringy):
            return [word.strip().replace(worthless_info, '') for word in stringy.split(bot_list_seperator)]
        bot_name = bot_name.strip().replace(worthless_info, '')
        bot_maps = set(quick_strip(bot_maps))
        bot_var_value = quick_strip(bot_var_value)

        # This function takes a string var1 -> [value1, value2, value3] and converts it to a usable tuple
        def var_values_extractor(input, bot_var_value_seperator=bot_var_value_seperator):
            # Split the string at '->' or bot_var_value_seperator
            var_name, var_value_str = input.split(bot_var_value_seperator)

            # Strip whitespace from the variable name
            var_name = var_name.strip().replace(worthless_info, '')

            # Use eval to convert the right-hand side string into a Python object
            # It's important to use eval carefully as it can execute arbitrary code
            # Here, we restrict its use to safe types (list, tuple, range)
            if var_value_str.strip().startswith(('(', '[', 'range')):
                var_value = eval(var_value_str.strip())
            else:
                raise ValueError("Unsupported value format")

            return var_name, var_value

        bot_var_value = [var_values_extractor(var_value) for var_value in bot_var_value]
        vars = [var_value[0] for var_value in bot_var_value]
        values = [var_value[1] for var_value in bot_var_value]

        # this is test code
        # print(f'bot name is {bot_name} and bot_maps is {bot_maps} and vars is {vars} and values is {values}')
        # this is test code

        # We need to expand on the ranges and tuples, so we have a list like below
        # [(botName1, maps1, (var1, var2, var3, var4, var5,...), (value1, value2, value3, value4, value5,...)), (botName1, ...), ..., (botName2,...)]

        # This is all possible combinations, probaly going to have to use the random choosing one for big sets
        for combo in get_all_combinations_one_bot(values):
            bots.append((bot_name, bot_maps, vars, combo))

    # now we have a list of all the bots, now we need to make the matches, this will look like ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    for bot1, bot2, in combinations(bots, 2):
        # This is the match, repr is to convert to string
        # This is the input ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
        print(repr(((bot1[0], bot1[2], bot1[3]), (bot2[0], bot2[2], bot2[3]), bot1[1].union(bot2[1]))))

    """line = re.sub(r'\W+', ' ', line.strip())
    words = line.split()

    for word in words:
        print('{}\t{}'.format(word, 1))"""
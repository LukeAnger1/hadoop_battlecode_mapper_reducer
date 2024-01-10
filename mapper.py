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
bot_source_file_folder_with_dummy_variables = "/mnt/c/Users/anger/OneDrive/Desktop/bc" # this is the folder to look for the bots (any bot name should have a file in this folder) that need to variables to be replaced
# IMPORTANT make sure the below file is right it will delete all bots!!!!
bot_source_file_folder = "/mnt/c/Users/anger/OneDrive/Desktop/bc/bcg/src" # This is where it puts the modified content, this is what the game will run

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

def unmake_ALL_bots(folder_path):
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

def run_command_in_terminal(command, directory=folder_with_gradlew):
    try:
        # Run the command
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, stdin=subprocess.DEVNULL, cwd=directory)

        # Return the standard output and error
        return result.stdout, result.stderr
    except subprocess.CalledProcessError as e:
        # Return the error message if the command fails
        return e.stdout, e.stderr
    
# TODO: this is the last bit of code it is going to need to return 
def run_games(bot1_name, bot2_name, maps):
    # This is what input should look like, not a string ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    results = ""
    for map in maps:
        # print(f'/gradlew run -Pmaps={map} -PteamA={bot1_name} -PteamB={bot2_name}')
        results+=repr(run_command_in_terminal(f'./gradlew run -Pmaps={map} -PteamA={bot1_name} -PteamB={bot2_name}'))
    return results



# The below is an example command to run games
# print(run_games((("Lv1", "dumby", "dumby"), ("Lv1", "dumby", "dumby"), ["DefaultSmall", "DefaultMedium", "DefaultLarge", "DefaultHuge"])))

if __name__ == '__main__':
    # each line is match info ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    for line in sys.stdin:
        bot1_info_old, bot2_info_old, maps = eval(line)
        print(f' the info is {eval(line)}')
        bot1_name_old, bot1_vars_old, bot1_combo_old = bot1_info_old
        bot2_name_old, bot2_vars_old, bot2_combo_old = bot2_info_old

        # this is to rename bots to prevent conflict
        bot1_name = bot1_name_old + 'a'
        bot2_name = bot2_name_old + 'b'

        # this code is to change in in files
        bot1_vars = list(bot1_vars_old)
        bot1_vars.append(bot1_name_old)
        bot1_combo = list(bot1_combo_old)
        bot1_combo.append(bot1_name)

        bot2_vars = list(bot2_vars_old)
        bot2_vars.append(bot2_name_old)
        bot2_combo = list(bot2_combo_old)
        bot2_combo.append(bot2_name)

        bot1_input_folder = bot_source_file_folder_with_dummy_variables + '/' + bot1_name_old
        bot1_output_folder = bot_source_file_folder + '/' + bot1_name
        bot2_input_folder = bot_source_file_folder_with_dummy_variables + '/' + bot2_name_old
        bot2_output_folder = bot_source_file_folder + '/' + bot2_name

        bot1 = make_bot(bot1_input_folder, bot1_output_folder, bot1_vars, bot1_combo)
        bot2 = make_bot(bot2_input_folder, bot2_output_folder, bot2_vars, bot2_combo)
        
        # TODO: cut these results somewhere, either here or in the reduce
        results = run_games(bot1_name, bot2_name, maps)
        print(f'the results are {results}')


        # unmake_ALL_bots(bot_source_file_folder)

        """
        add new_bot_name
        make_bot
        make_bot
        run_game
        unmake_bot
        unmake_bot
        """

    
#!/usr/bin/env python3
"""reducer.py"""

from operator import itemgetter
import sys
import subprocess
import os
import shutil

# EX
# input
"""
type the below into a terminal
echo "bot1 | map1.      map2.map3. mapy4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4] . var3->range(1, 2, 3)
bot2 | map1.      map2.mapy3. map4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4]" | python3 mapper.py | python3 reducer.py
"""

# This is the input ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    
# TODO: The current is just for testing and should be switched later
folder_with_gradlew = "/mnt/c/Users/anger/OneDrive/Desktop/bc/bcg"
bot_source_file_folder_with_dummy_variables = "/mnt/c/Users/anger/OneDrive/Desktop/bc/test_file.txt" # this is the folder to look for the bots (any bot name should have a file in this folder) that need to variables to be replaced
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
    # testing for bot file writing start
    # make_bot(bot_source_file_path, path_to_bot_src_folder + '/output.txt', ['var1', 'var2'], ['LLLLLL', 'Take the L'])
    # testing for bot file writing end

    # input comes from STDIN
    for index, line in enumerate(sys.stdin):
        # This is the input ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
        match_info = eval(line)
        """make_bot()
        make_bot()
        run_games()
        unmake_bots()"""

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

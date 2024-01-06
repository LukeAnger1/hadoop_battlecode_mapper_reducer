# This code takes the base code and makes multiple files for testing in the tournament

import itertools
import os

# file path
# TODO: Add a condition where folders work by applying to every subfolder and file
file_path = 'source_file.txt'

# Define the words to be replaced and their corresponding replacement ranges
original = ['<?var1?>', '<?var2?>', '<?var3?>']
replace_range = [range(0, -5, -1), (0, 1, 2),  [100, 200, 300, 400, 500, 6, 7]]
    # Add more words and replacements as needed

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
# TODO: Actually write this function

def separate_extension(filename):
    root, ext = os.path.splitext(filename)
    return root, ext

if __name__ == '__main__':

    # Generate all possible combinations
    combinations = get_all_combinations(replace_range)

    # Open the source file and read its content
    with open(file_path, 'r') as file:
        file_content = file.read()

    for replace in combinations:
        # Replace the specified words
        modified_content = replace_words(file_content, original, replace)

        # Write the modified content to a new file
        name, extension = separate_extension(file_path)
        with open(name + str(replace) + extension, 'w') as file:
            file.write(modified_content)

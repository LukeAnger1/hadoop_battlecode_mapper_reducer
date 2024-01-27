import sys
import itertools
import random
import argparse
from itertools import combinations

bot_seperator = '\n'
bot_type_sperator = '|'
bot_list_seperator = '.'
bot_var_value_seperator = '->'
worthless_info = ' '

# Argument parser for command line options
parser = argparse.ArgumentParser(description="Generate bot combinations")
parser.add_argument("-n", type=int, default=-1, help="Number of random combinations to generate of each seperate bot, if less than zero it will use every combination (default: -1)")
parser.add_argument("-m", type=int, default=1, help="Twice as many matches as each bot should run (default: 1)")
args = parser.parse_args()

# This will get a list of every possible combination
def get_all_combinations_one_bot(replace_range):
    return list(itertools.product(*replace_range))

# This will pick random ones within the range
def get_rand_combinations_one_bot(replace_range, number_combinations):
    random_combinations = []
    for _ in range(number_combinations):
        combination = tuple(random.choice(r) for r in replace_range)
        random_combinations.append(combination)
    return random_combinations

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

        iteration_holder = get_rand_combinations_one_bot(values, args.n) if args.n > 0 else get_all_combinations_one_bot(values)
        for combo in iteration_holder:
            bots.append((bot_name, bot_maps, vars, combo))

        # Shuffle the bots list for random pairing
        random.shuffle(bots)
        
        # now we have a list of all the bots, now we need to make the matches, this will look like ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
        with open("matches", 'w') as file:
            bots_length = len(bots)
            for i in range(bots_length):
                bot1 = bots[i]
                for j in range(1, args.m + 1):
                    # Use modulo to wrap around the bots list
                    bot2 = bots[(i + j) % bots_length]

                    # This is the input ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
                    match = ((bot1[0], bot1[2], bot1[3]), (bot2[0], bot2[2], bot2[3]), bot1[1].union(bot2[1]))
                    file.write(repr(match) + "\n")  # Adding '\n' for a newline

        

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
import re
import itertools
import random

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

# This will get a list of bot info in the form of (bot1, bot1_maps, [var1, var2, var3, ...], [value1, value2, value3,....]) for each bot
def get_bot():
    pass

if __name__ == '__main__':
    bots = [] # this will contain a list of the bots, not necassarily the same file name bot
    for line in sys.stdin:
        for value in line.split(bot_type_sperator):
            print(value)
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
            print((bot_name, bot_maps, vars, combo))
            bots.append((bot_name, bot_maps, vars, combo))

    # now we have a list of all the bots, now we need to make the matches, this will look like ((bot_name1, vars1, combo1), (bot_name2, vars2, combo2), maps)
    print(bots)

    """line = re.sub(r'\W+', ' ', line.strip())
    words = line.split()

    for word in words:
        print('{}\t{}'.format(word, 1))"""
#!/usr/bin/env python3

# This code expects the sys.stdin to look like the below

# BotName | Map1, Map2, Map3, Map4, ... | var1 -> range() , var2 -> range() , var3 -> range()
# Then we have multiple lines of this for multiple bots

# Will output
# BotName1, var1 -> value1, var2 -> value2, ... | BotName2, var1 -> value1, var2 -> value2, ... | Map1, Map2, Map3, Map4, Map5, ...

# EX:
# input
"""
bot1 | map1,      map2,map3, mapy4 | var1 ->     (1, 2, 3)    ,     var2->[1, 2, 3, 4]
bot2 | map1,      map2,mapy3, map4 | var1 ->     (1, 2, 3)    ,     var2->[1, 2, 3, 4]

type the below into a terminal
echo "bot1 | map1,      map2,map3, mapy4 | var1 ->     (1, 2, 3)    ,     var2->[1, 2, 3, 4]
bot2 | map1,      map2,mapy3, map4 | var1 ->     (1, 2, 3)    ,     var2->[1, 2, 3, 4]" | python3 mapper.py
"""

# output
# *BIG*

import sys
import re
import itertools
import random

bot_seperator = '\n'
bot_info_sperator = '|'
vot_var_value_seperator = '->'

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

    for line in sys.stdin:
        for value in line.split(bot_info_sperator):
            print(value)
        bot_name, bot_maps, bot_var_value = line.split(bot_info_sperator)
        print(f'bot name is {bot_name} and bot_maps is {bot_maps} and bot_var_line is {bot_var_value}')
        """line = re.sub(r'\W+', ' ', line.strip())
        words = line.split()

        for word in words:
            print('{}\t{}'.format(word, 1))"""
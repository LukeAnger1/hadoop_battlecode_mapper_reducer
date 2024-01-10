#!/usr/bin/env python3
"""reducer.py"""

from operator import itemgetter
import sys

# EX
# input
"""
type the below into a terminal
echo "bot1 | map1.      map2.map3. mapy4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4] . var3->range(1, 2, 3)
bot2 | map1.      map2.mapy3. map4 | var1 ->     (1, 2, 3)    .     var2->[1, 2, 3, 4]" | python3 mapper.py | python3 reducer.py
"""

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

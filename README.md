This is regression for a hadoop cluster running battlecode

# THIS IS IMPORTANT INFORMATION REGARDING RUNNING GAMES
1. For setup make sure to run chmod +x on the python files so they can be ran
2. Please be in the battlecode_beasts folder when running the command found in example
3. Please test your input using cat in the example for the mapper
4. Every bot in your src will be deleted working on a fix but save ur bots before running
5. Do not make bots with numbers in their name
6. DO NOT be Luke. DO NOT be an idiot and make these mistakes!!!
7. For the input.txt (9) isnt registered as a tuple, but as an int!!!!

Here is how to use the software. Make a file like test_input.txt which has different bots, with maps, and strings you want switched with an -> pointing to a tuple, list, or range of values to test

The below will create a file called matches that can be ran
cat test_input.txt | python3 make_matches.py

To run the mapper.py run first go into the mapper and change the folder paths to fit your needs then
cat matches | python3 mapper.py

I am working on the reducer but it will function like
cat matches | python3 mapper.py | python3 reducer.py

The above instructions are for running this code on your own software, while the below is for the hadoop cluster we have built and file paths are important

# https://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/ use this website for input
# Test mapper.py and reducer.py locally first

# upload
hadoop fs -copyFromLocal  <source_location_in_local filesystem><destination_location_in_HDFS>
# check if uploaded
hadoop fs -ls /path

# This is a python streaming test that does not work yet, so far the example on https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html is the one that works
# TODO: prob dont need file flag for mapper, make sure to use relative path absolute causes problems
# This is path dependent so for different setups there needs to be different paths
/home/help/hadoop/bin/hadoop jar /home/help/hadoop/share/hadoop/tools/lib/hadoop-streaming-3.3.6.jar -file mapper.py    -mapper mapper.py -file reducer.py   -reducer reducer.py -input /input1/matches -output /out1

# This fixes issues with long games
/home/help/hadoop/bin/hadoop jar /home/help/hadoop/share/hadoop/tools/lib/hadoop-streaming-3.3.6.jar     -D mapreduce.task.timeout=1800000     -D mapreduce.task.timeout.enable=true     -D mapreduce.reduce.task.timeout=1800000     -D mapreduce.reduce.task.timeout.enable=true     -file mapper.py     -mapper mapper.py     -file reducer.py     -reducer reducer.py     -input /input2     -output /out21



# please test your code on subset before uploading to cluster

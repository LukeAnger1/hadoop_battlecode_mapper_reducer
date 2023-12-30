import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BotTournament {

    public static class BotPairMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text pair = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            /* 
             * strucutre of value
             * botName
             * start, stop, step                                                This is for the first attribute
             * start, stop, step                                                This is for the second attribute
             * ...
             * 
             * 
             * example of value
             * bot_a
             * 4, 8, 2
             * 0, 10, 1
             */
            
            // This gets all the data into a list of strings
            String[] lines = value.toString().split("\\n");
            
            // the first string is the bot name
            String botName = lines[0]; // First line is the bot name, the following lines are the (start, stop, step) for range of values

            // each of the following lines corresponds to an attribute, each line represents the range the attribute should take during testing, ex: [start, stop, step]
            Integer[][] listOfRanges = new Integer[lines.length-1][3];
            for (int index=1; index<lines.length; index++) {
                String[] range_string = lines[index].split(",");
                Integer[] range = new Integer[range_string.length];
                for (int a = 0; a < range.length; a++) {
                    range[a] = Integer.parseInt(range_string[a]);
                }
                listOfRanges[index] = range;
            }
            
            int totalCombinations = 1;
            for (Integer[] range : listOfRanges) {
                int start = range[0];
                int stop = range[1];
                int step = range[2];
                totalCombinations *= Math.max(0, Math.ceil((stop - start) / step));
            }

            // TODO: catch the case there is a problem and no combinations

            // TODO: This is wrong from below here

            // This is a list containing every possible list made from the values given above
            Integer[][] result = new Integer[totalCombinations][listOfRanges.length];
            for (int i = 0; i < totalCombinations; i++) {
                Integer[] combination = new Integer[listOfRanges.length];
                int j = 1;
                for (int index = 0; index < listOfRanges.length; index ++) {
                    Integer[] range = listOfRanges[index]
                    combination[index] = range[(i / j) % range.length]
                    j *= range.length
                }
                result[i] = combination;
            }

            return result;
        

            /*String[] attributes = value.toString().split("\\s+");
            for (int i = 0; i < attributes.length; i++) {
                for (int j = i + 1; j < attributes.length; j++) {
                    pair.set(attributes[i] + "," + attributes[j]);
                    context.write(pair, one);
                }
            }*/
        }
    }

    public static class GameResultReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            // TODO: the key needs to be a list [[a1, a2, a3, ...], [b1, b2, b3, ...]] where the first list specifies the robot and the next list is the attirbutes we need to use
            String[] bots = key.toString().split(",");
            // TODO: change this to a function that makes a new bot source file with the given attributes
            Bot botA = makeBot(Integer.parseInt(bots[0]));
            Bot botB = makeBot(Integer.parseInt(bots[1]));

            // TODO: Assume runGame returns 1 if botA wins, 2 if botB wins, change this to extract the winner
            int winner = runGame(botA, botB);
            result.set(winner);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "bot tournament");
        job.setJarByClass(BotTournament.class);
        job.setMapperClass(BotPairMapper.class);
        job.setCombinerClass(GameResultReducer.class);
        job.setReducerClass(GameResultReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    // Placeholder for makeBot method
    private static Bot makeBot(int m) {
        // Implement this method according to your Bot framework
        return new Bot(m);
    }

    // Placeholder for runGame method
    private static int runGame(Bot botA, Bot botB) {
        // Implement this method to simulate a game and return the winner
        return 0; // Replace with actual game logic
    }

    // Placeholder Bot class
    private static class Bot {
        int m;

        public Bot(int m) {
            this.m = m;
        }
    }
}

package edu.uci.cs230.team10;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        JobConf jobConf = new JobConf(conf, Main.class);
        FileInputFormat.addInputPath(jobConf, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobConf, new Path("./output"));
        Job job = Job.getInstance(jobConf, "wiki index");
        job.setJarByClass(Main.class);
        job.setMapperClass(Parser.class);
        job.setCombinerClass(Indexer.class);
        //job.setReducerClass(Indexer.class); // should never actually run
        job.setNumReduceTasks(0);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(NullWritable.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

}

package com.course.mapreduce.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class HourPeakJob {
    public static class PeakMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private static final IntWritable ONE = new IntWritable(1);
        private final Text outKey = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            AccessLogRecord record = AccessLogRecord.parse(value.toString());
            if (record == null) {
                return;
            }
            outKey.set(record.hourKey());
            context.write(outKey, ONE);
        }
    }

    public static Job createJob(Configuration configuration, String input, String output) throws IOException {
        Job job = Job.getInstance(configuration, "website hour peak");
        job.setJarByClass(HourPeakJob.class);
        job.setMapperClass(PeakMapper.class);
        job.setCombinerClass(SumReducer.class);
        job.setReducerClass(SumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        return job;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: HourPeakJob <input> <output>");
            System.exit(2);
        }
        System.exit(createJob(new Configuration(), args[0], args[1]).waitForCompletion(true) ? 0 : 1);
    }
}

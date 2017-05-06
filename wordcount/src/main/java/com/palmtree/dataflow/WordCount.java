package com.palmtree.dataflow;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

/**
 * Created by sam on 6/5/17.
 */
public class WordCount {
    public static class CountWords extends PTransform<PCollection<String>,
                PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {

            // Convert lines of text into individual words.
            PCollection<String> words = lines.apply(
                    ParDo.of(new ExtractWordsFn()));

            // Count the number of times each word occurs.
            PCollection<KV<String, Long>> wordCounts =
                    words.apply(Count.<String>perElement());

            return wordCounts;
        }
    }

    public static class ExtractWordsFn extends DoFn<String, String> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Split the line into words.
            String input = c.element();
            String[] words = input.split("[^\\p{L}]+");

            // Output each word encountered into the output PCollection.
            for (String word : words) {
                if (!word.isEmpty()) {
                    c.output(word);
                }
            }
        }
    }

    /** A ParDo that converts a Word and Count into a printable string. */
    public static class FormatAsTextFn extends DoFn<KV<String, Long>, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            KV<String, Long> input = c.element();
            c.output(input.getKey() + ": " + input.getValue());
        }
    }

    public static void main(String args[]) {
        WordCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(WordCountOptions.class);

        Pipeline p = Pipeline.create(options);
        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
                .apply(new CountWords())
                .apply(ParDo.of(new FormatAsTextFn()))
                .apply("WriteCounts", TextIO.write().withoutSharding().to(options.getOutput()));

        p.run().waitUntilFinish();
    }
}

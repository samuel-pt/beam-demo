package com.palmtree.dataflow;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/**
 * Created by sam on 6/5/17.
 */
public interface WordCountOptions extends PipelineOptions {
    @Description("file to be used for word count")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/sample.txt")
    String getInputFile();

    void setInputFile(String value);

    @Description("destination folder")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/")
    String getOutput();

    void setOutput(String value);
}

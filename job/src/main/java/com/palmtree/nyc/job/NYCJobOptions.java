package com.palmtree.nyc.job;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/**
 * Created by sam on 6/5/17.
 */
public interface NYCJobOptions extends PipelineOptions {
    @Description("file to be used for word count")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/job/NYC_Jobs.csv")
    String getJobCsv();

    void setJobCsv(String value);

    @Description("Total jobs destination")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/job/total_jobs")
    String getTotalJobOutput();

    void setTotalJobOutput(String value);

    @Description("Salary Freqeuncy destination")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/job/sal_freq")
    String getSalFreqOutput();

    void setSalFreqOutput(String value);

    @Description("Software Jobs destination")
    @Default.String("/home/sam/work/projects/os/google/cloud-demo/job/software_jobs")
    String getSoftwareJobsOutput();

    void setSoftwareJobsOutput(String value);

}

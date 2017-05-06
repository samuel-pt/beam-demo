package com.palmtree.nyc.job;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 6/5/17.
 */
public class NYCJob {

    public static class ConvertToTableRow extends DoFn<String, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String row = c.element();

            TableRow tableRow = new TableRow();
            String[] columns = row.split(",");
            for (int i = 0, size = columns.length; i < size; i++) {
                String columnName = NYCJobUtils.getColumnNames().get(i);
                tableRow.set(columnName, columns[i]);
            }

            c.output(tableRow);
        }
    }

    public static class CountJobs extends PTransform<PCollection<TableRow>,
            PCollection<Long>> {
        @Override
        public PCollection<Long> expand(PCollection<TableRow> tableRows) {

            // Convert lines of text into individual words.
            PCollection<Long> noOfJobs = tableRows.apply(
                    ParDo.of(new ExtractJobCountFn()));

            // Count the number of times each word occurs.
            PCollection<Long> jobCount =
                    noOfJobs.apply(Sum.longsGlobally());

            return jobCount;
        }

    }

    public static class ExtractJobCountFn extends DoFn<TableRow, Long> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            TableRow row = c.element();

            Object noOfPositions = row.get("#_Of_Positions");
            if (noOfPositions != null) {
                c.output(Long.parseLong(noOfPositions.toString()));
            }
        }
    }

    public static class FormatLongAsTextFn extends DoFn<Long, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output("Total Jobs : " + c.element());
        }
    }

    public static class FormatKVAsTextFn extends DoFn<KV<String, Long>, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            KV<String, Long> input = c.element();
            c.output(input.getKey() + ": " + input.getValue());
        }
    }

    public static class GroupJobs extends PTransform<PCollection<TableRow>,
            PCollection<KV<String, Long>>> {

        @Override
        public PCollection<KV<String, Long>> expand(PCollection<TableRow> tableRows) {

            PCollection<String> salFreq= tableRows.apply(
                    ParDo.of(new ExtractSalFreqFn()));

            PCollection<KV<String, Long>> salFreqCount = salFreq.apply(Count.<String>perElement());
            return salFreqCount;
        }

    }

    public static class ExtractSalFreqFn extends DoFn<TableRow, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            TableRow row = c.element();

            Object salFreq = row.get("Salary_Frequency");
            if (salFreq!= null) {
                c.output(salFreq.toString());
            }
        }
    }

    public static class FilterJobs extends PTransform<PCollection<TableRow>,
            PCollection<String>> {

        @Override
        public PCollection<String> expand(PCollection<TableRow> tableRows) {

            PCollection<TableRow> salFreq= tableRows.apply(
                    ParDo.of(new ExtractSoftwareJobsFn()));

            PCollection<String> softwareJobs = salFreq.apply(ParDo.of(new ConvertToString()));
            return softwareJobs;
        }

    }

    public static class ConvertToString extends DoFn<TableRow, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            TableRow row = c.element();

            List<String> columnNames = NYCJobUtils.getColumnNames();
            List<String> columnValues = new ArrayList<>();

            for (int i = 0, size = columnNames.size(); i < size; i++) {
                String columnName = columnNames.get(i);
                Object columnValue = row.get(columnName);
                if (columnName != null) {
                    columnValues.add(columnValue.toString());
                }
            }

            c.output(String.join(",", columnValues));
        }
    }

    public static class ExtractSoftwareJobsFn extends DoFn<TableRow, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            TableRow row = c.element();

            Object businessTitle = row.get("Business_Title");
            if (businessTitle != null) {
                if (businessTitle.toString().contains("Software")) {
                    c.output(row);
                }
            }
        }
    }

    public static void main(String args[]) {
        NYCJobOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(NYCJobOptions.class);

        Pipeline p = Pipeline.create(options);
        PCollection<TableRow> tableRowPCollection = p.apply("ReadLines", TextIO.read().from(options.getJobCsv()))
                .apply(ParDo.of(new ConvertToTableRow()));
        // Total Job Count
        tableRowPCollection.apply(new CountJobs())
                .apply(ParDo.of(new FormatLongAsTextFn()))
                .apply("WriteCounts", TextIO.write().withoutSharding().to(options.getTotalJobOutput()));
        //Salary Frequency Based Count
        tableRowPCollection.apply(new GroupJobs())
                .apply(ParDo.of(new FormatKVAsTextFn()))
                .apply("WriteCounts", TextIO.write().withoutSharding().to(options.getSalFreqOutput()));
        //Software Jobs count
        tableRowPCollection.apply(new FilterJobs())
                .apply("WriteCounts", TextIO.write().withoutSharding().to(options.getSoftwareJobsOutput()));

        p.run().waitUntilFinish();

    }
}

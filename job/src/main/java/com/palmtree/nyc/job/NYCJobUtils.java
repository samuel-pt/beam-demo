package com.palmtree.nyc.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 6/5/17.
 */
public class NYCJobUtils {
    private static List<String> columnNames = null;
    public static List<String> getColumnNames() {
        if (columnNames == null) {
            columnNames = initColumnNames();
        }

        return columnNames;
    }

    private static List<String> initColumnNames() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Job_ID");
        columnNames.add("Agency");
        columnNames.add("Posting_Type");
        columnNames.add("#_Of_Positions");
        columnNames.add("Business_Title");
        columnNames.add("Civil_Service_Title");
        columnNames.add("Title_Code_No");
        columnNames.add("Level");
        columnNames.add("Salary_Range_From");
        columnNames.add("Salary_Range_To");
        columnNames.add("Salary_Frequency");
        columnNames.add("Work_Location");
        columnNames.add("Division/Work_Unit");
        columnNames.add("Job_Description");
        columnNames.add("Minimum_Qual_Requirements");
        columnNames.add("Preferred_Skills");
        columnNames.add("Additional_Information");
        columnNames.add("To_Apply");
        columnNames.add("Hours/Shift");
        columnNames.add("Work_Location_1");
        columnNames.add("Recruitment_Contact");
        columnNames.add("Residency_Requirement");
        columnNames.add("Posting_Date");
        columnNames.add("Post_Until");
        columnNames.add("Posting_Updated");
        columnNames.add("Process_Date");

        return columnNames;
    }
}

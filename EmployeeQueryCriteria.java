package com.taqniat.ae.elasticsearch.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class EmployeeQueryCriteria {

	private String indexName;

	private String joinDateStart;
	private String joinDateEnd;

	private List<String> states;

	private double salaryMin;
	private double salaryMax;
	private String leaveType;
	private int minimumSickLeaves;

	private String leaveStartDate;
	private String cityStartsWith;
	private String cityEndsWith;
	private double topPercentileSalary;
	private boolean hasNeverTakenLeave;

	private String leaveStartDateFrom; // Start of the last quarter
	private String leaveStartDateTo; // End of the last quarter

	// Address criteria
	private String excludeCountry; // e.g., "USA"
	private String includeCountry; // e.g., "Canada"

	// Salary criteria
	private double minimumSalary;

	// Sick leave criteria
	private String sickLeaveType; // e.g., "sick"
	private int minimumSickLeaveDays;
	private String sickLeaveStartDateFrom; // Start of the current year
	private String sickLeaveStartDateTo;

}

package com.preclaim.dao;

import java.util.List;

import com.preclaim.models.CaseDetailList;
import com.preclaim.models.CaseDetails;
import com.preclaim.models.UserDetails;

public interface CaseDao {
	
	String addBulkUpload(String filename, String username);
	long addcase(CaseDetails casedetail);
	String deleteCase(int caseId);
	String assignToRM(String policyNumber, String username, String caseSubStatus);
	CaseDetails getCaseDetail(int caseID);
	String updateCaseDetails(CaseDetails case_details);
	
	List<CaseDetailList> getPendingCaseList(String username);
	List<CaseDetailList> getAssignedCaseList(String username);
	List<UserDetails> getUserListByRole(String role_code);

}

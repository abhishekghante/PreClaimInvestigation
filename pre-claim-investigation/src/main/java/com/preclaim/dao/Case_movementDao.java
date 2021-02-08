package com.preclaim.dao;

import com.preclaim.models.CaseMovement;

public interface Case_movementDao {

	String CreatecaseMovement(CaseMovement caseMovement);
	CaseMovement getCaseById(long caseId);
	String updateCaseMovement(CaseMovement caseMovement);
}

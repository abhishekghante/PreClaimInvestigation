package com.preclaim.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.preclaim.models.caseMovement;

public class Case_movementDaoImpl implements Case_movementDao {

	@Autowired
	DataSource datasource;

	@Autowired
	private JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public String CreatecaseMovement(caseMovement caseMovement) {

		try
		{
		   String query="INSERT INTO case_movement(caseId, fromID, toId, caseStatus, remarks, createdDate, updatedDate) values(?, ?, ?, ?, '', now(), now()) ";
		   this.template.update(query,caseMovement.getCaseId(), caseMovement.getFromId(), caseMovement.getToId(), caseMovement.getCaseStatus());
		 
		   query="INSERT INTO audit_case_movement(caseId, fromID, toId, caseStatus, remarks, createdDate, updatedDate) values(?, ?, ?, ?, '', now(), now()) ";
		   this.template.update(query,caseMovement.getCaseId(), caseMovement.getFromId(), caseMovement.getToId(),caseMovement.getCaseStatus());
			
	    }
		catch(Exception e) {
			
			e.printStackTrace();
			return "Error adding createCaseMovement";
			
		}
		
		
		return "****";
	}


}

package com.preclaim.dao;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.preclaim.models.CaseMovement;

public class Case_movementDaoImpl implements Case_movementDao {

	@Autowired
	DataSource datasource;

	@Autowired
	private JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public String CreatecaseMovement(CaseMovement caseMovement) {

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

	@Override
	public CaseMovement getCaseById(long caseId) {
		String sql = "SELECT * FROM case_movement where caseId = ?";	
		return template.query(sql, new Object[] {caseId}, (ResultSet rs, int rowNum) -> {
			CaseMovement case_movement = new CaseMovement();
			case_movement.setCaseId(caseId);
			case_movement.setFromId(rs.getString("fromId"));
			case_movement.setToId(rs.getString("toId"));
			case_movement.setCaseStatus(rs.getString("caseStatus"));
			case_movement.setRemarks(rs.getString("Remarks"));
			return case_movement;
		}).get(0);
	}


}

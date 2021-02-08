package com.preclaim.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.preclaim.models.caseMovement;

public class case_movementDaoImpl implements case_movementDao {

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
		   String query="INSERT INTO case_movement( fromID, toId, caseStatus, remarks, createdDate, updatedDate) values(?, ?, 'Open', '', now(), now()) ";
		   this.template.update(query, caseMovement.getFromId(), caseMovement.getToId(), caseMovement.getRemarks());
			
	    }
		catch(Exception e) {
			
			e.printStackTrace();
			return "Error adding createCaseMovement";
			
		}
		
		
		return "****";
	}

}

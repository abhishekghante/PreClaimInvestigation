package com.preclaim.dao;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DashboardDaoImpl implements DashboardDao {

	@Autowired
	DataSource datasource;
	
	@Autowired
	JdbcTemplate template;
	
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public HashMap<String, Integer> getCaseCount(String username) {
		
		HashMap<String, Integer> dashboardCount = new HashMap<String, Integer>();
		
		int count = 0;
		String sql = "SELECT count(*) from case_movement where toId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("Claim Investigation", count);
		
		sql = "SELECT count(*) from case_lists a, case_movement b where a.caseStatus <> 'Closed' and "
				+ "b.toId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("New Cases", count);
		
		sql = "SELECT count(*) from audit_case_movement where fromId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("Assigned Cases", count);
		
		sql = "SELECT count(*) from audit_case_movement a, admin_user b where a.toId = b.username and "
				+ "b.role_name IN ('INV','AGNSUP') and a.fromId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("Assigned Investigation", count);
		
		sql = "SELECT count(*) from audit_case_movement a, case_lists b where b.intimationType IN ('PIV', "
				+ "'PIRV', 'LIVE') and a.toId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("PIV/PIRV/LIVE Pending", count);
		
		sql = "SELECT count(*) from audit_case_movement a, case_lists b where b.intimationType = 'CDP' "
				+ "and a.toId = '" + username + "'";
		template.queryForObject(sql, Integer.class);
		dashboardCount.put("CDP", count);
		
		return dashboardCount;
	}

}

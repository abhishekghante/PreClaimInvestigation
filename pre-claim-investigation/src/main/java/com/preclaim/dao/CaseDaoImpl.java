package com.preclaim.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.preclaim.config.Config;
import com.preclaim.config.CustomMethods;
import com.preclaim.models.CaseDetailList;
import com.preclaim.models.CaseDetails;
import com.preclaim.models.CaseMovement;
import com.preclaim.models.Location;
import com.preclaim.models.UserDetails;

public class CaseDaoImpl implements CaseDao {

	@Autowired
	DataSource datasource;
	
	@Autowired
	JdbcTemplate template;
	
	@Autowired
	InvestigationTypeDao investigationDao;
	
	@Autowired
	IntimationTypeDao intimationTypeDao;
	
	@Autowired
	LocationDao locationDao;
	
	@Autowired
	UserDAO userDao;
	
	@Autowired
	Case_movementDao case_movementDao;
	
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public String addBulkUpload(String filename, String username) {
		
		String extension = StringUtils.getFilenameExtension(filename).toLowerCase();
		String error ="";
		if(extension.equals("xlsx"))
			error = readCaseXlsx(filename, username);
		return error;
	}
	
	@Override
	public long addcase(CaseDetails casedetail) 
	{
		try 
		{
			String query = "INSERT INTO case_lists (policyNumber, investigationId, insuredName, insuredDOD, insuredDOB, sumAssured, "
						+ "intimationType, locationId, caseStatus, nominee_name, nominee_ContactNumber, nominee_address, "
						+ "insured_address, case_description, longitude, latitude, pdf1FilePath , pdf2FilePath, pdf3FilePath, "
						+ "audioFilePath, videoFilePath, signatureFilePath , capturedDate, createdBy, createdDate, updatedDate, updatedBy) "
						+ "values(?, ?, ?, ?, ?, ?, ?, ?, 'Assigned', ?, ?, ?, ?, '', '', '', '', '', '', '', '', '', '', ?, now(), now(), '')";    
			this.template.update(query, casedetail.getPolicyNumber(), casedetail.getInvestigationId(), casedetail.getInsuredName(), 
					casedetail.getInsuredDOD(), casedetail.getInsuredDOB(), casedetail.getSumAssured(), casedetail.getIntimationType(), 
					casedetail.getLocationId(), casedetail.getNominee_name(), casedetail.getNomineeContactNumber(), 
					casedetail.getNominee_address(), casedetail.getInsured_address(), 
					casedetail.getCreatedBy());				    	
			
			query = "SELECT caseId FROM case_lists where policyNumber = ? and createdBy = ? and updatedBy = ''";
			return template.query(query, new Object[] {casedetail.getPolicyNumber(), casedetail.getCreatedBy()}, 
					(ResultSet rs, int rowcount) ->
					{
						return rs.getLong("caseId");
					}).get(0);			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public List<CaseDetailList> getPendingCaseList(String username) {
		try
		{
			String sql ="SELECT * FROM case_lists a, case_movement b where a.caseId = b.caseId and"
					+ " a.caseStatus <> 'Closed' and b.toId = ?"; 			   
			List<CaseDetailList> casedetailList = template.query(sql, new Object[] {username},
					(ResultSet rs, int rowCount) -> 
					{
						CaseDetailList casedetail = new CaseDetailList();
						casedetail.setSrNo(rowCount+1);
						casedetail.setCaseId(rs.getInt("caseId"));
						casedetail.setPolicyNumber(rs.getString("policyNumber"));
						casedetail.setInsuredName(rs.getString("insuredName"));
						casedetail.setInvestigationCategoryId(rs.getInt("investigationId"));
						casedetail.setSumAssured(rs.getDouble("sumAssured"));
						casedetail.setCaseStatus(rs.getString("caseStatus"));
						casedetail.setIntimationType(rs.getString("intimationType"));	
						return casedetail;
					});
			
			List<HashMap<Integer, String>> investigationList = investigationDao.getActiveInvestigationMapping();
			for(HashMap<Integer, String> investigation: investigationList)
			{
				for(CaseDetailList caseDetail: casedetailList)
				{
					if(investigation.get(caseDetail.getInvestigationCategoryId()) != null)
					{
						caseDetail.setInvestigationCategory(investigation.get(caseDetail.getInvestigationCategoryId()));
						continue;
					}
						
				}
			}
			return casedetailList;
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<CaseDetailList> getAssignedCaseList(String username) {
		try
		{
			String sql ="SELECT * FROM case_lists where caseSubStatus <> 'PA' and createdBy = '" + username + "'"; 			   
			List<CaseDetailList> casedetailList = template.query(sql,(ResultSet rs, int rowCount) -> {
						CaseDetailList casedetail=new CaseDetailList();
						casedetail.setSrNo(rowCount+1);
						casedetail.setCaseId(rs.getInt("caseId"));
						casedetail.setPolicyNumber(rs.getString("policyNumber"));
						casedetail.setInsuredName(rs.getString("insuredName"));
						casedetail.setInvestigationCategory(rs.getString("investigationCategory"));
						casedetail.setSumAssured(rs.getInt("sumAssured"));
						casedetail.setCaseStatus(rs.getString("caseStatus"));
						casedetail.setIntimationType(rs.getString("intimationType"));	
						return casedetail;
					});
			return casedetailList;
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	public CaseDetails getCaseDetail(long caseID) {
		try
		{
			String sql = "SELECT * FROM case_lists a where a.caseId = ?";
			     List<CaseDetails> caseDetail = this.template.query(sql, new Object[] {caseID}, 
					(ResultSet rs, int rowCount) -> 
					{
						CaseDetails detail = new CaseDetails();
						detail.setCaseId(rs.getLong("caseId"));
						detail.setPolicyNumber(rs.getString("policyNumber"));
						detail.setInvestigationId(rs.getInt("investigationId"));
						detail.setInsuredName(rs.getString("insuredName"));
						detail.setInsuredDOD(rs.getString("insuredDOD"));
						detail.setInsuredDOB(rs.getString("insuredDOB"));
						detail.setSumAssured(rs.getInt("sumAssured"));
						detail.setIntimationType(rs.getString("intimationType"));
						detail.setLocationId(rs.getInt("locationId"));
						detail.setNominee_name(rs.getString("nominee_name"));
						detail.setNomineeContactNumber(rs.getString("nomineeContactNumber"));
						detail.setNominee_address(rs.getString("nominee_address"));
						detail.setInsured_address(rs.getString("insured_address"));
						detail.setCase_description(rs.getString("CaseDescription"));
						detail.setLongitude(rs.getString("longitude"));
						detail.setLatitude(rs.getString("latitude"));
						detail.setPdf1FilePath(rs.getString("pdf1FilePath"));
						detail.setPdf2FilePath(rs.getString("pdf2FilePath"));
						detail.setPdf3FilePath(rs.getString("pdf3FilePath"));
						detail.setAudioFilePath(rs.getString("audioFilePath"));
						detail.setVideoFilePath(rs.getString("videoFilePath"));
						detail.setSignatureFilePath(rs.getString("signatureFilePath"));
						detail.setCapturedDate(rs.getString("capturedDate"));
						return detail;
					}
					);
			   
			     //Assigner Details
			     sql = "SELECT * FROM case_movement where caseId = " + caseID;
			     CaseMovement case_movement = case_movementDao.getCaseById(caseID);
			     UserDetails user = userDao.getUserDetails(case_movement.getFromId());
			     caseDetail.get(0).setApprovedStatus(case_movement.getCaseStatus());
			     caseDetail.get(0).setAssignerRemarks(case_movement.getRemarks());
			     
			     //Get Role Name
			     caseDetail.get(0).setAssignerRole(userDao.getUserRole(user.getAccount_type()));
			     
			     return caseDetail.get(0);
		          
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String assignToRM(String policyNumber, String caseSubStatus, String username) {
	try {
		
	      String sql="UPDATE case_lists SET caseSubStatus = ?, updatedDate = now(), updatedBy = ? "
	      		+ "where caseSubStatus = 'PA' and policyNumber in (" + policyNumber + ")";
		  this.template.update(sql, caseSubStatus, username);
		  
	   }
	catch(Exception e) 
	{
		return "Error updating region status. Kindly contact system administrator";	
    }
	return "****";	  
	}
	
	@Override
	public String updateCaseDetails(CaseDetails casedetail) {
		try
		{
			String sql = "UPDATE case_lists SET policyNumber = ?, investigationId = ?, insuredName = ?, insuredDOD = ?, insuredDOB = ?, sumAssured = ?, intimationType = ?, claimantCity = ?,"
					+ "claimantZone = ?, claimantState = ?,nominee_name = ?, nomineeContactNumber = ?, nominee_address = ?, "
					+ "insured_address = ?";
			template.update(sql, casedetail.getPolicyNumber(), casedetail.getInvestigationId(), casedetail.getInsuredName(), casedetail.getInsuredDOD(),
					casedetail.getInsuredDOB(), casedetail.getSumAssured(), casedetail.getIntimationType(), casedetail.getClaimantCity(), casedetail.getClaimantZone(), 
					casedetail.getClaimantState(), casedetail.getNominee_name(), casedetail.getNomineeContactNumber(), casedetail.getNominee_address(),
					casedetail.getInsured_address());
					
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "Failed updating Case ID. Kindly contact system administrator";
		}
		return "****";
	}
	
	@Transactional
	public String readCaseXlsx(String filename, String username) {
		try {
			File error_file = new File(Config.upload_directory + "error_log.xlsx");
			if(error_file.exists())
				error_file.delete();
			File file = new File(Config.upload_directory + filename);
			//File not found error won't occur
			List<CaseDetails> caseList = new ArrayList<CaseDetails>();
			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook wb = new XSSFWorkbook(fis);   
			XSSFSheet sheet = wb.getSheetAt(0);
			Iterator<Row> itr = sheet.iterator();    //iterating over excel file  		
			itr.hasNext();
			String error_message = sanityCheck(itr.next());
			if(!error_message.equals("****"))
			{
				wb.close();
				return error_message;
			}
			List<String> investigation_list = investigationDao.getActiveInvestigationStringList();
			List<String> intimation_list = intimationTypeDao.getActiveIntimationTypeStringList();
			List<Location> location_list = locationDao.getActiveLocationList();
			Map<CaseDetails, String> error_case = new HashMap<CaseDetails, String>();
			while (itr.hasNext())                 
			{  
				error_message = "";
				String intimationType = "";	
				Row row = itr.next();
				Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column  
				CaseDetails caseDetails = new CaseDetails();
				Cell cell;
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setPolicyNumber(readCellStringValue(cell).toUpperCase());
					if(!caseDetails.getPolicyNumber().startsWith("C") || 
							caseDetails.getPolicyNumber().startsWith("U"))
						error_message += "Invalid Policy Number, ";
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setInvestigationCategory(readCellStringValue(cell));
					if(!investigation_list.contains(caseDetails.getInvestigationCategory()))
						error_message += "Invalid Investigation Type, ";
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setIntimationType(readCellStringValue(cell));
					if(!intimation_list.contains(caseDetails.getIntimationType()))
						error_message += "Invalid Intimation Type";
					intimationType = caseDetails.getIntimationType().toUpperCase();
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setInsuredName(readCellStringValue(cell));
					if(caseDetails.getInsuredName().equals(""))
					{
						
						if(!(intimationType.equals("PIV") || intimationType.equals("PIRV") || 
								intimationType.equals("LIVE")))
							error_message += "Insured Name is mandatory, ";
					}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					try
					{	
						caseDetails.setInsuredDOD(readCellDateValue(cell));
					}
					catch(Exception ex)
					{
						if(!(intimationType.equals("PIV") || intimationType.equals("PIRV") || 
								intimationType.equals("LIVE")))
							error_message += "Insured DOD is mandatory, ";
					}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					try
					{	
						caseDetails.setInsuredDOB(readCellDateValue(cell));
					}
					catch(Exception e)
					{
						if(!(intimationType.equals("PIV") || intimationType.equals("PIRV") || 
								intimationType.equals("LIVE")))
							error_message += "Insured DOB is mandatory";
					}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					try 
					{
						caseDetails.setSumAssured(readCellIntValue(cell));
					}
					catch(Exception e)
					{
						error_message += "Invalid Sum Assured";
						caseDetails.setSumAssured(0);
					}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setClaimantCity(readCellStringValue(cell));
					for(Location list: location_list)
					{
						if(caseDetails.getClaimantCity().equalsIgnoreCase(list.getCity()))
						{
							caseDetails.setClaimantState(list.getState());
							caseDetails.setClaimantZone(list.getZone());
							break;
						}
					}
					if(caseDetails.getClaimantState().equals(""))
						error_message = "City not present in database";
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();  					
					caseDetails.setNominee_name(readCellStringValue(cell));
					if(caseDetails.getNominee_name().equals(""))
					{
						
						if(!(intimationType.equals("PIV") || intimationType.equals("PIRV") || 
								intimationType.equals("LIVE")))
							error_message += "Nominee Name is mandatory, ";
					}
				}				
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					try
					{
						caseDetails.setNomineeContactNumber(String.valueOf(readCellIntValue(cell)));
					}
					catch(Exception e) {}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setNominee_address(readCellStringValue(cell));
					if(caseDetails.getNominee_address().equals(""))
					{
						
						if(!(intimationType.equals("PIV") || intimationType.equals("PIRV") || 
								intimationType.equals("LIVE")))
							error_message += "Nominee Address is mandatory, ";
					}
				}
				if(cellIterator.hasNext())
				{
					cell = cellIterator.next();
					caseDetails.setInsured_address(readCellStringValue(cell));
					if(caseDetails.getInsured_address().equals(""))
					{
						
						if(!intimationType.equals("CDP"))
							error_message += "Insured Address is mandatory, ";
					}
				}
				if(error_message.equals(""))
				{
					caseDetails.setCaseStatus("Open");
				//	caseDetails.setCaseSubstatus("PA");
					caseList.add(caseDetails);
				}
				else
				{
					error_message = error_message.trim().substring(0, error_message.length());
					error_case.put(caseDetails, error_message);
				}
			}
			wb.close();
			//Error File
			writeErrorCase(error_case);
			
			String sql = "INSERT INTO case_lists(policyNumber, investigationId, insuredName, insuredDOD, insuredDOB, "
					+ "sumAssured, intimationType, claimantCity, claimantZone, claimantState, caseStatus, "
					+ "nominee_name, nomineeContactNumber, nominee_address, insured_address, "
					+ "createdBy, createdDate, updatedDate, updatedBy) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '', '', '', '', '', '', '', '', '', '', ?, now(), "
					+ "'0000-00-00 00:00:00', 0)";
			
			template.batchUpdate(sql,caseList,5,
	                new ParameterizedPreparedStatementSetter<CaseDetails>() {
	                    public void setValues(PreparedStatement ps, CaseDetails caseDetails) throws SQLException          
	                    {
	                    	ps.setString(1, caseDetails.getPolicyNumber());
	                    	ps.setInt(2, caseDetails.getInvestigationId());
	                    	ps.setString(3, caseDetails.getInsuredName());
	                    	ps.setString(4, caseDetails.getInsuredDOD());
	                    	ps.setString(5, caseDetails.getInsuredDOB());
	                    	ps.setInt(6, (int)caseDetails.getSumAssured());
	                    	ps.setString(7, caseDetails.getIntimationType());
	                    	ps.setString(8,caseDetails.getClaimantCity());
	                    	ps.setString(9, caseDetails.getClaimantState());
	                    	ps.setString(10, caseDetails.getClaimantZone());
	                    	ps.setString(11, "Open");
	                    	ps.setString(12,"PA");
	                    	ps.setString(13, caseDetails.getNominee_name());
	                    	ps.setString(14, caseDetails.getNomineeContactNumber());
	                    	ps.setString(15,  caseDetails.getNominee_address());
	                    	ps.setString(16, caseDetails.getInsured_address());
	                    	ps.setString(17, username);
	                    }
	                });
			return "****";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public String readCellStringValue(Cell cell)
	{
		switch (cell.getCellType())               
		{  
			case STRING:  
				return cell.getStringCellValue().trim();  
			case NUMERIC:    //field that represents number cell type  
				return String.valueOf(cell.getNumericCellValue()).trim();
			default:
				return "";
		}
	}
	
	public String readCellDateValue(Cell cell)
	{
			Date date = Date.valueOf(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			return date.toString();		
	}
	
	public int readCellIntValue(Cell cell)
	{
		switch (cell.getCellType())               
		{  
			case STRING:  
				return Integer.parseInt(cell.getStringCellValue());  
			case NUMERIC:    //field that represents number cell type  
				return (int)cell.getNumericCellValue();
			default:
				return 0;
		}
	}
	
	public String sanityCheck(Row row)
	{
		Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column 
		Cell cell;
		ArrayList<String> headerList = CustomMethods.importCaseHeader();
		while(cellIterator.hasNext())
		{
			cell = cellIterator.next();
			if(!headerList.contains(readCellStringValue(cell).trim()))
				return "Invalid File Format";
		}
		return "****";
	}

	@Override
	public String deleteCase(int caseId) {
		try
		
		 {
			String sql="DELETE FROM case_lists where caseId=?";	
			this.template.update(sql,caseId);	
		 }
		catch(Exception e) 
		 {
			e.printStackTrace();
			return "Error Deleting User";
			
		 }
	
		return "****";
	}
	
	private void writeErrorCase(Map<CaseDetails,String> error_case)
	{
		//File error_file = new File(Config.upload_directory + "error_log.xlsx");
		try 
		{
			XSSFWorkbook error_wb = new XSSFWorkbook();
			XSSFSheet error_sheet = error_wb.createSheet("Error Log");
			int rowNum = 0;
			Row newRow = error_sheet.createRow(rowNum);
			ArrayList<String> headerList = CustomMethods.importCaseHeader();
			int colNum = 0;
			for(String header: headerList)
			{
				Cell cell = newRow.createCell(colNum);
				cell.setCellValue(header);
				colNum++;
			}
			Cell cell = newRow.createCell(colNum);
			colNum++;
			cell.setCellValue("Remarks");
			rowNum++;
			newRow = error_sheet.createRow(rowNum);
			for(Map.Entry<CaseDetails, String> entry: error_case.entrySet())
			{
				colNum = 0;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getPolicyNumber());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getInvestigationId());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getIntimationType());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getInsuredName());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getInsuredDOD());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getInsuredDOB());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getSumAssured());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getClaimantCity());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getNominee_name());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getNomineeContactNumber());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getNominee_address());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getKey().getInsured_address());
				colNum++;
				cell = newRow.createCell(colNum);
				cell.setCellValue(entry.getValue());
				rowNum++;
				newRow = error_sheet.createRow(rowNum);
			}
			FileOutputStream outputStream = new FileOutputStream(Config.upload_directory + "error_log.xlsx");
			error_wb.write(outputStream);
			error_wb.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return;
	}

	@Override
	public List<UserDetails> getUserListByRole(String role_code) {
		String sql = "SELECT * FROM admin_user where status = 1 and role_name = ?";
		return template.query(sql, new Object[] {role_code},
				(ResultSet rs, int rowCount) -> 
				{
					UserDetails details = new UserDetails();
					details.setFull_name(rs.getString("full_name"));
					details.setUsername(rs.getString("username"));
					details.setUserID(rs.getInt("user_id"));
					return details;
				});
	}
	
}

package com.preclaim.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.preclaim.config.Config;
import com.preclaim.dao.CaseDao;
import com.preclaim.dao.IntimationTypeDao;
import com.preclaim.dao.InvestigationTypeDao;
import com.preclaim.dao.LocationDao;
import com.preclaim.dao.UserDAO;
import com.preclaim.dao.Case_movementDao;
import com.preclaim.models.CaseDetails;
import com.preclaim.models.ScreenDetails;
import com.preclaim.models.UserDetails;
import com.preclaim.models.CaseMovement;

@Controller
@RequestMapping(value = "/message")
public class CaseController {

	@Autowired
	CaseDao caseDao;
	
	@Autowired
	UserDAO userDao;
	
	@Autowired
	InvestigationTypeDao investigationDao;
	
	@Autowired
	IntimationTypeDao intimationTypeDao;
	
	@Autowired
	LocationDao locationDao;
	
	@Autowired
	Case_movementDao caseMovementDao;
	
    @RequestMapping(value = "/import_case", method = RequestMethod.GET)
    public String import_case(HttpSession session) {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		
		session.removeAttribute("ScreenDetails");
    	ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/import_case.jsp");
    	details.setScreen_title("Import Case");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Bulk case uploads");
    	details.setSub_menu2("App Users");
    	details.setSub_menu2_path("/app_user/app_user");
    	session.setAttribute("ScreenDetails", details);
    	session.setAttribute("userRole", userDao.getAssigneeRole());
    	
    	return "common/templatecontent";
    }
    
    @RequestMapping(value = "/add_message", method = RequestMethod.GET)
    public String add_message(HttpSession session, HttpServletRequest request) {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
    	session.removeAttribute("ScreenDetails");
    	
    	ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/add_message.jsp");
    	details.setScreen_title("Add Cases");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Create Case");
    	details.setSub_menu2("Manage Cases");
    	details.setSub_menu2_path("../message/pending_message.jsp");
    	session.setAttribute("ScreenDetails", details);    	
    
    	session.setAttribute("userRole", userDao.getAssigneeRole());
    	session.setAttribute("investigation_list", investigationDao.getActiveInvestigationList());
    	session.setAttribute("intimation_list", intimationTypeDao.getActiveIntimationType());
    	session.setAttribute("location_list", locationDao.getActiveLocationList());
    	
    	return "common/templatecontent";
    }
    
    @RequestMapping(value = "/pending_message", method = RequestMethod.GET)
    public String pending_message(HttpSession session) {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		session.removeAttribute("ScreenDetails");
    	ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/pending_message.jsp");
    	details.setScreen_title("Pending Cases Lists");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Pending Cases");
    	session.setAttribute("ScreenDetails", details);
    	session.setAttribute("pendingCaseList", caseDao.getPendingCaseList(user.getUsername()));
    	session.setAttribute("investigation_list", investigationDao.getActiveInvestigationList());
    	session.setAttribute("intimation_list", intimationTypeDao.getActiveIntimationType());
    	return "common/templatecontent"; 
    }
  
    @RequestMapping(value = "/active_message", method = RequestMethod.GET)
    public String active_message(HttpSession session) {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		session.removeAttribute("ScreenDetails");
    	
		ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/active_message.jsp");
    	details.setScreen_title("Active Cases Lists");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Case Lists");
    	session.setAttribute("ScreenDetails", details);
    	session.setAttribute("assignCaseList", caseDao.getAssignedCaseList(user.getUsername()));
    	session.setAttribute("investigation_list", investigationDao.getActiveInvestigationList());
    	session.setAttribute("intimation_list", intimationTypeDao.getActiveIntimationType());
    	
    	return "common/templatecontent";
    }
    
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
	public String importData(@RequestParam CommonsMultipartFile userfile,HttpSession session, HttpServletRequest request)
	{
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		session.removeAttribute("ScreenDetails");    	
		
		ScreenDetails details = new ScreenDetails();
		details.setScreen_name("../message/import_case.jsp");
    	details.setScreen_title("Import Case");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Bulk case uploads");
    	details.setSub_menu2("App Users");
    	details.setSub_menu2_path("/app_user/app_user");
    	session.setAttribute("ScreenDetails", details);
				
		//File Uploading Routine
		if(userfile != null)
		{
			try 
			{
				String toId = request.getParameter("assigneeId");
	    		byte[] temp = userfile.getBytes();
	    		String filename = userfile.getOriginalFilename();
	    		filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-SS")) + "_" + filename;
	    		Path path = Paths.get(Config.upload_directory + filename);
	    		System.out.println("Entered");
				Files.write(path, temp);
				String message = caseDao.addBulkUpload(filename, user.getUsername(), toId);
				if(message.equals("****"))
					details.setSuccess_message1("File uploaded successfully");		
				else
					details.setError_message1(message);
				userDao.activity_log("RCUTEAM", "Excel", "BULKUPLOAD", user.getUsername());	
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				details.setError_message1(e.getMessage());
			}    	
		}
	  
		return "common/templatecontent";
	}
    
    @RequestMapping(value = "/addMessage",method = RequestMethod.POST)
   	public @ResponseBody String addMessage(HttpSession session, HttpServletRequest request) 
   	{			
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
       	
		CaseDetails caseDetail=new CaseDetails();
       	caseDetail.setPolicyNumber(request.getParameter("policyNumber"));
       	caseDetail.setInvestigationId(Integer.parseInt(request.getParameter("msgCategory")));
       	caseDetail.setInsuredName( request.getParameter("insuredName"));
       	caseDetail.setInsuredDOD(request.getParameter("insuredDOD"));
       	caseDetail.setInsuredDOB(request.getParameter("insuredDOB"));
       	caseDetail.setSumAssured(Integer.parseInt(request.getParameter("sumAssured")));
       	caseDetail.setIntimationType( request.getParameter("msgIntimationType"));
       	caseDetail.setLocationId(Integer.parseInt(request.getParameter("claimantCity")));
       	caseDetail.setNominee_name(request.getParameter("nomineeName"));
       	caseDetail.setNomineeContactNumber(request.getParameter("nomineeMob"));
       	caseDetail.setNominee_address(request.getParameter("nomineeAdd"));
       	caseDetail.setInsured_address(request.getParameter("insuredAdd"));
		caseDetail.setCreatedBy(user.getUsername()); 
       	long caseId = caseDao.addcase(caseDetail);
       	
       	if(caseId == 0)
       		return "Error adding case";
       	
       	System.out.println("caseDetail============="+caseDetail);
       	CaseMovement caseMovement = new CaseMovement();
       	caseMovement.setCaseId(caseId);
       	caseMovement.setFromId(caseDetail.getCreatedBy());
       	caseMovement.setToId(request.getParameter("assigneeId"));
       	String message = caseMovementDao.CreatecaseMovement(caseMovement);
       	
       	userDao.activity_log("CASE HISTORY", caseDetail.getPolicyNumber(), "ADD CASE", user.getUsername());
   		
       	return message;
   	}
    
    @RequestMapping(value = "/deleteMessage",method = RequestMethod.POST)
    public @ResponseBody String deleteMessage(HttpServletRequest request,HttpSession session) 
    {
		int caseId=Integer.parseInt(request.getParameter("msgId"));
	    String message=caseDao.deleteCase(caseId);  	
	    return message;
    }
    
    @RequestMapping(value = "/edit",method = RequestMethod.GET)
   	public String edit(HttpSession session, HttpServletRequest request) 
   	{			
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		
		session.removeAttribute("ScreenDetails"); 	
		ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/edit_message.jsp");
    	details.setScreen_title("Edit Case");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("Create Case");
    	details.setSub_menu2("Manage Cases");
    	details.setSub_menu2_path("../message/pending_message.jsp");
    	session.setAttribute("ScreenDetails", details);    	
    	session.setAttribute("userRole", userDao.getAssigneeRole());
    	session.setAttribute("location_list", locationDao.getActiveLocationList());
    	session.setAttribute("investigation_list", investigationDao.getActiveInvestigationList());
    	session.setAttribute("intimation_list", intimationTypeDao.getActiveIntimationType());
    	session.setAttribute("case_detail",caseDao.getCaseDetail(Integer.parseInt(request.getParameter("caseId"))));
		
		return "common/templatecontent";
   	}
    
    @RequestMapping(value = "/updateMessageDetails",method = RequestMethod.POST)
    public @ResponseBody String updateMessageDetails(HttpSession session, HttpServletRequest request) {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
       	
		CaseDetails caseDetail = new CaseDetails();
       	caseDetail.setPolicyNumber(request.getParameter("policyNumber"));
       	caseDetail.setInvestigationId(Integer.parseInt(request.getParameter("msgCategory")));
       	caseDetail.setInsuredName(request.getParameter("insuredName"));
       	caseDetail.setInsuredDOD(request.getParameter("insuredDOD"));
       	caseDetail.setInsuredDOB(request.getParameter("insuredDOB"));
       	caseDetail.setSumAssured(Double.parseDouble(request.getParameter("sumAssured")));
       	caseDetail.setIntimationType( request.getParameter("msgIntimationType"));
    	caseDetail.setLocationId(Integer.parseInt(request.getParameter("locationId")));
       	caseDetail.setNominee_name(request.getParameter("nomineeName"));
       	caseDetail.setNomineeContactNumber(request.getParameter("nomineeMob"));
       	caseDetail.setNominee_address(request.getParameter("nomineeAdd"));
       	caseDetail.setInsured_address(request.getParameter("insuredAdd"));
		caseDetail.setUpdatedBy(user.getUsername());
		caseDetail.setCaseId(Long.parseLong(request.getParameter("caseId")));
       	System.out.println(caseDetail.toString());
		String update = caseDao.updateCaseDetails(caseDetail);
       	if(!update.equals("****"))
       		return update;
       	long caseId = caseDetail.getCaseId();
		String toId = request.getParameter("toId");
		String fromId = user.getUsername();
		String toStatus = request.getParameter("toStatus");
		String toRemarks = request.getParameter("toRemarks");
    	CaseMovement case_movement = new CaseMovement(caseId, fromId, toId, toStatus, toRemarks);
    	String message = caseMovementDao.updateCaseMovement(case_movement);
    	
    	userDao.activity_log("CASE HISTORY", caseDetail.getPolicyNumber(), "EDIT CASE", user.getUsername());
   		return message;
    }
    
    @RequestMapping(value = "/assignCase",method = RequestMethod.POST)
    public @ResponseBody String assignToRM(HttpServletRequest request,HttpSession session) 
    {
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
    	long caseId = Integer.parseInt(request.getParameter("caseId"));
		String toId = request.getParameter("toId");
		String fromId = user.getUsername();
		String toStatus = request.getParameter("toStatus");
		String toRemarks = request.getParameter("toRemarks");
    	CaseMovement case_movement = new CaseMovement(caseId, fromId, toId, toStatus, toRemarks);
    	String message = caseMovementDao.updateCaseMovement(case_movement);
    	userDao.activity_log("CASE HISTORY","", "ASSIGN CASE", user.getUsername());
		return message;
		
    }
    
    @RequestMapping(value = "/downloadErrorReport",method = RequestMethod.GET)
    public void downloadErrorReport(HttpServletRequest request, HttpServletResponse response) 
    {
    	ServletContext context = request.getSession().getServletContext();
    	String filename = Config.upload_directory + "error_log.xlsx";
    	File downloadFile = new File(filename);
    	if(!(downloadFile.isFile() && downloadFile.exists()))
    		return;
        try
        {
	    	FileInputStream inputStream = new FileInputStream(downloadFile);
	
	        response.setContentType(context.getMimeType(filename));
	        response.setContentLength((int) downloadFile.length());
	        response.setHeader("Content-Disposition", 
	        		String.format("attachment; filename=\"%s\"", downloadFile.getName()));
	        OutputStream outStream = response.getOutputStream();
	
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	
	        // write bytes read from the input stream into the output stream
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	
	        inputStream.close();
	        outStream.close();
	        downloadFile.delete();
        }
        catch(Exception e)
        {
        	return;
        }
    }
    
    @RequestMapping(value = "/getUserByRole",method = RequestMethod.POST)
    public @ResponseBody List<UserDetails> getUserByRole(HttpServletRequest request,HttpSession session) 
    {
    	String role_code = request.getParameter("role_code");
    	return caseDao.getUserListByRole(role_code);
		
    }
    
    
    @RequestMapping(value = "/case_history",method = RequestMethod.GET)
    public String timeline(HttpServletRequest request,HttpSession session) 
    {			
    	UserDetails user = (UserDetails) session.getAttribute("User_Login");
		if(user == null)
			return "common/login";
		
		long caseId = Long.parseLong(request.getParameter("caseId"));
		
		session.removeAttribute("ScreenDetails"); 	
		ScreenDetails details=new ScreenDetails();
    	details.setScreen_name("../message/timeline.jsp");
    	details.setScreen_title("Case History");
    	details.setMain_menu("Case Management");
    	details.setSub_menu1("");
    	details.setSub_menu2("");
    	details.setSub_menu2_path("../message/pending_message.jsp");
    	session.setAttribute("ScreenDetails", details);
    	session.setAttribute("case_history", caseMovementDao.getCaseMovementHistory(caseId));
    
    	return "common/templatecontent";
   	}
    
    
}

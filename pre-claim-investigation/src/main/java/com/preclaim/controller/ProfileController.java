package com.preclaim.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.preclaim.config.Config;
import com.preclaim.dao.UserDAO;
import com.preclaim.models.ScreenDetails;
import com.preclaim.models.UserDetails;

@Controller
@RequestMapping(value = "/profile")
public class ProfileController {

	@Autowired
	UserDAO dao;
	
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(HttpSession session) {
    	UserDetails user_details = (UserDetails) session.getAttribute("User_Login");
    	if(user_details == null)
    		return "common/login";
    	session.removeAttribute("ScreenDetails");    	
		ScreenDetails details = new ScreenDetails();
    	details.setScreen_name("../profile/edit_profile.jsp");
    	details.setScreen_title("Edit Profile");
    	session.setAttribute("ScreenDetails", details);
		return "common/templatecontent";
    }
    
    @RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
	public @ResponseBody String updateProfile(HttpSession session, HttpServletRequest request)
	{
		UserDetails user_details = (UserDetails) session.getAttribute("User_Login");
		user_details.setFull_name(request.getParameter("full_name"));
		user_details.setUser_email(request.getParameter("user_email"));
		user_details.setUsername(request.getParameter("username"));
		user_details.setPassword(request.getParameter("password"));
		user_details.setUserimage(request.getParameter("account_img"));
		user_details.setUserImageb64(Config.upload_directory + user_details.getUserimage());
		user_details.setUserID(Integer.parseInt(request.getParameter("user_id")));
		user_details.setUpdatedBy(user_details.getUsername());
		session.removeAttribute("User_Login");
		session.setAttribute("User_Login",user_details);
		String message = dao.updateProfile(user_details);
		dao.activity_log("PROFILE", user_details.getUsername(), "UPDATE", user_details.getUsername());
		return message; 
	}
    
}

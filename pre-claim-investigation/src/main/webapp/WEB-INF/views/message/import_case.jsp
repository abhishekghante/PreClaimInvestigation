<%@page import = "java.util.List" %>
<%@page import = "com.preclaim.models.ScreenDetails" %>
<%@page import = "com.preclaim.models.UserRole"%>
<%
List<String>user_permission=(List<String>)session.getAttribute("user_permission");
List<UserRole> userRole =(List<UserRole>)session.getAttribute("userRole");
session.removeAttribute("userRole");
ScreenDetails details = (ScreenDetails) session.getAttribute("ScreenDetails");
%>
<style type="text/css">
#imgAccount { display:none;}
</style>
<div class="row">
  <div class="col-md-12 col-sm-12">
    <div class="portlet box">
      <div class="portlet-title">
        <div class="caption">
            <i class="icon-user font-green-sharp"></i>
            <span class="caption-subject font-green-sharp sbold">Import Cases</span>
        </div>
        <div class="actions">
            <div class="btn-group">
              <a href="${pageContext.request.contextPath}/app_user/app_user" data-toggle="tooltip" 
              	title="Back" class="btn green-haze btn-outline btn-xs pull-right" 
              	style="margin-right: 5px;" data-original-title="Back">
                <i class="fa fa-reply"></i>
              </a>
            </div>
        </div>
      </div>
    </div>
    <div class="box box-primary">
      <!-- /.box-header -->
      <!-- form start -->

      <div class="box-body">
        <div class="row">
          <div class="col-md-12">
          <form id="import_user_form" class="form-horizontal" method = "POST" enctype="multipart/form-data">
              <div class="form-group">
                <label class="col-md-4 padding-left-5 col-xs-4 control-label">Import Data</label>
                <div class="col-md-6 padding-left-0 col-xs-6">
                  <input type="file" name="userfile" id="userfile" class="form-control" required>
                  <note>Kindly upload .xlsx file only</note>
                </div>
                <div class="col-md-2 padding-left-0 col-xs-2">
                  <button type="button" class="btn btn-info btn-sm" name="importfile" id = "importfile" onclick="importData()">
                  	Import
                  </button>
                </div>
                <div class="col-md-12 text-center">
                  <div><a style="display: inline-block;" href="../resources/uploads/Import Case.xlsx">Click to download sample "Excel" file</a></div>
                </div>
              </div>
              <div class="form-group selectDiv">
                <label class="col-md-4 control-label" for="roleName">Select Role Name 
                	<span class="text-danger">*</span></label>
                <div class="col-md-2">
                  <select name="roleName" id="roleName" class="form-control" tabindex="-1" required>
                    <option value="-1" selected disabled>Select</option>
                     <%if(userRole != null){
                    	for(UserRole userRoleLists: userRole){%>
                    	<option value = "<%=userRoleLists.getRole_code()%>"><%=userRoleLists.getRole() %></option>
                    <%}} %> 
                  </select>
                </div>
                
                <label class="col-md-2 control-label" for="assigneeId">Select User 
                	<span class="text-danger">*</span></label>
                <div class="col-md-2">
                  <select name="assigneeId" id="assigneeId" class="form-control" required>
                  	<option value = '-1' selected disabled>Select</option>
                  </select>
                </div>
                
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<script>
$(document).ready(function(){
	<%if(!details.getSuccess_message1().equals("")){%>
		$.ajax({
			url:"${pageContext.request.contextPath}/message/downloadErrorReport",
			type:"GET",
			data:{}
		});
	<%}%>
	<%if(!details.getError_message2().equals("")){%>
		console.log(<%=details.getError_message2()%>);
	<%}%>
});


function importData()
{
	console.log("Enter")
	var userfile = $("#import_user_form #userfile");
	console.log(userfile);
	var roleName = $("#import_user_form #roleName").val();
	var userId = $("#import_user_form #assigneeId").val();
	
	if(roleName == null)
	{
			toastr.error("Kindly select Assignee Role","Error");		
	}
	if(userfile == null)
	{
		toastr.error("Kindly select Excel file","Error");	
	}
	console.log("enter2");
	if(userId == null)
	{
		toastr.error("Kindly select Assignee","Error");
	}

	console.log("enter3");
	$("#importfile").html('<img src="${pageContext.request.contextPath}/resources/img/input-spinner.gif"> Loading...');
	$("#importfile").prop("disabled",true);

	console.log("enter");
	var formData = new FormData();
	formData.append('userfile', userfile.files);
	console.log(formData);
	$.ajax({
	    type: "POST",
	    url: 'importData',
	    data: formData,
	    enctype: 'multipart/form-data',
	    cache: false,
        contentType: false,
        processData: false,
        async:false,
	    success: function(data)
	    {
	  		console.log(data);
	    }
});	

}

</script>



<script>
$("#roleName").change(function(){
	console.log($("#roleName option:selected").val());
	var roleCode = $(this).val();
	$("#assigneeId option").each(function(){
		if($(this).val() != '-1')
			$(this).remove();
	});
	$.ajax({
	    type: "POST",
	    url: 'getUserByRole',
	    data: {"role_code": roleCode},
	    success: function(userList)
	    {
	    	console.log(userList);
	  		var options = "";
	    	for(i = 0; i < userList.length ; i++)
	  			{
	  				options += "<option value ='" + userList[i].username + "'>" + userList[i].full_name + "</option>";  
	  			}
	  		console.log(options);
	    	$("#assigneeId").append(options);
	    }
});

});
</script>
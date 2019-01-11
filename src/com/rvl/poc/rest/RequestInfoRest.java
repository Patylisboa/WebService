package com.rvl.poc.rest;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.rvl.poc.config.DataUtil;
import com.rvl.poc.model.SolutionRVL;
import com.rvl.poc.util.Constant;



@Path("/requestInfo")
public class RequestInfoRest {

	private static final String CHARSET_UTF8 = ";charset=utf-8";

	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON + CHARSET_UTF8)
	@Produces(MediaType.TEXT_PLAIN)
	public String doSolutionUmc( SolutionRVL umc ) throws URISyntaxException
	{
		if((umc.getProductkey() == null || umc.getProductkey().isEmpty()) && (String.valueOf(umc.getid()) == null || umc.getid() == 0))  
			return "Please provide a  ID and  Product Key";
		
		if(umc.getProductkey() == null || umc.getProductkey().isEmpty())
			return "Please provide a Product Key";
		
		if(String.valueOf(umc.getid()) == null || umc.getid() == 0)
			return "Please provide a ID";
							
		String productKeyPost = umc.getProductkey();
		String idPost = String.valueOf(umc.getid());
		String msg = "Start";
		Map<String, String> parameters = new HashMap<String, String>();
		
		try {
			System.out.println("Begin service RVL");
			DataUtil dbUtil = new DataUtil();
			
			parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_GET_GETID);
			parameters.put(Constant.MAP_PRODUCTKEY, productKeyPost);
			parameters.put(Constant.MAP_ID, idPost);
						
			List<SolutionRVL> selectList = dbUtil.doSelect(parameters);
							
			if(selectList.isEmpty()) {
				
				System.out.println("Empty list, adding new solution");
				parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_INSERT_INSERT);
				dbUtil.doInsert(parameters);
				msg = "Insert done";
				
			}else {
				System.out.println("ID found! Updating the solution");
				parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_UPDATE_UPDATES);
				SolutionRVL solutionFound = selectList.get(0);
				parameters.put(Constant.MAP_PRODUCTKEY, umc.getProductkey());
				parameters.put(Constant.MAP_ID, String.valueOf(solutionFound.getid()));
				
				dbUtil.doUpdate(parameters);
				msg = "Update done";
			}
			
			dbUtil.closeConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Something went wrong";

		}
		return msg;
	}
}


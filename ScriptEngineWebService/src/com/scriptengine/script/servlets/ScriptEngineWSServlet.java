package com.scriptengine.script.servlets;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.sun.xml.ws.transport.http.servlet.WSServlet;

/**
 * Servlet implementation class ScriptEngineWSServlet
 */
public class ScriptEngineWSServlet extends WSServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ScriptEngineWSServlet() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

}
//TODO this class can be removed

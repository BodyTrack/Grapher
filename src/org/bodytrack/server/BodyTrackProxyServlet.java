package org.bodytrack.server;

import java.io.IOException;
import java.net.URL;

//import javax.naming.ConfigurationException;
import javax.servlet.ServletException;

import com.woonoz.proxy.servlet.ProxyServlet;

public class BodyTrackProxyServlet extends ProxyServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void init() throws ServletException {
            try {
            	URL url = new URL("http://www2.bodytrack.org/");
            	//URL url = new URL("http://localhost:3000/");
            init(url, 200);
            } catch (IOException e) {
                    throw new ServletException(e);
            }// catch (ConfigurationException e) {
             //       throw new ServletException(e);
             //}
    }       
}

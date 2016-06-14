import java.io.*;
import java.net.*;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */


public class testIntegration {

	    private static final Logger LOG = LoggerFactory.getLogger(testIntegration.class);
	    
	    private String mock_request = "xml/request.in.enveloppe.xml";
	    private String mock_url = "http://localhost:8060/getCustomer";

	    
	    /**
	     * Helper method to copy bytes from an InputStream to an OutputStream.
	     */
	    private static void copyInputStream(InputStream in, OutputStream out) throws Exception {
	        int c = 0;
	        try {
	            while ((c = in.read()) != -1) {
	                out.write(c);
	            }
	        } finally {
	            in.close();
	        }
	    }

	    /**
	     * Helper method to read bytes from an InputStream and return them as a String.
	     */
	    private static String getStringFromInputStream(InputStream in) throws Exception {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        copyInputStream(in, bos);
	        bos.close();
	        return bos.toString();
	    }

	    @Test
	    public void sendRequest() throws Exception {

	        String response;
	        /*
	         * Set up the URL connection to the web service address
	         */
	        URLConnection connection = new URL(mock_url).openConnection();
	        connection.setDoInput(true);
	        connection.setDoOutput(true);


	        OutputStream os = connection.getOutputStream();
	        InputStream fis = readStream(testIntegration.class.getResourceAsStream(mock_request), "utf-8");
	        copyInputStream(fis, os);


	        InputStream is = connection.getInputStream();
	        LOG.info("the response is ====> ");
	        response = getStringFromInputStream(is);
	        LOG.info(response);
	        Assert.assertTrue( (response.contains("name>Michael") && response.contains("surname>Thirion") ));
	    }
	    
	    public static InputStream readStream(InputStream is, String code) {
	    	
	    	StringBuilder sb = new StringBuilder(512);
	    	Reader r=null;
	        try {
	            r = new InputStreamReader(is, code);
	            int c = 0;
	            while ((c = r.read()) != -1) {
	                sb.append((char) c);
	            }
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	        return new ByteArrayInputStream(sb.toString().getBytes());
	    }	    
}
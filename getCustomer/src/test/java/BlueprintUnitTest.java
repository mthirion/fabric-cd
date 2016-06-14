import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.ObjectHelper;
import org.junit.Test;

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


public class BlueprintUnitTest extends CamelBlueprintTestSupport {
	
	private String client = "cxf:bean:Fuse_getCustomerEndpoint";
	private String backend = "cxf:bean:customerServiceEndpoint";
	private String blueprint = "OSGI-INF/blueprint/blueprint.xml";
	private String in_request = "xml/request.in.payload.xml";
	private String service_response = "xml/service_response.xml";
	
	
	@Override
    protected String getBlueprintDescriptor() {
		System.setProperty("org.apache.aries.blueprint.synchronous", "true");
        return blueprint;
    }
    
	
	
	@Override
    public String isMockEndpointsAndSkip(){
		/*
		 * override this method and return the pattern for which endpoints to mock.
		 * use * to indicate all
		 * (uri1 | uri2 | uri")
		 */
        						
		return (backend); 
    }
    
    
      
    
	
    @Test
    public void doTest() throws InterruptedException {

    	
    	MockEndpoint mock_service = getMockEndpoint("mock:"+backend);
    	

    	/* asserting request message
    	 * ========================= */
		
    	mock_service.expectedMessagesMatches(new Predicate() {
	        @Override
	        public boolean matches(Exchange exchange) {
	            	return exchange.getIn().getBody(String.class).contains("externalClientNo>111222");
	            }
        });     	   		

     	mock_service.whenAnyExchangeReceived(new Processor() {

			@Override
			public void process(Exchange arg0) throws Exception {
				// TODO Auto-generated method stub
				String response = convertStreamToString(BlueprintUnitTest.class.getResourceAsStream(service_response) );
				arg0.getOut().setBody(response);
			}
    		
    	});  	
    	
    	mock_service.expectedMessageCount(1);
    
    	
     	
    	/* ===================
    	 * Performing the test 
    	 * =================== */
    	
    	
    	template.setDefaultEndpointUri(client);
    	Object input = readFromFile(BlueprintUnitTest.class.getResourceAsStream(in_request), "utf-8");
    	
    	String response = template.requestBody(input, String.class);
    	if (! (response.contains("name>Michael") && response.contains("surname>Thirion") ) ) {
    			fail();
    	}
    	
    	assertMockEndpointsSatisfied(); 

    }
    
    
    static String convertStreamToString(InputStream is) {
    	System.out.println(Charset.defaultCharset().name());
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    static String getStringFromInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyInputStream(in, bos);
        bos.close();
        return bos.toString();
    }    
    
    static void copyInputStream(InputStream in, OutputStream out) throws Exception {
        int c = 0;
        try {
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } finally {
            in.close();
        }
    }    
    
    
    public static String readFromFile(InputStream is, String code) {
    	
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, code);
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

}
package webservices;

import general.Log;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import beans.PlanningWorks;

import plugins.BasicDataBaseUtils;
import plugins.DatabasePlugin;

public class PlanningWebServiceClient
{
	
	public static HttpEntity callWebService(String url,String soapAction, String soapEnvBody) throws ClientProtocolException, IOException 
	{
        // Create a StringEntity for the SOAP XML.
        String body =soapEnvBody;
        StringEntity stringEntity = new StringEntity(body, "UTF-8");
        stringEntity.setChunked(true);

        // Request parameters and other properties.
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("Accept", "text/xml");
        httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
        httpPost.addHeader("SOAPAction", soapAction);

        // Execute and get the response.
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        String strResponse = null;
        if (entity != null) {
            //strResponse = EntityUtils.toString(entity);
            //System.out.println(strResponse);
        }
        return entity;
    }
	
	@SuppressWarnings({ "rawtypes" })
	public static void main(String[] args) 
	{
		try
		{
			getWorksDetails(con);
    		System.out.println("Completed..");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void getWorksDetails(Connection con) throws Exception
	{
		String serviceurl="http://www.examples.com";
		String soapaction = "http://www.examples.com/SayHello";
		
		String soapmessage="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.dbs.com/\">" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
				"<ser:getWorkDetails/>" +
				"</soapenv:Body>" +
				"</soapenv:Envelope>";
		
		HttpEntity entity=callWebService(serviceurl, soapaction, soapmessage);
        
		if (entity != null) 
		{
        	XMLInputFactory xif = XMLInputFactory.newInstance();
        	StreamSource xml = new StreamSource(entity.getContent());//entity.getContent()
        	XMLStreamReader xsr = xif.createXMLStreamReader(xml);
        	xsr.nextTag(); // Advance to Envelope tag
        	String localname=xsr.getLocalName();
        	while (!localname.equals("getAbcResponse")) // modify this
        	{
        		xsr.nextTag();
        		localname=xsr.getLocalName();
        	}
	       
        	JAXBContext jc = JAXBContext.newInstance(AbcResponse.class);
        	//System.out.println(xsr.getLocalName());
        	Unmarshaller unmarshaller = jc.createUnmarshaller();
        	JAXBElement<AbcResponse> je = unmarshaller.unmarshal(xsr, AbcResponse.class);
	       
        	AbcResponse dataList=je.getValue();
        	
	        if(dataList.getReturn() !=null) {
	        	saveWorkData(dataList,con); // Custom mwthod to save data
			}
		}
	}
}
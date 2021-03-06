package com.fxsession.fastplus.fpf;

/**
 * @author Dmitry Vulf
 * Singleton 
 */

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.openfast.session.FastConnectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FPFXmlSettings {

	private static Logger mylogger = Logger.getLogger(FPFXmlSettings.class);
	
	
	private static FPFXmlSettings instance;
	static public final String ROOT_NODE = "settings";
	static public final String ATTRIBUTE_ID = "id";
	
	static public final String TEMPLATE_FILE ="templateFileName";
	static public final String TRACE_DECODED = "traceDecoded";
	static public final String TRACE_DECODED_FILE = "traceDecodedFile";
	static public final String CONNS_NODE = "connection";

	
	
	private String paramFileName = "fastplus.xml";
	private Document doc;
	
	private FPFXmlSettings () {
	} 
	 
    static {
	 instance = new FPFXmlSettings();	    
    }
	
    private void parseParams(String filename){
		try {
			    mylogger.debug("Reading settings from " + paramFileName);
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				doc = docBuilder.parse (new File(filename));
				doc.getDocumentElement ().normalize ();
	
				
 				NodeList Nodelist = doc.getElementsByTagName(ROOT_NODE);
 	    		int _len=Nodelist.getLength();
 	    		if (_len ==0) {
 	    			throw new IOException("Can't find <settings> attribute  in the fastplus.xml.");
 	    		}
 				
 				
    } catch (Exception err) {
    	mylogger.error("Exiting application.",err);
       	System.exit(1);
    } 
  }

    
	public static FPFXmlSettings getInstance() {
        return instance;
    }	

    /**
     * I assume that paramters xml file is stored in the same folder as this jar if not specified directly - parapath = null 
     */
    public void Init (String parampath) throws FastConnectionException{
    	String localPath;
    	if (parampath==null){
    		File currentJavaJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());   
    		String currentJavaJarFilePath = currentJavaJarFile.getAbsolutePath();
    		String currentRootDirectoryPath = currentJavaJarFilePath.replace(currentJavaJarFile.getName(), "");
    		localPath = currentRootDirectoryPath;
    	} else{
    		localPath = parampath; 	}
    	
    	localPath+= paramFileName;
    	
   		try{
   			File paramFile = new File(localPath);
   			if (!paramFile.exists())
   				throw new IOException ("Parameters file <fastplus.xml> can't be found in " + localPath);
   			/**
			*  Final step to initialize DocumentBuilder   
			*/
   			parseParams(localPath);
   		}
   		catch (Exception e){
   			System.out.println(e.getMessage());
   			throw new FastConnectionException(e);
   		}
    }
    

	/**
	*  nodeName - name of highlevel node
	*  nodeKey - Attribute of the highlevel node
    *  elementName - name of the element
    *  return value of the elementName
    *  e.g. <connection>, <A>, <groupIP> 
	*/
	public String readElement(String nodeName,String nodeAttrib, String elementName){
	
		NodeList nList = doc.getElementsByTagName(nodeName);
		String elValue= null;
		for(int i=0; i<nList.getLength() ; i++){
			Node listNode = nList.item(i);
            if(listNode.getNodeType() == Node.ELEMENT_NODE){
            	Element listElement = (Element) listNode; 
            	String lAttrib = listElement.getAttribute(ATTRIBUTE_ID);
            	if (lAttrib.equals(nodeAttrib)){   //found highlevel node with given attribute
            		NodeList elList = listElement.getElementsByTagName(elementName);
            		for(int k=0; k<elList.getLength() ; k++){
            			Element elElement = (Element)elList.item(k);
            			if (elElement.getNodeName().equals(elementName)){ //found entry with given elementName
            				NodeList zList = elElement.getChildNodes();
            				for (int z=0;z<zList.getLength();z++){
            					elValue = ((Node)zList.item(z)).getNodeValue();
            				}
            			}
            		}
            	}
            }
		}
		if (elValue==null){
			if (mylogger.isDebugEnabled()){
				mylogger.debug("Can't find  parameter in fastplus.xml "+ nodeName+"."+elementName); 				
			}
		}
 	
		return elValue;
	}

	public boolean nodeExists(String nodeName,String nodeAttrib){
	
	    boolean retval = false;
		NodeList nList = doc.getElementsByTagName(nodeName);
		for(int i=0; i<nList.getLength() ; i++){
			Node listNode = nList.item(i);
            if(listNode.getNodeType() == Node.ELEMENT_NODE){
            	Element listElement = (Element) listNode; 
            	String lAttrib = listElement.getAttribute(ATTRIBUTE_ID);
            	if (lAttrib.equals(nodeAttrib)){   //found highlevel node with given attribute
            	 retval = true;
            	}
           	}
        }
	    return retval;
	}
	
	/**
	 * Method to simplify reading from connection structure
	 */
    public static String 	readConnectionElement(String siteName,String elementName){
    	return getInstance().readElement(CONNS_NODE,siteName,elementName);
    }


}

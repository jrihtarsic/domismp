import groovy.sql.Sql;
import java.sql.SQLException;
import java.security.MessageDigest;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.custommonkey.xmlunit.*;

import java.security.PublicKey;
import java.security.cert.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext
import javax.xml.crypto.dsig.XMLSignatureFactory
import javax.xml.crypto.dsig.XMLSignature
import java.util.Iterator;
// Giving error Could not find artifact xmlbeans:xbean:jar:fixed-2.4.0 in cefdigital-releases (https://ec.europa.eu/digital-building-blocks/artifact/content/repositories/eDelivery/)
//import sun.misc.IOUtils;
import java.util.Base64

import java.text.SimpleDateFormat
import com.eviware.soapui.support.GroovyUtils
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep
import groovy.json.JsonSlurper
import groovy.json.JsonOutput



class SMP implements  AutoCloseable
{
	// Database parameters
	def sqlConnection;
	def url;
	def driver;
	def testDatabase="false";
	def messageExchange;
	def context
	def log;
	static def DEFAULT_LOG_LEVEL = true;

	// Table allocated to store the data/parameters of the request.
	def requestDataTable = [];

	// Table allocated to store the data/parameters of the response.
	def responseDataTable = [];

	// Table allocated to store the intermediate data/parameters.
	def tempoContainer = [];

	// String allocated to extract parts of XML.
	def tempoString = null;

	// Table allocated to store metadata.
	def tablebuffer = [];

	//Signature Algorithm
	def String SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
	def String SIGNATURE_XMLNS = "http://www.w3.org/2000/09/xmldsig#";

	// Node container
	def Node nodeContainer = null;

	def dbUser=null
	def dbPassword=null

	static def XSFRTOKEN = null
	static def SYSTEM_USER="system"
	static def SYSTEM_PWD="123456"

	// Constructor of the SMP Class
	SMP(log,messageExchange,context) {
		debugLog("Create SMP instance", log)
		this.log = log
		this.messageExchange = messageExchange;
		this.context=context;
		this.url=context.expand( '${#Project#jdbc.url}' );
		driver=context.expand( '${#Project#jdbc.driver}' );
		testDatabase=context.expand( '${#Project#testDB}' );
		dbUser=context.expand( '${#Project#dbUser}' );
		dbPassword=context.expand( '${#Project#dbPassword}' );
		sqlConnection = null;
		debugLog("SMP instance created", log)
	}

	// Class destructor
	/*
    void finalize(){
        log.info "Test finished."
    }*/
	void close(){
		log.info "Test finished."
	}

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Log information wrapper
	static def void debugLog(logMsg, logObject,  logLevel = DEFAULT_LOG_LEVEL) {
		if (logLevel.toString()=="1" || logLevel.toString() == "true")
			logObject.info (logMsg)
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Simply open DB connection (dev or test depending on testEnvironment variable)
	def openConnection(){
		debugLog("Open DB connections", log)
		if(testDatabase.toLowerCase()=="true") {
			if (sqlConnection) {
				debugLog("DB connection seems already open", log)
			}
			else {
				try{
					if(driver.contains("oracle")){
						// Oracle DB
						GroovyUtils.registerJdbcDriver( "oracle.jdbc.driver.OracleDriver" )
					}else{
						// Mysql DB (assuming fallback: currently, we use only those 2 DBs ...)
						GroovyUtils.registerJdbcDriver( "com.mysql.jdbc.Driver" )
					}
					debugLog("Open connection with url ${url} dbUser=${dbUser} pass=${dbPassword} driver=${driver} |", log)
					sqlConnection = Sql.newInstance(url, dbUser, dbPassword, driver)

				}
				catch (SQLException ex)
				{
					assert 0,"SQLException occurred: " + ex;
				}
			}
		}
		else // testDatabase.toLowerCase()=="false")
			assert 0, "testDatabase param is set not set to true value - would not try to open DB connection"
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Close the DB connection opened previously
	def closeConnection(){
		debugLog("Close DB connection", log)
		if(testDatabase.toLowerCase()=="true"){
			if(sqlConnection){
				sqlConnection.connection.close();
				sqlConnection = null;
			}
		}
		debugLog("DB connection closed", log)
	}

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	def executeListOfSqlQueries(String[] sqlQueriesList) {
		def connectionOpenedInsideMethod = false

		if (!sqlConnection) {
			debugLog("Method executed without connections open to the DB - try to open connection", log)
			openConnection()
			connectionOpenedInsideMethod = true
		}

		for (query in sqlQueriesList) {
			debugLog("Executing SQL query: " + query, log)
			try{
				sqlConnection.execute query
			}
			catch (SQLException ex){
				closeConnection();
				assert 0,"SQLException occurred: " + ex;
			}
		}

		if (connectionOpenedInsideMethod) {
			debugLog("Connection to DB opened during method execution - close opened connection", log)
			closeConnection()
		}
	}

	//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	def executeSqlAndReturnFirstRow(String query) {
		def connectionOpenedInsideMethod = false
		def res

		if (!sqlConnection) {
			debugLog("Method executed without connections open to the DB - try to open connection", log)
			openConnection()
			connectionOpenedInsideMethod = true
		}

		debugLog("Executing SQL query: " + query, log)
		debugLog("Executing SQL query: " + (sqlConnection == null), log)
		try{
			res = sqlConnection.firstRow query
		}
		catch (SQLException ex){
			closeConnection();
			assert 0,"SQLException occurred: " + ex;
		}

		if (connectionOpenedInsideMethod) {
			debugLog("Connection to DB opened during method execution - close opened connection", log)
			closeConnection()
		}
		return res
	}

	def findDomainName() {
		def result = executeSqlAndReturnFirstRow('SELECT DOMAIN_CODE FROM SMP_DOMAIN order by ID')
		return result.domain_code
	}

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//// filterForTestSuite = /PASSING_AUTO_BAMBOO/   // for multiple test suite use more advanced regexp like for example:  /PASSING_AUTO_BAMBOO|PASSING_NOT_FOR_BAMBOO/
//// filterForTestCases = /SMP001.*/   //for single test case use simple regexp like /SMP001.*/

	def cleanAndAddHeaderElement(filterForTestSuite,  filterForTestCases, String fieldName, String newValue = null, restMethodName = 'PUT ServiceGroup') {

		debugLog("START: modyfication of test requests", log)
		context.testCase.testSuite.project.getTestSuiteList().each { testSuite ->
			if (testSuite.getLabel() =~ filterForTestSuite) {
				debugLog("test suite: " + testSuite.getLabel(), log)
				testSuite.getTestCaseList().each { testCase ->
					if (testCase.getLabel() =~ filterForTestCases) {
						debugLog("test label:" + testCase.getLabel(), log)
						testCase.getTestStepList().each {testStep ->
							if (testStep instanceof RestTestRequestStep && testStep.getRestMethod().name == restMethodName) {

								def hOld = testStep.getHttpRequest().getRequestHeaders()
								hOld.remove(fieldName)
								hOld.remove(fieldName.capitalize())
								hOld.remove(fieldName.toUpperCase())
								if (newValue)
									hOld[fieldName] = [newValue]
								testStep.getHttpRequest().setRequestHeaders(hOld)
								debugLog("For testStep:" + testStep.name + "; Header: "  + testStep.getHttpRequest().getRequestHeaders(), log)
							}
						}
					}

				}
			}
		}
		debugLog("END: Modification of requests hedears finished", log)
	}
//=================================================================================
//======================== Initialize the parameters names ========================
//=================================================================================
	def initParameters(String testType, String indicator){
		if(indicator.toLowerCase()=="request"){
			switch(testType.toLowerCase()){
				case "servicemetadata":
					requestDataTable[0]=["0","businessIdSchemeRequest"];
					requestDataTable[1]=["0","ParticipantIdentifierRequest"];
					requestDataTable[2]=["0","documentIdentSchemeRequest"];
					requestDataTable[3]=["0","documentIdentifierRequest"];
					break;
				case "servicegroup":
					requestDataTable[0]=["0","businessIdSchemeRequest"];
					requestDataTable[1]=["0","ParticipantIdentifierRequest"];
					requestDataTable[2]=["0","Extension"];
					requestDataTable[3]=["0","Certificate"];
					break;
				case "redirection":
					requestDataTable[0]=["0","redirectUrl"];
					requestDataTable[1]=["0","CertificateUID"];
					break;
				default:
					log.info "Unknown operation";
			}
		}
		if(indicator.toLowerCase()=="response"){
			switch(testType){
				case "servicemetadata":
					responseDataTable[0]=["0","businessIdSchemeResponse"];
					responseDataTable[1]=["0","ParticipantIdentifierResponse"];
					responseDataTable[2]=["0","documentIdentSchemeRequest"];
					responseDataTable[3]=["0","documentIdentifierRequest"];
					break;
				case "servicegroup":
					responseDataTable[0]=["0","businessIdSchemeResponse"];
					responseDataTable[1]=["0","ParticipantIdentifierResponse"];
					responseDataTable[2]=["0","Extension"];
					responseDataTable[3]=["0","Certificate"];
					break;
				case "redirection":
					responseDataTable[0]=["0","redirectUrl"]
					responseDataTable[1]=["0","CertificateUID"]
					break;
				default:
					log.info "Unknown operation";
			}
		}
	}
//=================================================================================



//=================================================================================
//========================== Extract request parameters ===========================
//=================================================================================
	def extractRequestParameters(String testType, String testStepName="false"){
		def requestContent = null;

		// Load the Request
		requestContent = messageExchange.getOperation();
		assert (requestContent!=null),locateTest()+"Error: Not possible to extract the request content. Request content extracted is empty.";

		// Browse the REST request
		extractFromURL(requestContent.toString());

		switch(testType.toLowerCase()){

		// Extract the Participant Identifier and the Business Identifier Scheme from the Request
			case "servicegroup":
				debugLog("In extractRequestParameters tempoContainer: $tempoContainer", log)
				initParameters("servicegroup","request");
				requestDataTable[0][0] = tempoContainer[0];
				requestDataTable[1][0] = tempoContainer[1];
				if(testStepName.toLowerCase()!="false"){
					requestDataTable[2][0] = extractExtValues(extractTextFromReq(testStepName));
					requestDataTable[3][0] = extractNodeValue("CertificateIdentifier",extractTextFromReq(testStepName));
				}
				break;

				// Extract the Participant Identifier and the document from the Request
			case "servicemetadata":
			case "signature":
				initParameters("servicemetadata","request");
				requestDataTable[0][0] = tempoContainer[0];
				requestDataTable[1][0] = tempoContainer[1];
				requestDataTable[2][0] = tempoContainer[2];
				requestDataTable[3][0] = tempoContainer[3];
				break;


			case "redirection":
				initParameters("redirection","request");
				break;

			default:
				if(testType.toLowerCase()=="contenttype"){
					// Do nothing
					break;
				}
				assert(0), locateTest()+"Error: -extractRequestParameters-Unknown operation: "+testType+"."+" Possible operations: serviceGroup, serviceMetadata, Redirection, Signature, contentType";
				break;
		}
	}
//=================================================================================
	def static getSoapUiCustomProperty(log, context, String custPropName="", String level="testcase",canBeNull=false){	

		def retPropVal=null
		
        switch (level.toLowerCase()) {
            case  "testcase":
                retPropVal=context.expand('${#TestCase#'+custPropName+'}')
                break
            case "testsuite":
                retPropVal=context.expand('${#TestSuite#'+custPropName+'}')
                break
            case "project":
                retPropVal=context.expand('${#Project#'+custPropName+'}')
                break
            default:
				debugLog("Warning:getSoapUiCustomProperty: Unknown type of custom property level: \"$level\". Assuming test case level property", log)
				retPropVal=context.expand('${#TestCase#'+custPropName+'}')
        }
		
		if(!canBeNull){
			assert(retPropVal!= null),"Error:getSoapUiCustomProperty: Couldn't fetch property \"$custPropName\" value at level \"$level\""
			assert(retPropVal.trim()!= ""),"Error:getSoapUiCustomProperty: Property \"$custPropName\" at level \"$level\" returned value is empty."		
		}
		return retPropVal
    }
//=================================================================================
	def static setSoapUiCustomProperty(log,testRunner, String custPropName="", String custPropValue="", level="testcase"){	
        switch (level.toLowerCase()) {
            case  "testcase":
                testRunner.testCase.setPropertyValue(custPropName,custPropValue)
                break
            case "testsuite":
                testRunner.testCase.testSuite.setPropertyValue(custPropName,custPropValue)
                break
            case "project":
                testRunner.testCase.testSuite.project.setPropertyValue(custPropName,custPropValue)
                break
            default:
				debugLog("Warning:setSoapUiCustomProperty: Unknown type of custom property level: \"$level\". Assuming test case level property", log)
				testRunner.testCase.setPropertyValue(custPropName,custPropValue)
        }	
		debugLog("  ====  \"setSoapUiCustomProperty\" DONE.", log)
    }
//=================================================================================
	def static fetchExpectedValues(context, log, String testType){
	
		def expectedParameters=[:]
		
		switch(testType.toLowerCase()){
			case "sgextension":
				expectedParameters["extension"]=getExtensionFromString(context, log,getSoapUiCustomProperty(log, context, "PutResourceRequestExtTemplate", "testsuite",false))
			case "servicegroup":
			case "resource":
				expectedParameters["partId"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierValue", "testcase",false)
				expectedParameters["partScheme"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierScheme", "testcase",true)
				expectedParameters["version"]=getSoapUiCustomProperty(log, context, "version", "testsuite",false)
				break;
			case "subresourcemulti":
			case "servicemetadatamulti":
				expectedParameters["metadata"]=getMetadataFromString(context, log, getSoapUiCustomProperty(log, context, "PutSubresourceRequestTemplateMulti", "testsuite",false))
			case "servicemetadata":
			case "subresource":
				expectedParameters["partId"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierValue", "testcase",false)
				expectedParameters["partScheme"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierScheme", "testcase",true)
				expectedParameters["version"]=getSoapUiCustomProperty(log, context, "version", "testsuite",false)
				expectedParameters["serviceId"]=getSoapUiCustomProperty(log, context, "SubresourceIdentifierValue", "testcase",false)
				expectedParameters["serviceScheme"]=getSoapUiCustomProperty(log, context, "SubresourceIdentifierScheme", "testcase",false)
				if(expectedParameters["metadata"]==null){
					expectedParameters["metadata"]=getMetadataFromString(context, log, getSoapUiCustomProperty(log, context, "PutSubresourceRequestTemplate", "testsuite",false))
				}
				break;
			case "redirection":
				expectedParameters["partId"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierValue", "testcase",false)
				expectedParameters["partScheme"]=getSoapUiCustomProperty(log, context, "ResourceIdentifierScheme", "testcase",true)
				expectedParameters["version"]=getSoapUiCustomProperty(log, context, "version", "testsuite",false)
				expectedParameters["serviceId"]=getSoapUiCustomProperty(log, context, "SubresourceIdentifierValue", "testcase",false)
				expectedParameters["serviceScheme"]=getSoapUiCustomProperty(log, context, "SubresourceIdentifierScheme", "testcase",false)
				expectedParameters["redirectUrl"]=getSoapUiCustomProperty(log, context, "redirectUrl", "testcase",false)
				break;	
			case "contenttype":
				expectedParameters["contenttype"]=getSoapUiCustomProperty(log, context, "contenttype", "testcase",false)
				expectedParameters["charset"]=getSoapUiCustomProperty(log, context, "charset", "testcase",false)
				break;	
			case "signature":
				expectedParameters["smpsignaturemethod"]=getSoapUiCustomProperty(log, context, "smpsignaturemethod", "testsuite",false)
				expectedParameters["smpsignaturesubj"]=getSoapUiCustomProperty(log, context, "smpsignaturesubj", "testsuite",true)
				break;				
			default:
				assert(0), "Error: -fetchExpectedValues-Unknown operation: "+testType+"."+" Possible operations: resource, servicegroup, subresource, servicemetadata, Redirection, Signature, contentType.";
				break;
		}
		return expectedParameters
	}
//=================================================================================
//========================== verify test results ==========================
//=================================================================================
	def static verifyTestResults(context, log, messageExchange, String testType){
		debugLog("Verifying test results ...", log)
		def responseParameters=[:]
		def expectedParameters=[:]
		responseParameters=retrieveResponseParameters(context, log, messageExchange, testType)		
		expectedParameters=fetchExpectedValues(context, log, testType)
		debugLog("responseParameters="+responseParameters, log)
		debugLog("expectedParameters="+expectedParameters, log)
		switch(testType.toLowerCase()){
			case "sgextension":
				assert(compare2XMLs(log,expectedParameters["extension"], responseParameters["extension"])), " Error: extension in the response does not match the expected value "
			case "servicegroup":
			case "resource":
				assert(expectedParameters["partId"].toLowerCase().equals(responseParameters["partId"].toLowerCase())),"Error: ParticipantID response value: "+responseParameters["partId"]+" does not match expected value: "+expectedParameters["partId"] 
				assert(expectedParameters["partScheme"].toLowerCase().equals(responseParameters["partScheme"].toLowerCase())),"Error: ParticipantScheme response value: "+responseParameters["partScheme"]+" does not match expected value: "+expectedParameters["partScheme"]
				assert(expectedParameters["version"].toLowerCase().equals(responseParameters["version"].toLowerCase())),"Error: version response value: "+responseParameters["version"]+" does not match expected value: "+expectedParameters["version"]
				break;
			case "subresourcemulti":
			case "servicemetadatamulti":
			case "servicemetadata":
			case "subresource":
				assert(expectedParameters["partId"].toLowerCase().equals(responseParameters["partId"].toLowerCase())),"Error: ParticipantID response value: "+responseParameters["partId"]+" does not match expected value: "+expectedParameters["partId"] 
				assert(expectedParameters["partScheme"].toLowerCase().equals(responseParameters["partScheme"].toLowerCase())),"Error: ParticipantScheme response value: "+responseParameters["partScheme"]+" does not match expected value: "+expectedParameters["partScheme"]
				assert(expectedParameters["version"].toLowerCase().equals(responseParameters["version"].toLowerCase())),"Error: version response value: "+responseParameters["version"]+" does not match expected value: "+expectedParameters["version"]
				assert(expectedParameters["serviceId"].toLowerCase().equals(responseParameters["serviceId"].toLowerCase())),"Error: serviceId response value: "+responseParameters["serviceId"]+" does not match expected value: "+expectedParameters["serviceId"] 
				assert(expectedParameters["serviceScheme"].toLowerCase().equals(responseParameters["serviceScheme"].toLowerCase())),"Error: serviceScheme response value: "+responseParameters["serviceScheme"]+" does not match expected value: "+expectedParameters["serviceScheme"]
				assert(compare2XMLs(log,expectedParameters["metadata"], responseParameters["metadata"])), " Error: metadata in the response does not match the expected value "
				break;
			case "redirection":
				assert(expectedParameters["partId"].toLowerCase().equals(responseParameters["partId"].toLowerCase())),"Error: ParticipantID response value: "+responseParameters["partId"]+" does not match expected value: "+expectedParameters["partId"] 
				assert(expectedParameters["partScheme"].toLowerCase().equals(responseParameters["partScheme"].toLowerCase())),"Error: ParticipantScheme response value: "+responseParameters["partScheme"]+" does not match expected value: "+expectedParameters["partScheme"]
				assert(expectedParameters["version"].toLowerCase().equals(responseParameters["version"].toLowerCase())),"Error: version response value: "+responseParameters["version"]+" does not match expected value: "+expectedParameters["version"]
				assert(expectedParameters["serviceId"].toLowerCase().equals(responseParameters["serviceId"].toLowerCase())),"Error: serviceId response value: "+responseParameters["serviceId"]+" does not match expected value: "+expectedParameters["serviceId"] 
				assert(expectedParameters["serviceScheme"].toLowerCase().equals(responseParameters["serviceScheme"].toLowerCase())),"Error: serviceScheme response value: "+responseParameters["serviceScheme"]+" does not match expected value: "+expectedParameters["serviceScheme"]
				assert(expectedParameters["redirectUrl"].toLowerCase().equals(responseParameters["redirectUrl"].toLowerCase())),"Error: redirectUrl response value: "+responseParameters["redirectUrl"]+" does not match expected value: "+expectedParameters["redirectUrl"]
				break;
			case  "contenttype":
				assert(expectedParameters["contenttype"].toLowerCase().equals(responseParameters["contenttype"].toLowerCase())),"Error: Content-Type response value: "+responseParameters["contenttype"]+" does not match expected value: "+expectedParameters["contenttype"] 
				assert(expectedParameters["charset"].toLowerCase().equals(responseParameters["charset"].toLowerCase())),"Error: Charset response value: "+responseParameters["charset"]+" does not match expected value: "+expectedParameters["charset"] 
				break;
			case  "signature":
				assert(expectedParameters["smpsignaturemethod"].toLowerCase().equals(responseParameters["smpsignaturemethod"].toLowerCase())),"Error: SMP signature method response value: "+responseParameters["smpsignaturemethod"]+" does not match expected value: "+expectedParameters["smpsignaturemethod"] 
				assert(expectedParameters["smpsignaturesubj"].toLowerCase().equals(responseParameters["smpsignaturesubj"].toLowerCase())),"Error: SMP signature subject response value: "+responseParameters["smpsignaturesubj"]+" does not match expected value: "+expectedParameters["smpsignaturesubj"] 
				break;
			default:
				assert(0), "Error: -verifyTestResults-Unknown operation: "+testType+"."+" Possible operations: resource, servicegroup, subresource, servicemetadata, Redirection, Signature, contentType.";
				break;
		}
		debugLog("Test results verified successfully", log)
				
	}
	
	def static String getMetadataFromString(context, log, String input){
		def certValue="";
		def newCertValue=""
		def certId="contentbinaryobject";
		def stringMeta=extractFromXML(removeNamespaces(input),"ProcessMetadata")	
		def details=null
		def allNodes=null

		if(stringMeta.length()==0){
			stringMeta=extractFromXML(removeNamespaces(input),"ProcessList")
			certId="certificate";
		}		
		details = new XmlSlurper().parseText(input);
		allNodes = details.depthFirst().each{
					if(it.name().toLowerCase().equals(certId)){
						certValue=it.text();
					}
		}
		newCertValue=certValue.replaceAll("\\s","")
		return stringMeta.replace(certValue,newCertValue)
	}

	def static String getExtensionFromString(context, log, String input){
		def stringMeta=extractFromXML(removeNamespaces(input),"SMPExtensions")	

		if(stringMeta.length()==0){
			stringMeta=extractFromXML(removeNamespaces(input),"Extension")
		}		

		return stringMeta
	}
//=================================================================================
//========================== Extract and return response parameters ==========================
//=================================================================================
	def static retrieveResponseParameters(context, log, messageExchange, String testType){
		def parametersMap=[:];
		def ctMap=[];
		def headers=null;
		def allNodes=null;
		// Load the response xml file
		def responseContent = messageExchange.getResponseContentAsXml();
		// Extract the Participant Identifier, the references to the signed metadata and the extensions from the Response
		def ServiceDetails = new XmlSlurper().parseText(responseContent);
		parametersMap["version"]="1.0"
		switch(testType.toLowerCase()){
			case "sgextension":
				parametersMap["extension"]=getExtensionFromString(context, log, responseContent)							
			case "servicegroup":
			case "resource":
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name().toLowerCase().equals("participantid")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@schemeID.text();
					}
					if(it.name().toLowerCase().equals("participantidentifier")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@scheme.text();
					}
					if(it.name().toLowerCase().equals("smpversionid")){
						parametersMap["version"]=it.text();
					}
				}
				break;

			case "subresourcemulti":
			case "servicemetadatamulti":
			case "servicemetadata":
			case "subresource":
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name().toLowerCase().equals("participantid")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@schemeID.text();
					}
					if(it.name().toLowerCase().equals("participantidentifier")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@scheme.text();
					}
					if(it.name().toLowerCase().equals("smpversionid")){
						parametersMap["version"]=it.text();
					}
					if(it.name().toLowerCase().equals("serviceid")){
						parametersMap["serviceId"]=it.text();
						parametersMap["serviceScheme"]=it.@schemeID.text();
					}
					if(it.name().toLowerCase().equals("documentidentifier")){
						parametersMap["serviceId"]=it.text();
						parametersMap["serviceScheme"]=it.@scheme.text();
					}
					parametersMap["metadata"]=getMetadataFromString(context, log, responseContent)
				
				}
				
				break;
			case "redirection":
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name().toLowerCase().equals("participantid")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@schemeID.text();
					}
					if(it.name().toLowerCase().equals("participantidentifier")){
						parametersMap["partId"]=it.text();
						parametersMap["partScheme"]=it.@scheme.text();
					}
					if(it.name().toLowerCase().equals("smpversionid")){
						parametersMap["version"]=it.text();
					}
					if(it.name().toLowerCase().equals("serviceid")){
						parametersMap["serviceId"]=it.text();
						parametersMap["serviceScheme"]=it.@schemeID.text();
					}
					if(it.name().toLowerCase().equals("documentidentifier")){
						parametersMap["serviceId"]=it.text();
						parametersMap["serviceScheme"]=it.@scheme.text();
					}
					if((it.name().toLowerCase().equals("publisheruri")) && (it.parent().name().toLowerCase().equals("redirect"))){
						parametersMap["redirectUrl"]=it.text();
					}
					if(it.name().toLowerCase().equals("redirect")){
						parametersMap["redirectUrl"]=it.@href.text();
					}
				}
				break;
			case "contenttype":
				headers=messageExchange.getResponseHeaders()
				headers.each{ header ->
					if(header.getKey().equals("Content-Type")){
						ctMap=header.getValue()[0].split(";")
						parametersMap["contenttype"]=ctMap[0]
						parametersMap["charset"]=ctMap[1].split("=")[1]
					}
				}
				break;				
			case "signature":
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name().toLowerCase().equals("signaturemethod")){
						parametersMap["smpsignaturemethod"]=it.@Algorithm.text();
					}
					if(it.name().toLowerCase().equals("x509subjectname")){
						parametersMap["smpsignaturesubj"]=it.text();
					}
				}				
				break;
			default:
				assert(0), "Error: -retrieveResponseParameters-Unknown operation: "+testType+"."+" Possible operations: resource, servicegroup, subresource, servicemetadata, Redirection, Signature, contentType.";
				break;
		}
		return parametersMap
	}
//=================================================================================
//========================== Retrieve SMP Property metadata ================================
//=================================================================================
	def static getSmpConfigPropertyMeta(log, context, propName, authenticationUser=SYSTEM_USER, authenticationPwd=SYSTEM_PWD){
		debugLog("Calling \"getSmpConfigPropertyMeta\".", log)
		debugLog("  getSmpConfigPropertyMeta  [][]  Property to get: \"$propName\".", log)
		def jsonSlurper = new JsonSlurper()
		def commandString=null
		def commandResult=null 
		def propMap=null 
		def propMetadata=null
		def propMeta=null
		def urlToSMP=getSoapUiCustomProperty(log, context, "url", "project",false)
		def urlExt="/ui/internal/rest/property?"

		try{
			commandString=["curl", urlToSMP + urlExt + "page=0&pageSize=50&property=$propName",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H",  "Content-Type: text/xml",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(log, context, authenticationUser, authenticationPwd),
                                 "-v"]
			commandResult=runCommandInShell(log, commandString)
			assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:getSmpConfigPropertyMeta: Error while fetching value of property \"$propName\"."
            propMetadata=commandResult[0]
			debugLog("  getSmpConfigPropertyMeta  [][]  Property get result: $propMetadata", log)
            propMap=jsonSlurper.parseText(propMetadata)
            assert(propMap != null),"Error:getSmpConfigPropertyMeta: Error while parsing the returned property value: null result found."
			propMap.serviceEntities.each{ prop ->
				if(prop.property.toLowerCase().equals(propName.toLowerCase())){
					propMeta=prop
				}
			}
			debugLog("  getSmpConfigPropertyMeta  [][]  Property \"$propName\" metadata = \"$propMeta\".", log)
        }finally{
            XSFRTOKEN=null
        }
		return propMeta
	}
//=================================================================================
//========================== Retrieve SMP Property value ================================
//=================================================================================
	def static getSmpConfigPropertyValue(log, context, propName, authenticationUser=SYSTEM_USER, authenticationPwd=SYSTEM_PWD){
		debugLog("Calling \"getSmpConfigPropertyValue\".", log)
		debugLog("  getSmpConfigPropertyValue  [][]  Property to get: \"$propName\".", log)
		
		return getSmpConfigPropertyMeta(log, context, propName, authenticationUser, authenticationPwd).value
	}
//=================================================================================
//========================== Set SMP Property ================================
//=================================================================================
	def static setSmpConfigProperty(log, context, propName, newValue, authenticationUser=SYSTEM_USER, authenticationPwd=SYSTEM_PWD){
		debugLog("Calling \"setSmpConfigProperty\".", log)
		debugLog("  setSmpConfigProperty  [][]  Setting property \"$propName\" to \"$newValue\".", log)
		def updatedProp=[:]
		def updatedPropJson=null
		def updatedPropJsonList=[]
		def commandString=null
		def commandResult=null 
		def urlToSMP=getSoapUiCustomProperty(log, context, "url", "project",false)
		def urlExt="/ui/internal/rest/property"
		
		// Get the propety Metadata
		def propMetadata=getSmpConfigPropertyMeta(log, context, propName, authenticationUser, authenticationPwd)
		
		propMetadata.each{index, val ->
			updatedProp[index]=val
		}
		updatedProp["status"]=1
		updatedProp["deleted"]=false
		updatedProp["value"]=newValue
		
		updatedPropJsonList<<updatedProp
		updatedPropJson=JsonOutput.toJson(updatedPropJsonList).toString()
		
		log.info "=========="
		log.info "updatedPropJson=JsonOutput.toJson(updatedPropJsonList)="+JsonOutput.toJson(updatedPropJsonList)
		log.info "updatedPropJson="+updatedPropJson
		log.info "formatJsonForCurl(log,updatedPropJson)="+formatJsonForCurl(log,updatedPropJson)
		log.info "=========="
		
        commandString=["curl", urlToSMP+urlExt,
                                         "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                         "-H", "Content-Type: application/json",
                                         "-H","X-XSRF-TOKEN: " + returnXsfrToken(log, context, authenticationUser, authenticationPwd),
                                         "-X", "PUT",
                                         "--data", formatJsonForCurl(log,updatedPropJson),
                                         "-v"]	
										 
        commandResult=runCommandInShell(log, commandString)
        assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || commandResult[1].contains("successfully")),"Error:setSmpConfigProperty: Error while trying to connect to the SMP. CommandResult[0]:" +commandResult[0] + "| commandResult[1]:" + commandResult[1]
		debugLog("  setSmpConfigProperty  [][]  Property \"$propName\" update done successfully.", log)		
	}
//============================================================================
//========================== Run curl command ================================
//============================================================================
    static def runCommandInShell(log, inputCommand) {
        debugLog("Calling \"runCommandInShell\".", log)
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
		debugLog("  runCommandInShell  [][]  Run curl command: " + inputCommand, log)
        if (inputCommand) {
            def proc = inputCommand.execute()
            if (proc != null) {
                proc.waitForProcessOutput(outputCatcher, errorCatcher)
            }
        }
		debugLog("  runCommandInShell  [][]  outputCatcher: " + outputCatcher.toString(), log)
		debugLog("  runCommandInShell  [][]  errorCatcher: " + errorCatcher.toString(), log)
        return ([outputCatcher.toString(), errorCatcher.toString()])
    }
	
//=================================================================================
//========================== Get returnXsfr Token =================================
//=================================================================================	
	static String returnXsfrToken(log, context, String userLogin=SYSTEM_USER, passwordLogin=SYSTEM_PWD) {
		def String output=""
		if (XSFRTOKEN == null) {
			output=fetchCookieHeader(log, context, userLogin, passwordLogin)
			XSFRTOKEN = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
		}
		return XSFRTOKEN
	}
//=================================================================================
//=========================== Get Cookie Header ===================================
//=================================================================================	
	static String fetchCookieHeader(log, context, String userLogin = SYSTEM_USER, passwordLogin = SYSTEM_PWD) {
		def json=null
		json = ifWindowsEscapeJsonString('{\"username\":\"' + "${userLogin}" + '\",\"password\":\"' + "${passwordLogin}" + '\"}')
		def urlToSMP=getSoapUiCustomProperty(log, context, "url", "project",false)
		def urlExt="/ui/public/rest/security/authentication"
        def commandString = ["curl", urlToSMP+urlExt,
                             "-i",
                             "-H",  "Content-Type: application/json",
                             "--data-binary", json, "-c", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "--trace-ascii", "-"]		
        def commandResult = runCommandInShell(log, commandString)
        assert(commandResult[0].contains("XSRF-TOKEN")),"Error:Authenticating user: Error while trying to connect to the DomiSMP."
        return commandResult[0]    
	}
//=================================================================================
//===================== Format json input for windows =============================
//=================================================================================		
    static def ifWindowsEscapeJsonString(json) {
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            json = json.replace("\"", "\\\"")
        return json
    }
//=================================================================================
//====================== Format json input for curl ===============================
//=================================================================================		
    static def formatJsonForCurl(log, input) {
        if (System.properties['os.name'].toLowerCase().contains('windows')) {
            assert(input != null),"Error:formatJsonForCurl: input string is null."
            assert(input.contains("[") && input.contains("]")),"Error:formatJsonForCurl: input string is corrupted."
            def intermediate = input.substring(input.indexOf("[") + 1, input.lastIndexOf("]")).replace("\"", "\"\"\"")
            return "[" + intermediate + "]"
        }
        return input
    }
//=================================================================================
//========================== Extract response parameters ==========================
//=================================================================================
	def extractResponseParameters(String testType){
		def headerFound = 0;
		def urlRefCounter = 0;
		def allNodes = null;
		// Load the response xml file
		def responseContent = messageExchange.getResponseContentAsXml();
		// Extract the Participant Identifier, the references to the signed metadata and the extensions from the Response
		def ServiceDetails = new XmlSlurper().parseText(responseContent);
		switch(testType.toLowerCase()){
			case "servicegroup":
				initParameters("servicegroup","response");
				urlRefCounter = 4;
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name()== "ParticipantIdentifier"){
						responseDataTable[1][0]=it.text();
						responseDataTable[0][0]=it.@scheme.text();
					}
					if(it.name()== "ServiceMetadataReference"){
						responseDataTable[urlRefCounter]=[it.@href.text(),"ServiceMetadataReference"];
						urlRefCounter+=1;
					}
					/*if(it.name()== "Extension"){
                        responseDataTable[2][0]=it.text();
                        }*/
					if(it.name()== "CertificateIdentifier"){
						responseDataTable[3][0]=it.text();
					}
				}
				// Extract the extension
				responseDataTable[2][0]=extractExtValues(responseContent.toString());
				break;

			case "servicemetadata":
			case "signature":
				tempoString = null;
				initParameters("servicemetadata","response");
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name()== "ParticipantIdentifier"){
						responseDataTable[1][0]=it.text();
						responseDataTable[0][0]=it.@scheme.text();
					}
					if(it.name()== "DocumentIdentifier"){
						responseDataTable[2][0]=it.@scheme.text();
						responseDataTable[3][0]=it.text();
					}
				}
				tempoString = responseContent.toString();
				break;

			case "redirection":
				initParameters("redirection","response");
				allNodes = ServiceDetails.depthFirst().each{
					if(it.name()== "Redirect"){
						responseDataTable[0][0]=it.@href.text();
					}
					if(it.name()== "CertificateUID"){
						responseDataTable[1][0]=it.text();
					}
				}
				assert((responseDataTable[0][0]!=null)&&(responseDataTable[0][0]!="0")), locateTest()+"Error: Redirection is expected but redirect element was not found in the response.";
				assert((responseDataTable[1][0]!=null)&&(responseDataTable[1][0]!="0")), locateTest()+"Error: Redirection is expected but CertificateUID element was not found in the response.";
				break;

			default:
				// content-Type = text/xml
				if(testType.toLowerCase()=="contenttype"){
					for(header in messageExchange.getResponseHeaders()){
						if((header.toString().contains("Content-Type")) && (header.toString().contains("text/xml"))){
							headerFound = 1;
						}
					}
					assert(headerFound==1), locateTest()+"Error: Header content-Type is not found or is not set to text/xml.";
					break;
				}
				assert(0), locateTest()+"Error: -extractResponseParameters-Unknown operation: "+testType+"."+" Possible operations: serviceGroup, serviceMetadata, Redirection, Signature, contentType.";
				break;
		}
	}
//=================================================================================



//=================================================================================
//========================== Perform test verifications ===========================
//=================================================================================
	def verifyResults(String testType, String expectedResult, String testStepName="false", String redirectURL=null, String redirectCer=null, int nRef=0){
		// In case of testType = "servicegroup",
		debugLog("Entering verifyResults method with testType: $testType, expectedResult: $expectedResult, testStepName: $testStepName, redirectURL: $redirectURL, redirectCer: $redirectCer, nRef: $nRef", log)
		def counter = 0;
		def String reqString = null;
		def String extensionRequest = "0";
		def String extensionResponse = "0";
		def sigAlgo = "0";
		debugLog("Befor extractRequestParameters(testType,testStepName)", log)
		extractRequestParameters(testType,testStepName);
		debugLog("After extractRequestParameters(testType,testStepName)", log)
		extractResponseParameters(testType);
		debugLog("After extractResponseParameters(testType)", log)
		switch(testType.toLowerCase()){
			case "servicegroup":
				if(expectedResult.toLowerCase()=="success"){
					while(counter<4){
						if ((counter==2)&&(requestDataTable[2][0]!="0")){
							if(compareXMLs(responseDataTable[counter][0],requestDataTable[counter][0])==false){
								log.error "Extension in request: "+requestDataTable[counter][0];
								log.error "Extension in response: "+responseDataTable[counter][0];
								assert(0), locateTest()+"Error: Extension returned is different from Extension pushed. For details, please refer to logs in red.";
							}
						}else{
							assert(responseDataTable[counter][0].toLowerCase()==requestDataTable[counter][0].toLowerCase()), locateTest()+"Error: in request, "+requestDataTable[counter][1]+"=\""+requestDataTable[counter][0]+"\""+" wheras in response, "+responseDataTable[counter][1]+"=\""+responseDataTable[counter][0]+"\".";
						}
						counter++;
					}
					counter = 4;
					if(nRef>0){
						assert(nRef+counter==responseDataTable.size()), locateTest()+"Error: Number of ServiceMetadataReference in the response is "+(responseDataTable.size()-counter)+" instead of "+nRef+".";
					}
					while(counter < responseDataTable.size()){
						if(responseDataTable[counter][1]=="ServiceMetadataReference"){
							extractFromURL(responseDataTable[counter][0])
							assert((tempoContainer[0]==responseDataTable[0][0])&&(tempoContainer[1]==responseDataTable[1][0])), locateTest()+"Error: in a ServiceMetadataReference in the response, participant is ("+tempoContainer[0]+","+tempoContainer[1]+") instead of ("+responseDataTable[0][0]+","+responseDataTable[1][0]+").";
						}
						counter++;
					}
					if(nRef>0){
						assert(nRef), locateTest()+"Error: in a ServiceMetadataReference in the response, participant is ("+tempoContainer[0]+","+tempoContainer[1]+") instead of ("+responseDataTable[0][0]+","+responseDataTable[1][0]+").";
					}
				}
				break;
			case "servicemetadata":
				counter = 0;
				if(expectedResult.toLowerCase()=="success"){
					while(counter<4){
						assert(responseDataTable[counter][0]==requestDataTable[counter][0]), locateTest()+"Error: in request, "+requestDataTable[counter][1]+"=\""+requestDataTable[counter][0]+"\""+" wheras in response, "+responseDataTable[counter][1]+"=\""+responseDataTable[counter][0]+"\".";
						counter++;
					}
					extensionResponse=extractExtValues(removeNamespaces(tempoString));
					tempoString = extractPartFromXML(removeNamespaces(tempoString),"servicemetadata");
					reqString = removeNamespaces(extractPartFromXML(extractTextFromReq(testStepName),"servicemetadata"));
					assert(compareMetadata(reqString,tempoString).toLowerCase()=="true"), locateTest()+"Error: in ServiceMetadata returned ------"+tempoString+"------ is not equal to the metadata pushed ------"+reqString+"------.";
					extensionRequest=extractNodeValue("Extension", extractTextFromReq(testStepName),"ServiceInformation");
					extensionRequest=extractExtValues(removeNamespaces(extractTextFromReq(testStepName)));
					if(compareXMLs(extensionRequest,extensionResponse)==false){
						log.error "Extension in request: "+extensionRequest;
						log.error "Extension in response: "+extensionResponse;
						assert(0), locateTest()+"Error: Extension returned is different from Extension pushed. For details, please refer to logs in red.";
					}
				}
				break;
			case "redirection":
				counter = 0;
				requestDataTable[0][0]=redirectURL;
				requestDataTable[1][0]=redirectCer;
				assert(requestDataTable[0][0]==responseDataTable[0][0]), locateTest()+"Error: in ServiceMetadata returned redirect URL is ------"+responseDataTable[0][0]+"------ instead of ------"+requestDataTable[0][0]+"------.";
				assert(requestDataTable[0][0]==responseDataTable[0][0]), locateTest()+"Error: in ServiceMetadata returned certificate is ------"+responseDataTable[1][0]+"------ instead of ------"+requestDataTable[1][0]+"------.";
				break;
			case "signature":
				sigAlgo = extractNodeValue("SignatureMethod", tempoString,null, "Algorithm");
				assert(sigAlgo!= "0"), locateTest()+"Error: Signature Algorithm couldn't be extracted from the response."
				assert(SIGNATURE_ALGORITHM==sigAlgo), locateTest()+"Error: Signature Algorithm is "+sigAlgo+" instead of "+SIGNATURE_ALGORITHM+".";
				// Verify the SMP signature validity
				def Boolean validResult = validateSignature(returnDOMDocument(tempoString));
				assert (validResult == true),locateTest()+"Error: Signature of the SMP is not valid.";
				validResult =false;

				// TODO: Enable the extension signature validation.
				validResult = validateSignatureExtension(returnDOMDocument(tempoString));
				assert (validResult == true),locateTest()+"Error: Signature in the extension is not valid.";
				break;

			default:
				if(testType.toLowerCase()=="contenttype"){
					// Do nothing
					break;
				}
				assert(0), locateTest()+"Error: -verifyResults-Unknown operation: "+testType+"."+" Possible operations: serviceGroup, serviceMetadata, Redirection, Signature, contentType.";
				break;
		}
	}
//=================================================================================



//=================================================================================
//=========================== Extract PUT XML contents ============================
//=================================================================================
	def String extractTextFromReq(String testStepName){
		def fullRequest = context.testCase.getTestStepByName(testStepName);
		assert (fullRequest != null), locateTest()+"Error in function \"extractTextFromReq\": can't find test step name: \""+testStepName+"\"";
		def request = fullRequest.getProperty( "request" );
		def result = request.value.toString();
		result = result.replace("%23","#");
		result = result.replace("%3A",":");
		return result;
	}
//=================================================================================


//=================================================================================
//============================== Extract Node Value ===============================
//=================================================================================
	def String extractNodeValue(String nodeName, String input,String parent=null, String attribute=null){
		def String result = "0";
		if(nodeName=="Extension"){
			result="";
		}
		def rootNode = new XmlSlurper().parseText(input);
		def allNodes = rootNode.depthFirst().each{
			if((it.name()== nodeName)&&((parent==null)||(it.parent().name()==parent))){
				if(attribute==null){
					if(nodeName=="Extension"){
						result=result+it.text();
					}else{
						result=it.text();
					}
				}
				else{
					result=it.@{attribute.toString()}.text();
				}
			}
		}
		if(result==""){
			result="0";
		}
		return result;
	}

	// Extensions are extracted in a different way
	def String extractExtValues(String extInput){
		def String extResult = "";
		def String inputTrimmed=extInput.replaceAll("\n","").replaceAll("\r", "").replaceAll(">\\s+<", "><").replaceAll("%23","#").replaceAll("%3A",":");
		def containerExt = (inputTrimmed =~ /<Extension>((?!<Extension>).)*<\/Extension>/);
		while(containerExt.find()){
			extResult = extResult+containerExt.group();
		}
		if(extResult==""){
			extResult="0";
		}
		//log.info "<AllExtensionsRoot>"+extResult+"</AllExtensionsRoot>";
		return "<AllExtensionsRoot>"+extResult+"</AllExtensionsRoot>";
	}

	// Difference between XMLs
	def Boolean compareXMLs(String request, String response){
		def DetailedDiff myDiff = new DetailedDiff(new Diff(request, response));
		def List allDifferences = myDiff.getAllDifferences();

		if(!myDiff.similar()){
			// Enable for more logs
			for (Object object : allDifferences){
				Difference difference = (Difference)object;
				log.error(difference);
				log.error("============================");
			}
			return false;
		}
		return true;
	}
	
	// Difference between XMLs
	def static Boolean compare2XMLs(log,String request, String response){
		XMLUnit.setIgnoreWhitespace(true);
		def DetailedDiff myDiff = new DetailedDiff(new Diff(request, response));
		def List allDifferences = myDiff.getAllDifferences();

		if(!myDiff.similar()){
			// Enable for more logs
			for (Object object : allDifferences){
				Difference difference = (Difference)object;
				log.error(difference);
				log.error("============================");
			}
			return false;
		}
		return true;
	}
//=================================================================================



//=================================================================================
//=========================== Remove namespaces in XML ============================
//=================================================================================
	def static String removeNamespaces(String input){
		def String result = null;
		result = input.replaceAll(/<\/.{0,4}:/,"</");
		result = result.replaceAll(/<.{0,4}:/,"<");
		result = result.replace("%23","#");
		result = result.replace("%3A",":");
		return result;
	}

//=================================================================================




//=================================================================================
//========================= Extract part of XML contents ==========================
//=================================================================================
	def String extractPartFromXML(String input, String requestName){
		def String startTag = null;
		def String endTag = null;
		def String result = null;

		//if(requestName.toLowerCase()=="servicegroup"){
		//	startTag = "<ServiceMetadataReferenceCollection>";
		//	endTag = "</ServiceGroup>";
		//}
		if(requestName.toLowerCase()=="signature"){
			startTag = "<Signature";
			endTag = "</SignedServiceMetadata>";
		}
		if(requestName.toLowerCase()=="servicemetadata"){
			startTag = "<ProcessList>";
			endTag = "</ServiceInformation>";
		}
		result = input.substring(input.indexOf(startTag), input.indexOf(endTag));
		return result;
	}
	def static String extractFromXML(String input, String target){
		def String startTag = "<"+target+">";
		def String endTag = "</"+target+">";
		def targetSize=endTag.length()
		def String result = null;

		if(input.indexOf(startTag)<0){
			return ""
		}
		result = input.substring(input.indexOf(startTag), input.lastIndexOf(endTag)+targetSize);
		return result;
	}
//=================================================================================



//=================================================================================
//========================= Return hash value of a string =========================
//=================================================================================
	def String returnHash(String input){
		def String result = MessageDigest.getInstance("MD5").digest(input.toLowerCase(Locale.US).bytes).encodeHex().toString()
		return result;
	}
//=================================================================================




//=================================================================================
//========================= Extract duplet or quadruplet ==========================
//=================================================================================
	def extractFromURL(String url){
		def Table1 = [];
		def parts = [];
		def mesure = 0;
		def extraParts = null;
		debugLog("entering extractFromURL", log)

		tempoContainer=["0","0","0","0"];

		Table1 = url.split('/services/');
		parts=Table1[0].split('/');
		mesure=parts.size();
		assert (mesure > 0),locateTest()+"Error: Could not extract the Participant Identifier from the url. Non usual url format.";
		parts[mesure-1]=parts[mesure-1].replace("%3A",":");
		parts[mesure-1]=parts[mesure-1].replace("%23","#");

		if(Table1.size() > 1){
			extraParts=Table1[1]
		}
		Table1 = [];
		Table1 = parts[mesure-1].split('::',2);
		assert (Table1.size()== 2),locateTest()+"Error: Could not extract the Participant Identifier from the url. Non usual url format, :: separator not found";
		tempoContainer[0] = Table1[0];
		tempoContainer[1] = Table1[1];
		debugLog("Filling tempoContainer table", log)
		// TODO FIX this backward compatibility issue
		if (messageExchange.getProperties()) {
			debugLog("Extracting ParticipantIdentifier from property. Table1: Table1", log)
			tempoContainer[0] = messageExchange.getProperty('ParticipantIdentifierScheme')
			tempoContainer[1] = messageExchange.getProperty('ParticipantIdentifier')
		}

		if(extraParts!=null){
			debugLog("Filling tempoContainer table fields 2 and 3. extraParts: $extraParts", log)
			extraParts = extraParts.replace("%3A",":");
			extraParts = extraParts.replace("%23","#");
			Table1 = [];
			Table1=extraParts.split('::',2);
			tempoContainer[2] = Table1[0].replace("%2F","/");
			tempoContainer[3] = Table1[1].replace("%2F","/");
			// TODO FIX this backward compatibility issue
			if (messageExchange.getProperties()) {
				debugLog("Extracting DocTypeIdentifier from property", log)
				tempoContainer[2] = messageExchange.getProperty('DocTypeIdentifierScheme')
				tempoContainer[3] = messageExchange.getProperty('DocTypeIdentifier')
			}
		}
		debugLog("Leaving extractFromURL", log)
	}
//=================================================================================


//=================================================================================
//=============================== Compare 2 Metadata ==============================
//=================================================================================
	def String compareMetadata(String metaData1, String metaData2){
		def i = 0;
		def String outcome = "false";
		def table1 = [];
		def table2 = [];
		table1=parseMetadata("<rootnode>"+metaData1+"</rootnode>");
		table2=parseMetadata("<rootnode>"+metaData2+"</rootnode>");
		outcome = compareTables(table1,table2);
		return (outcome);
	}
//=================================================================================


//=================================================================================
//====================== Parse Metadata and store hash values =====================
//=================================================================================
	def parseMetadata(String metadata){
		def i = 0;
		def result = [];
		def switchProcess = 0;
		def switchEndPoint = 0;
		def String oldProcessId ="0";
		def String oldProcessScheme ="0";
		tablebuffer=["0","0","0","0","0","0","0","0","0","0","0","0","0"];
		def rootMT = new XmlSlurper().parseText(metadata);
		def allNodes = rootMT.depthFirst().each{
			if(it.name()== "ProcessIdentifier"){
				if(switchProcess==0){
					oldProcessScheme=it.@scheme.text();
					oldProcessId=it.text();
					switchProcess=1;
				}else{
					oldProcessScheme=tablebuffer[0];
					oldProcessId=tablebuffer[1];
					switchProcess=0;
				}
				tablebuffer[0]=it.@scheme.text();
				tablebuffer[1]=it.text();
			}
			if(it.name()== "Endpoint"){
				if(switchEndPoint==0){
					switchEndPoint=1;
				}else{
					//charge endpoint
					result[i]=returnHash(tablebuffer.join(","));
					i=i+1;
				}
				tablebuffer[2]=it.@transportProfile.text();
			}
			if(it.name()== "EndpointURI"){
				tablebuffer[3]=it.text().trim();
			}
			if(it.name()== "RequireBusinessLevelSignature"){
				if(it.text()=~ /[f|F][a|A][l|L][S|s]/){
					tablebuffer[4]="0";
				}else{
					if(it.text()=~ /[T|t][R|r][U|u]/){
						tablebuffer[4]="1";
					}
					else{
						tablebuffer[4] = it.text()
					}
				}
			}
			if(it.name()== "ServiceActivationDate"){
				//tablebuffer[5]=it.text();
				tablebuffer[5]=Date.parse("yyyy-MM-dd",it.text());
			}
			if(it.name()== "ServiceExpirationDate"){
				//tablebuffer[6]=it.text();
				tablebuffer[6]=Date.parse("yyyy-MM-dd",it.text());
			}
			if(it.name()== "Certificate"){
				tablebuffer[7]=it.text();
			}
			if(it.name()== "ServiceDescription"){
				tablebuffer[8]=it.text();
			}
			if(it.name()== "TechnicalContactUrl"){
				tablebuffer[9]=it.text();
			}
			if(it.name()== "minimumAuthenticationLevel"){
				tablebuffer[10]=it.text();
			}
			if(it.name()== "TechnicalInformationUrl"){
				tablebuffer[11]=it.text();
			}
			if(it.name()== "extension"){
				tablebuffer[12]=it.text();
			}
		}
		result[i]=returnHash(tablebuffer.join(","));
		return(result);
	}
//=================================================================================



//=================================================================================
//================================ Compare 2 tables ===============================
//=================================================================================
	def String compareTables(tab1,tab2){
		def found = 0;
		if(tab1.size()!=tab2.size()){
			return "false";
		}
		for (String item1 : tab1) {
			for (String item2 : tab2) {
				if(item1 == item2){
					found = 1;
				}
			}
			if(found==0){
				return "false";
			}
			found = 0;
		}
		found = 0;
		for (String item2 : tab2) {
			for (String item1 : tab1) {
				if(item1 == item2){
					found = 1;
				}
			}
			if(found==0){
				return "false";
			}
			found = 0;
		}
		return "true";
	}
//=================================================================================



//=================================================================================
//============= Locate the test case for display it in the error logs =============
//=================================================================================
	def String locateTest(){
		// Returns: "--TestCase--testStep--"
		return("--"+context.testCase.name+"--"+context.testCase.getTestStepAt(context.getCurrentStepIndex()).getLabel()+"--  ");
	}
//=================================================================================



//=================================================================================
//============================== Dump request table ===============================
//=================================================================================
	def String dumpRequestTable(){
		def ii = 0;
		log.info("== Request Table ==");
		while(ii<requestDataTable.size()){
			log.info "--"+requestDataTable[ii][1]+"--"+requestDataTable[ii][0]+"--";
			ii=ii+1;
		}
		log.info("================================");
	}
//=================================================================================


//=================================================================================
//============================== Dump response table ==============================
//=================================================================================
	def String dumpResponseTable(){
		def ii = 0;
		log.info("== Response Table ==");
		while(ii<responseDataTable.size()){
			log.info "--"+responseDataTable[ii][1]+"--"+responseDataTable[ii][0]+"--";
			ii=ii+1;
		}
		log.info("================================");
	}
//=================================================================================


//=================================================================================
//================================== Dump table ===================================
//=================================================================================
	def dumpTable(tableToDump, String name, dimension){
		def ii = 0;
		if(dimension=='2'){
			log.info("== "+name+" Table ==");
			while(ii<tableToDump.size()){
				log.info "--"+tableToDump[ii][1]+"--"+tableToDump[ii][0]+"--";
				ii=ii+1;
			}
		}
		if(dimension=='1'){
			log.info("== "+name+" Table ==");
			while(ii<tableToDump.size()){
				log.info "--"+tableToDump[ii]+"--";
				ii=ii+1;
			}
		}
		log.info("================================");
	}
//=================================================================================

//====================== Signature code ===================================
	def Document returnDOMDocument(String input){
		def Document doc = null;
		try {
			def DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			def DocumentBuilder db = dbf.newDocumentBuilder();
			def InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(input));
			doc = db.parse(is);
		}catch(Exception ex) {
			assert (0),"-- returnDOMDocument function -- Error occurred while trying to build document from String: "+ex;
		}
		return(doc);
	}

	def Certificate decodeX509Certificate(Document doc){
		def Certificate cert = null;
		def String certMessage = null;

		// Check Certificate
		def Element smpSig = findElement(doc,"X509Certificate","SMP",null);
		assert (smpSig != null),locateTest()+"Error: SMP X509Certificate Signature not found in the response.";

		certMessage=smpSig.getTextContent();
		def CertificateFactory cf = CertificateFactory.getInstance("X509");
		//def InputStream is = new ByteArrayInputStream(new sun.misc.BASE64Decoder().decodeBuffer(certMessage));
		def InputStream is = Base64.decoder.decode(certMessage);
		cert =cf.generateCertificate(is);
		return (cert);
	}

	def Boolean validateSignature(Document doc){
		def Boolean validFlag = true;

		// Find the signature of the SMP node to extract the signature algorithm
		def Element smpSig = findElement(doc,"Signature","SMP",SIGNATURE_XMLNS);
		assert (smpSig != null),locateTest()+"Error: SMP Signature not found in the response.";

		def PublicKey publicKey = decodeX509Certificate(doc).getPublicKey();
		def XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		def DOMValidateContext valContext = new DOMValidateContext(publicKey, smpSig);
		valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);

		// Unmarshal the XMLSignature.
		def XMLSignature signature = fac.unmarshalXMLSignature(valContext);
		try {
			validFlag = signature.validate(valContext);
		}catch(Exception ex) {
			assert (0),"-- validateSignature function -- Error occurred while trying to validate the signature: "+ex;
		}

		return (validFlag);
	}

	def Boolean validateSignatureExtension(Document doc){
		def Boolean validFlag = true;

		// Find the signature of the SMP node to extract the signature algorithm
		def Element smpSig = findElement(doc,"Signature","Extension",SIGNATURE_XMLNS);
		if(smpSig==null){
			log.info "No extension Signature.";
			return(true);
		}

		def PublicKey publicKey = decodeX509Certificate(doc).getPublicKey();
		def XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		def DOMValidateContext valContext = new DOMValidateContext(publicKey, smpSig);
		valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);


		// Unmarshal the XMLSignature.
		def XMLSignature signature = fac.unmarshalXMLSignature(valContext);
		//displaySignatureInfo(signature,valContext);

		try {
			validFlag = signature.validate(valContext);
		}catch(Exception ex) {
			assert (0),"-- validateSignatureExtension function -- Error occurred while trying to validate the signature: "+ex;
		}
		if(validFlag==false){
			printErrorSigValDetails(valContext,signature);
		}
		return (validFlag);
	}



	def Element findElement(Document doc, String elementName, String target, String nameSpace){
		def elements =null;
		if(nameSpace!=null){
			elements = doc.getElementsByTagNameNS(nameSpace, elementName);
		}else{
			elements = doc.getElementsByTagName(elementName);
		}
		if(target=="SMP"){
			if(elements.getLength()>1){
				return (Element) elements.item(1);
			}
			if(elements.getLength()==1){
				return (Element) elements.item(0);
			}
			if(elements.getLength()<1){
				return null;
			}
		}else{
			if(elements.getLength()>1){
				return (Element) elements.item(0);
			}else{
				return null;
			}
		}
	}

	def printErrorSigValDetails(DOMValidateContext valContext, XMLSignature signature){
		boolean sv = signature.getSignatureValue().validate(valContext);
		log.info("signature validation status: " + sv);
		if (sv == false) {
			// Check the validation status of each Reference.
			Iterator i1 = signature.getSignedInfo().getReferences().iterator();
			//log.info i1.getAt(0);
			//log.info i1.getAt(1);
			//log.info i1.toString();
			for (int j = 0; i1.hasNext(); j++) {
				boolean refValid = ((org.jcp.xml.dsig.internal.dom.DOMReference) i1.next()).validate(valContext);
				log.info("ref[" + j + "] validity status: " + refValid);
			}
		}
	}

	def displaySignatureInfo(XMLSignature signature,DOMValidateContext valContext){
		log.info"======== Signature ========";
		log.info "- Signature Value: "+signature.getSignatureValue().getValue();
		log.info"===========================";
	}

}

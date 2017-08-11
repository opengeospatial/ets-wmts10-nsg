package nsg.wmts10.testsuite.getfeatureinfo;

import ets.wmts10.testsuite.getfeatureinfo.AbstractBaseGetFeatureInfoFixture;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.logging.Level;

import javax.xml.soap.SOAPMessage;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import static de.latlon.ets.core.assertion.ETSAssert.assertUrl;

import de.latlon.ets.core.util.TestSuiteLogger;
import ets.wmts10.core.domain.ProtocolBinding;
import ets.wmts10.core.domain.WMTS_Constants;
import ets.wmts10.core.domain.WmtsNamespaces;
import ets.wmts10.core.util.ServiceMetadataUtils;
import ets.wmts10.core.util.WMTS_SOAPcontainer;

/*
*
* @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
* @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
*
*/
// -------

public class GetFeatureInfoSoap extends AbstractBaseGetFeatureInfoFixture
{
/*---
	NSG Requirement 9: 
		An NSG WMTS server shall implement SOAP encoding using HTTP POST transfer of the GetFeatureInfo operation 
		request, using SOAP version 1.2 encoding. 	
---*/
	
	private URI getFeatureInfoURI = null;
	
	private boolean _debug = false;
	
	//--	private String _debugSOAPurl = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";
	//--	private String _debugSOAPurl = "https://basemap.nationalmap.gov:443/arcgis/services/USGSTopo/MapServer";

	// ---
	
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 9", dependsOnMethods = "verifyGetFeatureInfoSupported")
	public void wmtsGetFeatureInfoSOAPRequestsExists()
	{
		getFeatureInfoURI = ServiceMetadataUtils.getOperationEndpoint_SOAP(wmtsCapabilities, WMTS_Constants.GET_FEATURE_INFO, ProtocolBinding.POST);
/*--
if ( getFeatureInfoURI==null)
{
soapURI = URI.create(this._debugSOAPurl);
}
--*/			
		assertTrue(this.getFeatureInfoURI != null, "GetFeatureInfo (POST) endpoint not found in ServiceMetadata capabilities document or WMTS does not support SOAP.");
	}
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 9", dependsOnMethods = "wmtsGetFeatureInfoSOAPRequestsExists")
	public void wmtsGetTileRequestFormatParameters( ITestContext testContext ) 
	{
		if ( getFeatureInfoURI == null )
		{
			getFeatureInfoURI = ServiceMetadataUtils.getOperationEndpoint_SOAP( this.wmtsCapabilities, WMTS_Constants.GET_FEATURE_INFO, ProtocolBinding.POST );
		}
		String soapURIstr = getFeatureInfoURI.toString(); 
		assertUrl(soapURIstr);
		
		try
		{
			XPath xPath = createXPath();
			
			String layerName = this.reqEntity.getKvpValue(WMTS_Constants.LAYER_PARAM);
			if ( layerName == null)
			{
				NodeList layers = ServiceMetadataUtils.getNodeElements( wmtsCapabilities, "//wmts:Contents/wmts:Layer/ows:Identifier");
				if ( layers.getLength() > 0)
				{
					layerName = ((Node)layers.item(0)).getTextContent().trim();
				}			
			}
		
		// --- get the prepopulated KVP parameters, for the SOAP parameters
		
			String style = this.reqEntity.getKvpValue(WMTS_Constants.STYLE_PARAM);
			String tileMatrixSet = this.reqEntity.getKvpValue(WMTS_Constants.TILE_MATRIX_SET_PARAM);
			String tileMatrix = this.reqEntity.getKvpValue(WMTS_Constants.TILE_MATRIX_PARAM);
			String tileRow = this.reqEntity.getKvpValue(WMTS_Constants.TILE_ROW_PARAM);
			String tileCol = this.reqEntity.getKvpValue(WMTS_Constants.TILE_COL_PARAM);
			
			String pixelI = this.reqEntity.getKvpValue(WMTS_Constants.I_PARAM);
			String pixelJ = this.reqEntity.getKvpValue(WMTS_Constants.J_PARAM);
			
			String infoFormat = this.reqEntity.getKvpValue(WMTS_Constants.INFO_FORMAT_PARAM);
//infoFormat = "text/xml; subtype=\"gml/3.1.1/profiles/gmlsf/1.0.0/0\"";
//infoFormat = "application/soap+xml"; 
			String requestFormat = this.reqEntity.getKvpValue(WMTS_Constants.FORMAT_PARAM);  	
			
			WMTS_SOAPcontainer soap = new WMTS_SOAPcontainer(WMTS_Constants.GET_FEATURE_INFO, soapURIstr);
			
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.LAYER_PARAM, layerName);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.STYLE_PARAM, style);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.FORMAT_PARAM, requestFormat);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_MATRIX_SET_PARAM, tileMatrixSet);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_MATRIX_PARAM, tileMatrix);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_ROW_PARAM, tileRow);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_COL_PARAM, tileCol);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.I_PARAM, pixelI);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.J_PARAM, pixelJ);
			soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.INFO_FORMAT_PARAM, infoFormat);
		
			SOAPMessage soapResponse = soap.getSOAPresponse(true);
			assertTrue(soapResponse != null, "SOAP reposnse came back null");
		
			Document soapDocument = (Document)soap.getResponseDocument();
		
				//String imageString = (String)createXPath().evaluate("//wmts:BinaryPayload/wmts:BinaryContent", soapDocument,XPathConstants.STRING);
				
			//String formatStr = ServiceMetadataUtils.getNodeText(xPath, soapDocument, "//wmts:BinaryPayload/wmts:Format");
						
				//storeSoapResponseImage( soapResponse, "Requirement9", "simple", formatStr );
				/*-- */
			this.parseNodes(soapDocument,0);
			/*--
			NodeList nodes = (NodeList)ServiceMetadataUtils.getNodeElements(xPath, soapDocument, ".");
			NodeList nodes1 = (NodeList)ServiceMetadataUtils.getNodeElements(xPath, soapDocument, "//FeatureCollection");
			NodeList nodes2 = (NodeList)ServiceMetadataUtils.getNodeElements(xPath, soapDocument, "//ows:FeatureCollection");
			NodeList nodes3 = (NodeList)ServiceMetadataUtils.getNodeElements(xPath, soapDocument, "//wmts:FeatureCollection");
			NodeList nodes4 = (NodeList)ServiceMetadataUtils.getNodeElements(xPath, soapDocument, "//gml:FeatureCollection");
			--*/
			boolean _break = true;
				/* --*/
				//Assert.assertEquals(formatStr, requestFormat, "SOAP response received format: " + formatStr + " but expected: " + requestFormat);
//			 assertXPath( "//wmts:Capabilities/@version = '1.0.0'", soapDocument, NS_BINDINGS );
//				WmtsAssertion.assertXPath( sa, "//wmts:BinaryPayload/wmts:Format = '" + requestFormat + "'", soapDocument, NS_BINDINGS );
	
		}
		catch ( XPathExpressionException | XPathFactoryConfigurationException xpe)
		{
			System.out.println(xpe);
			TestSuiteLogger.log(Level.WARNING, "Invalid or corrupt SOAP content or XML format", xpe);
			if ( this._debug )
			{
				xpe.printStackTrace();
			}
		} 
	}
	
	// ---

	// ---
	   

	
	// --- --------
	
/*--- */   
    private void parseNodes(Node n, int level)
    {       	
    	if ( n != null)
    	{
    		String nam = n.getNodeName();
    		String val = n.getNodeValue();
    		String lnm = n.getLocalName();
    		//String txt = n.getTextContent().trim();
    		
    		if (!nam.contains(":") && !nam.startsWith("#"))
    		{
    			String namespaceURI = n.getNamespaceURI();
    			if ( namespaceURI.contains("soap"))
    				nam = "soap:" + nam;
    			else if ( namespaceURI.contains("ows"))
    				nam = "ows:" + nam;
    			else if ( namespaceURI.contains("wmts"))
    				nam = "wmts:" + nam;
    			
    		}
    		
    		for (int i=0; i<level; i++)
    			System.out.print("\t");
    		System.out.println("Node: " + nam + " = " + val );//+ "( or:  " + txt + " )");
    		parseNodes(n.getFirstChild(), level+1);
    		
    		parseNodes(n.getNextSibling(), level);
    	}
    }
/*  ---*/
		

    
	// --- 
/*---	  */
	private XPath createXPath()
               throws XPathFactoryConfigurationException
	{
		XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext( NS_BINDINGS );
		return xpath;
	}
/* ---*/
}
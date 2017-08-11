package nsg.wmts10.testsuite.getfeatureinfo;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import javax.xml.xpath.XPathExpressionException;

import org.testng.annotations.Test;
import org.w3c.dom.NodeList;

import ets.wmts10.core.domain.ProtocolBinding;
import ets.wmts10.core.domain.WMTS_Constants;
import ets.wmts10.core.util.ServiceMetadataUtils;
import ets.wmts10.testsuite.getfeatureinfo.AbstractBaseGetFeatureInfoFixture;

/*
*
* @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
* @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
*
*/
public class GetFeatureInfoResponse extends AbstractBaseGetFeatureInfoFixture 
{
/*---
	NSG Requirement 20: 
			An NSG WMTS server shall provide the GetFeatureInfo output format in text/XML and text/HTML.  
---*/
	
	//private URI getFeatureInfoURI;	
	
	
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 20", dependsOnMethods = "verifyGetFeatureInfoSupported")
	public void wmtsGetFeatureInfoRespondWith() 
	{
		NodeList infoFormats = null;
		try 
		{
			infoFormats = (NodeList)ServiceMetadataUtils.getNodeElements(wmtsCapabilities, "//wmts:InfoFormat[text() = 'text/xml']");
			if (( infoFormats == null ) || (infoFormats.getLength() <= 0 ))
			{
				infoFormats = (NodeList)ServiceMetadataUtils.getNodeElements(wmtsCapabilities, "//wmts:InfoFormat[text() = 'text/html']");
			}
		}
		catch (XPathExpressionException xpe) 
		{
			// TODO Auto-generated catch block
			//xpe.printStackTrace();
		}
		assertFalse((( infoFormats == null ) || (infoFormats.getLength() <= 0 )), "This WMTS does not support 'text/xml' or 'text/html' for GetFeatureInfo function.");
		
		//getFeatureInfoURI = ServiceMetadataUtils.getOperationEndpoint_KVP( this.wmtsCapabilities, WMTS_Constants.GET_FEATURE_INFO, ProtocolBinding.GET );
		//assertTrue(getFeatureInfoURI != null, "GetFeatureInfo (GET) endpoint not found or KVP is not supported in ServiceMetadata capabilities document.");
	}
	   
	// ---
	

	
	
}
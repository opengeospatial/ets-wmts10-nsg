package nsg.wmts10.testsuite.gettile;

import ets.wmts10.testsuite.gettile.AbstractBaseGetTileFixture;
import nsg.wmts10.testsuite.getcapabilities.GetCapabilitiesKvpFormatTest;

import java.net.URI;

import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;
//import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertTrue;

import com.sun.jersey.api.client.ClientResponse;

import de.latlon.ets.core.error.ErrorMessage;
import de.latlon.ets.core.error.ErrorMessageKey;
import static ets.wmts10.core.assertion.WmtsAssertion.assertStatusCode;
import static ets.wmts10.core.assertion.WmtsAssertion.assertContentType;
import ets.wmts10.core.domain.ProtocolBinding;
import ets.wmts10.core.domain.WMTS_Constants;
import ets.wmts10.core.util.ServiceMetadataUtils;

/*
*
* @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
* @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
*
*/
public class GetTileOfferings extends AbstractBaseGetTileFixture
{
/*---
	NSG Requirement 14: 
		An NSG WMTS server shall offer tiles in the image/png, image/jpeg, and image/gif file formats. 	
---*/
		
	private URI getTileURI = null;
	
	//private boolean _debug = false;
	
	// ---
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14")
	public void wmtsGetCapabilitiiesWithXML(ITestContext testContext)
	{
		GetCapabilitiesKvpFormatTest gck = new GetCapabilitiesKvpFormatTest();
		gck.init(testContext);
		
		gck.TestXML();
	}
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14")
	public void wmtsGetCapabilitiiesWithHTML(ITestContext testContext)
	{
		GetCapabilitiesKvpFormatTest gck = new GetCapabilitiesKvpFormatTest();
		gck.init(testContext);
		
		gck.TestHTML();
	}
	
	// ---

	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14")
	public void wmtsGetCapabilitiiesInEnglish()
	{
		//assertTrue(true);
		throw new SkipException("Test for results in English not implemented.");
	}
	
	// ---	
	
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14", dependsOnMethods = "verifyGetTileSupported")
	public void wmtsGetTileKVPRequestsExists() 
	{
		getTileURI = ServiceMetadataUtils.getOperationEndpoint_KVP( this.wmtsCapabilities, WMTS_Constants.GET_TILE, ProtocolBinding.GET );
		assertTrue(getTileURI != null, "GetTile (GET) endpoint not found or KVP is not supported in ServiceMetadata capabilities document.");
	}
	   
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14", dependsOnMethods = "wmtsGetTileKVPRequestsExists")
	public void wmtsGetTileOfferingsTestPNG()
	{
		checkGetTileWithImageFormat(WMTS_Constants.IMAGE_PNG);
	}
	
	// ---

	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14", dependsOnMethods = "wmtsGetTileKVPRequestsExists")
	public void wmtsGetTileOfferingsTestJPG()
	{
		checkGetTileWithImageFormat(WMTS_Constants.IMAGE_JPEG);
	}
	
	// ---

	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 14", dependsOnMethods = "wmtsGetTileKVPRequestsExists")
	public void wmtsGetTileOfferingsTestGIF()
	{
		checkGetTileWithImageFormat(WMTS_Constants.IMAGE_GIF);
	}
	
	// ---

	// --- -------
	
	private void checkGetTileWithImageFormat(String requestFormat)
	{
		if ( getTileURI == null )
		{
			getTileURI = ServiceMetadataUtils.getOperationEndpoint_KVP( this.wmtsCapabilities, WMTS_Constants.GET_TILE, ProtocolBinding.GET );
		}
		
		this.reqEntity.removeKvp( WMTS_Constants.FORMAT_PARAM );
		this.reqEntity.addKvp( WMTS_Constants.FORMAT_PARAM, requestFormat );

		ClientResponse rsp = wmtsClient.submitRequest( this.reqEntity, getTileURI );
        
		storeResponseImage( rsp, "Requirement14", "simple", requestFormat );

		assertTrue( rsp.hasEntity(), ErrorMessage.get( ErrorMessageKey.MISSING_XML_ENTITY ) );
		assertStatusCode(  rsp.getStatus(),  200 );
		assertContentType( rsp.getHeaders(), requestFormat );
	}
	
	// ---
				
/* --    
	private XPath createXPath()
               throws XPathFactoryConfigurationException
	{
		XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext( NS_BINDINGS );
		return xpath;
	}
/*  --*/
}
package org.opengeospatial.cite.wmts10.nsg.testsuite.getcapabilities;

import static de.latlon.ets.core.assertion.ETSAssert.assertUrl;
import static de.latlon.ets.core.assertion.ETSAssert.assertXPath;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import javax.xml.soap.SOAPMessage;

import org.opengeospatial.cite.wmts10.ets.core.domain.ProtocolBinding;
import org.opengeospatial.cite.wmts10.ets.core.domain.WMTS_Constants;
import org.opengeospatial.cite.wmts10.ets.core.domain.WmtsNamespaces;
import org.opengeospatial.cite.wmts10.ets.core.util.ServiceMetadataUtils;
import org.opengeospatial.cite.wmts10.ets.core.util.WMTS_SOAPcontainer;
import org.opengeospatial.cite.wmts10.ets.testsuite.getcapabilities.AbstractBaseGetCapabilitiesFixture;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 *
 * @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 *
 */
public class GetCapabilitiesSoap extends AbstractBaseGetCapabilitiesFixture {
    /**
     * --- NSG Requirement 3: An NSG WMTS server shall generate a ServiceMetadata document in response to a SOAP encoded
     * GetCapabilities request. ---
     */
    private URI soapURI;

    GetCapabilitiesSoap() {
    }

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 3", dependsOnMethods = "verifyGetCapabilitiesSupported")
    public void wmtsCapabilitiesSOAPCapable() {
        soapURI = ServiceMetadataUtils.getOperationEndpoint_SOAP( wmtsCapabilities, WMTS_Constants.GET_CAPABILITIES,
                                                                  ProtocolBinding.POST );
        assertTrue( this.soapURI != null, "This WMTS does not support SOAP or does not " );
    }

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 3", dependsOnMethods = "wmtsCapabilitiesSOAPCapable")
    public void wmtsCapabilitiesSOAPReponseTest() {
        assertTrue( soapURI != null, "There is no SOAP URL to test against" );

        String soapURIstr = soapURI.toString();
        assertUrl( soapURIstr );

        WMTS_SOAPcontainer soap = new WMTS_SOAPcontainer( WMTS_Constants.GET_CAPABILITIES, soapURIstr );

        soap.AddParameterWithChild( WmtsNamespaces.serviceOWS, WMTS_Constants.ACCEPT_VERSIONS_PARAM,
                                    WMTS_Constants.VERSION_PARAM, WMTS_Constants.VERSION );
        soap.AddParameterWithChild( WmtsNamespaces.serviceOWS, WMTS_Constants.ACCEPT_FORMAT_PARAM,
                                    WMTS_Constants.OUTPUT_PARAM, WMTS_Constants.SOAP_XML );

        SOAPMessage soapResponse = soap.getSOAPresponse( true );
        assertTrue( soapResponse != null, "SOAP reposnse came back null" );

        Document soapDocument = soap.getResponseDocument();
        assertXPath( "//wmts:Capabilities/@version = '1.0.0'", soapDocument, NS_BINDINGS );

    }

}
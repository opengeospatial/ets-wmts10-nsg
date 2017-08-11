package ets.wmts10.testsuite.getfeatureinfo;

import static org.testng.Assert.assertNotNull;

import javax.xml.xpath.XPathExpressionException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Node;

import ets.wmts10.core.client.WmtsKvpRequest;
import ets.wmts10.core.domain.WMTS_Constants;
import ets.wmts10.core.util.ServiceMetadataUtils;
import ets.wmts10.core.util.request.WmtsKvpRequestBuilder;
import ets.wmts10.testsuite.AbstractBaseGetFixture;


/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a> (original)
 * @author Jim Beatty (modified/fixed May/Jun/Jul-2017 for WMS/WMTS)
 */
public abstract class AbstractBaseGetFeatureInfoFixture extends AbstractBaseGetFixture 
{
    /**
     * Builds a {WmtsKvpRequest} representing a GetMap request.
     * @throws XPathExpressionException in case bad XPath
     */
    @BeforeClass
    public void buildGetFeatureInfoRequest() 
    		throws XPathExpressionException
    {
    	this.reqEntity = WmtsKvpRequestBuilder.buildGetFeatureInfoRequest( wmtsCapabilities, layerInfo );
    }
    
    @Test
    public void verifyGetFeatureInfoSupported() 
    {
    	Node getFeatureInfoEntry = null;
    	try
    	{
			getFeatureInfoEntry = (Node)ServiceMetadataUtils.getNode(wmtsCapabilities, "//ows:OperationsMetadata/ows:Operation[@name = 'GetFeatureInfo']");
		} 
    	catch (XPathExpressionException e) 
    	{
		}
    	assertNotNull( getFeatureInfoEntry, "GetFeatureInfo is not supported by this WMTS" );
    }
    
    
/*--
    protected NodeList parseFeatureMemberNodes( Document entity )
                    throws XPathFactoryConfigurationException, XPathExpressionException 
    {
        String xPathAbstract = "//*[local-name() = 'FeatureInfoResponse']/*[local-name() = 'FIELDS']";
        return (NodeList) createXPath().evaluate( xPathAbstract, entity, XPathConstants.NODESET );
    }
--*/
    // ---
/*--    
    protected XPath createXPath()
                    throws XPathFactoryConfigurationException {
        XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext( NS_BINDINGS );
        return xpath;
    }
    --*/
    // --- --------
    
 
}
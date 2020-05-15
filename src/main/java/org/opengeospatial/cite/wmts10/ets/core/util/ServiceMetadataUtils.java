package org.opengeospatial.cite.wmts10.ets.core.util;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengeospatial.cite.wmts10.ets.core.domain.BoundingBox;
import org.opengeospatial.cite.wmts10.ets.core.domain.LayerInfo;
import org.opengeospatial.cite.wmts10.ets.core.domain.ProtocolBinding;
import org.opengeospatial.cite.wmts10.ets.core.domain.WmtsNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.latlon.ets.core.util.NamespaceBindings;
import de.latlon.ets.core.util.TestSuiteLogger;

/**
 * Provides various utility methods for accessing service metadata.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a> (original)
 * @author Jim Beatty (modified/fixed May/Jun/Jul-2017 for WMS and/or WMTS)
 */
public final class ServiceMetadataUtils {

    private static final Logger LOGR = Logger.getLogger( ServiceMetadataUtils.class.getName() );

    private static final NamespaceBindings NS_BINDINGS = WmtsNamespaces.withStandardBindings();

    private ServiceMetadataUtils() {
    }

    /**
     * Extracts a request KVP endpoint from a WMTS capabilities document. If the request URI contains a query component
     * it is removed (but not from the source document).
     * 
     * @param wmtsMetadata
     *            the document node containing service metadata (OGC capabilities document).
     * @param opName
     *            the operation (request) name
     * @param binding
     *            the message binding to use (if {@code null} any supported binding will be used)
     * 
     * @return the URI referring to a request endpoint, <code>null</code> if no matching endpoint is found
     * 
     */
    public static URI getOperationEndpoint_KVP( final Document wmtsMetadata, String opName, ProtocolBinding binding ) {
        return getOperationEndpoint( wmtsMetadata, binding, "KVP", opName );
    }

    /**
     * Extracts a request REST endpoint from a WMTS capabilities document. If the request URI contains a query component
     * it is removed (but not from the source document).
     * 
     * @param wmtsMetadata
     *            the document node containing service metadata (OGC capabilities document).
     * @param opName
     *            the operation (request) name
     * @param binding
     *            the message binding to use (if {@code null} any supported binding will be used)
     * 
     * @return the URI referring to a request endpoint, <code>null</code> if no matching endpoint is found
     * 
     */
    public static URI getOperationEndpoint_REST( final Document wmtsMetadata, String opName, ProtocolBinding binding ) {
        return getOperationEndpoint( wmtsMetadata, binding, "RESTful", opName );
    }

    /**
     * Extracts a request SOAP endpoint from a WMTS capabilities document. If the request URI contains a query component
     * it is removed (but not from the source document).
     * 
     * @param wmtsMetadata
     *            the document node containing service metadata (OGC capabilities document).
     * @param opName
     *            the operation (request) name
     * @param binding
     *            the message binding to use (if {@code null} any supported binding will be used)
     * 
     * @return the URI referring to a request endpoint, <code>null</code> if no matching endpoint is found
     * 
     */
    public static URI getOperationEndpoint_SOAP( final Document wmtsMetadata, String opName, ProtocolBinding binding ) {
        return getOperationEndpoint( wmtsMetadata, binding, "SOAP", opName );
    }

    private static URI getOperationEndpoint( final Document wmtsMetadata, ProtocolBinding binding, String protocol,
                                             String opName ) {
        if ( ( binding == null ) || binding.equals( ProtocolBinding.ANY ) ) {
            binding = getOperationBindings( wmtsMetadata, opName ).iterator().next();
        }
        if ( binding == null )
            return null;

        try {
            String xPathString = "//ows:OperationsMetadata/ows:Operation[@name = '%s'and ( ./ows:Constraint/ows:AllowedValues/ows:Value = '%s' or ./ows:DCP/ows:HTTP/ows:%s/ows:Constraint/ows:AllowedValues/ows:Value = '%s')]/ows:DCP/ows:HTTP/ows:%s[descendant::ows:Value[text()='%s']]/@xlink:href";
            String xPathExpr = String.format( xPathString, opName, protocol, binding.getElementName(), protocol, binding.getElementName(), protocol );
            XPath xPath = createXPath();
            String href = getNodeText( xPath, wmtsMetadata, xPathExpr );
            return createEndpoint( href );
        } catch ( XPathExpressionException ex ) {
            TestSuiteLogger.log( Level.INFO, ex.getMessage() );
        }
        return null;
    }

    /**
     * Determines which protocol bindings are supported for a given operation.
     * 
     * @param wmtsMetadata
     *            the capabilities document (wmts:Capabilities), never <code>null</code>
     * @param opName
     *            the name of the WMTS operation
     * @return A Set of protocol bindings supported for the operation. May be empty but never <code>null</code>.
     */
    public static Set<ProtocolBinding> getOperationBindings( final Document wmtsMetadata, String opName ) {
        Set<ProtocolBinding> protoBindings = new HashSet<>();

        if ( isOperationBindingSupported( wmtsMetadata, opName, ProtocolBinding.GET ) )
            protoBindings.add( ProtocolBinding.GET );
        if ( isOperationBindingSupported( wmtsMetadata, opName, ProtocolBinding.POST ) )
            protoBindings.add( ProtocolBinding.POST );

        return protoBindings;
    }

    public static NodeList getNodeElements( XPath xPath, Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        if ( xPath == null )
            xPath = createXPath();
        return (NodeList) xPath.evaluate( xPathAbstract, wmtsCapabilities, XPathConstants.NODESET );
    }

    public static NodeList getNodeElements( Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        return getNodeElements( null, wmtsCapabilities, xPathAbstract );
    }

    public static Node getNode( XPath xPath, Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        if ( xPath == null )
            xPath = createXPath();
        return (Node) xPath.evaluate( xPathAbstract, wmtsCapabilities, XPathConstants.NODE );
    }

    public static Node getNode( Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        return getNode( null, wmtsCapabilities, xPathAbstract );
    }

    public static String getNodeText( XPath xPath, Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        if ( xPath == null )
            xPath = createXPath();
        return (String) xPath.evaluate( xPathAbstract, wmtsCapabilities, XPathConstants.STRING );
    }

    public static String getNodeText( Node wmtsCapabilities, String xPathAbstract )
                            throws XPathExpressionException {
        return getNodeText( null, wmtsCapabilities, xPathAbstract );
    }

    /**
     * Parses the configured formats for the given operation.
     * 
     * @param wmtsCapabilities
     *            the capabilities document (wmts:Capabilities), never <code>null</code>
     * @param layerName
     *            the name of the selected layer
     * @param childElement
     *            the child element(s) of the layer
     * @return a list of the child elements by the operation, never <code>null</code>
     */

    public static NodeList parseLayerChildElements( Document wmtsCapabilities, String layerName, String childElement ) {
        if ( !childElement.startsWith( "ows:" ) && !childElement.startsWith( "wmts:" ) )
            childElement = "wmts:" + childElement; // --- apply default namespace if none given

        String xPathExpr = "//wmts:Contents/wmts:Layer[ows:Identifier = '" + layerName + "']/" + childElement;
        try {
            return getNodeElements( wmtsCapabilities, xPathExpr );
        } catch ( XPathExpressionException ex ) {
            TestSuiteLogger.log( Level.INFO, ex.getMessage() );
        }
        return null;
    }

    /**
     * Parses all named layers from the capabilities document.
     * 
     * @param wmtsCapabilities
     *            the capabilities document (wmts:Capabilities), never <code>null</code>
     * @return a list of {@link LayerInfo}s supported by the WMTS, never <code>null</code>
     */
    public static List<LayerInfo> parseLayerInfo( Document wmtsCapabilities ) {
        ArrayList<LayerInfo> layerInfos = new ArrayList<>();
        XPath xPath = createXPath();
        try {
            NodeList layerNodes = parseLayers( xPath, wmtsCapabilities );

            for ( int layerNodeIndex = 0; layerNodeIndex < layerNodes.getLength(); layerNodeIndex++ ) {
                Node layerNode = layerNodes.item( layerNodeIndex );
                LayerInfo layerInfo = parseLayerInfo( xPath, layerNode );
                layerInfos.add( layerInfo );
            }
        } catch ( XPathExpressionException xpe ) {
            throw new RuntimeException( "Error evaluating XPath expression against capabilities doc. ", xpe );
        } catch ( ParseException e ) {
            throw new RuntimeException( "Error parsing layer infos from doc. ", e );
        }

        return layerInfos;
    }

    public static NodeList parseLayers( XPath xPath, Document wmtsCapabilities ) {
        try {
            return getNodeElements( xPath, wmtsCapabilities, "//wmts:Contents/wmts:Layer" );
        } catch ( XPathExpressionException xpe ) {
            throw new RuntimeException( "Error collecting layers from the Service Metadata Capabilities doc. ", xpe );
        }
    }

    public static NodeList parseLayers( Document wmtsCapabilities ) {
        return parseLayers( createXPath(), wmtsCapabilities );
    }

    public static String parseNodeElementName( XPath xPath, Node nodeElement )
                            throws XPathExpressionException {
        if ( xPath == null )
            xPath = createXPath();
        String name = (String) xPath.evaluate( "ows:Identifier", nodeElement, XPathConstants.STRING );
        return name.trim();
    }

    public static String parseNodeElementName( Node nodeElement )
                            throws XPathExpressionException {
        return parseNodeElementName( createXPath(), nodeElement );
    }

    /**
     * Parses the BoundingBox from the layer; either as the WGS84 Bbox, or one of the other listed ones
     * 
     * @param bboxNode
     *            node of the layer, never <code>null</code> wgs84 if parsing the WGS84 bounding box
     * @param presumedWGS84
     *            are we working in WGS84 bounding box or not
     * 
     * @return the {@link BoundingBox} - crs is CRS:84 if wgs84 is true, otherwise parse from attribute
     */
    public static BoundingBox parseBoundingBox( Node bboxNode, boolean presumedWGS84 ) {
        XPath xPath = createXPath();

        String crsID = "";
        BoundingBox bbox = null;
        try {
            NodeList coordsList = (NodeList) bboxNode.getChildNodes();
            if ( coordsList.getLength() > 0 ) {
                Node cornerCoordNode = (Node) xPath.evaluate( "//ows:LowerCorner", bboxNode, XPathConstants.NODE );
                double[] lowerCoords = asDoublePair( bboxNode, cornerCoordNode.getNodeName(), xPath );

                cornerCoordNode = (Node) xPath.evaluate( "//ows:UpperCorner", bboxNode, XPathConstants.NODE );
                double[] upperCoords = asDoublePair( bboxNode, cornerCoordNode.getNodeName(), xPath );

                String crsStr = ( (String) xPath.evaluate( "@crs", bboxNode, XPathConstants.STRING ) ).toUpperCase();

                if ( presumedWGS84 ) {
                    crsID = "CRS:84";
                    if ( crsStr.contains( "EPSG:" ) ) {
                        crsID = "EPSG:4326";
                    }
                } else {
                    // crs="urn:ogc:def:crs:EPSG::3857"
                    if ( crsStr.contains( "EPSG:" ) ) {
                        int indx = crsStr.indexOf( "EPSG:" );
                        crsID = crsStr.substring( indx ).replace( "::", ":" );
                    } else if ( crsStr.contains( "CRS:" ) ) {
                        int indx = crsStr.indexOf( "CRS:" );
                        crsID = crsStr.substring( indx ).replace( "::", ":" );
                    }
                    assertFalse( crsID.equals( "" ), "Unknown CRS: " + crsStr );
                }
                bbox = new BoundingBox( crsID, lowerCoords[0], lowerCoords[1], upperCoords[0], upperCoords[1] );
            }
            assertTrue( bbox != null, String.format( "Could not parse %s Bounding Box", crsID ) );

            return bbox;
        } catch ( XPathExpressionException xpe ) {
            throw new RuntimeException(
                                        "Error evaluating XPath expression against capabilities doc while parsing geographic BBOX of layer. ",
                                        xpe );
        }
    }

    private static LayerInfo parseLayerInfo( XPath xPath, Node layerNode )
                            throws XPathExpressionException, ParseException {
        String layerName = parseNodeElementName( xPath, layerNode );
        List<BoundingBox> bboxes = parseBoundingBoxes( xPath, layerNode );
        BoundingBox wgs84BBox = parseWGS84BoundingBox( xPath, layerNode );
        return new LayerInfo( layerName, bboxes, wgs84BBox );
    }

    private static List<BoundingBox> parseBoundingBoxes( XPath xPath, Node layerNode )
                            throws XPathExpressionException {
        Map<String, BoundingBox> bboxes = new HashMap<>();
        String bboxesExpr = "ancestor-or-self::wmts:Layer/ows:BoundingBox";
        NodeList bboxNodes = getNodeElements( xPath, layerNode, bboxesExpr );
        for ( int bboxNodeIndex = 0; bboxNodeIndex < bboxNodes.getLength(); bboxNodeIndex++ ) {
            Node bboxNode = bboxNodes.item( bboxNodeIndex );
            BoundingBox bbox = parseBoundingBox( bboxNode, false );
            bboxes.put( bbox.getCrs(), bbox );
        }
        return new ArrayList<>( bboxes.values() );
    }

    private static BoundingBox parseWGS84BoundingBox( XPath xPath, Node layerNode )
                            throws XPathExpressionException {
        String bboxExpr = "ancestor-or-self::wmts:Layer/ows:WGS84BoundingBox";
        Node bboxNode = (Node) xPath.evaluate( bboxExpr, layerNode, XPathConstants.NODE );

        return parseBoundingBox( bboxNode, true );
    }

    private static double[] asDoublePair( Node node, String xPathExpr, XPath xPath )
                            throws XPathExpressionException {
        String content = (String) xPath.evaluate( xPathExpr, node, XPathConstants.STRING );

        int indx = content.indexOf( ' ' );
        if ( indx < 0 )
            indx = content.indexOf( ',' );

        String content0 = (String) content.substring( 0, indx );
        String content1 = (String) content.substring( indx + 1 );

        double[] pair = new double[2];
        pair[0] = Double.parseDouble( content0 );
        pair[1] = Double.parseDouble( content1 );

        return pair;
    }

    private static boolean isOperationBindingSupported( Document wmtsMetadata, String opName, ProtocolBinding binding ) {
        String exprTemplate = "count(/wmts:Capabilities/ows:OperationsMetadata/ows:Operation[@name='%s']/ows:DCP/ows:HTTP/ows:%s)";
        String xPathExpr = String.format( exprTemplate, opName, binding.getElementName() );
        try {
            XPath xPath = createXPath();
            Double bindings = (Double) xPath.evaluate( xPathExpr, wmtsMetadata, XPathConstants.NUMBER );
            if ( bindings > 0 ) {
                return true;
            }
        } catch ( XPathExpressionException xpe ) {
            throw new RuntimeException( "Error evaluating XPath expression against capabilities doc. ", xpe );
        }
        return false;
    }

    private static URI createEndpoint( String href ) {
        if ( href == null || href.isEmpty() )
            return null;
        URI endpoint = URI.create( href );
        if ( null != endpoint.getQuery() ) {
            String uri = endpoint.toString();
            endpoint = URI.create( uri.substring( 0, uri.indexOf( '?' ) ) );
        }
        return endpoint;
    }

    private static XPath createXPath() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext( NS_BINDINGS );
        return xPath;
    }

}
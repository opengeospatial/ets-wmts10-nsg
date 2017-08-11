package ets.wmts10.testsuite.gettile;

import static org.testng.Assert.assertNotNull;

import java.awt.image.BufferedImage;

//import static ets.wmts10.core.util.request.WmtsRequestBuilder.getSupportedTransparentFormat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.joda.time.DateTime;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.jersey.api.client.ClientResponse;

import de.latlon.ets.core.util.TestSuiteLogger;

import javax.imageio.ImageIO;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

//import org.apache.tika.mime.MimeType;
//import org.apache.tika.mime.MimeTypeException;
//import org.apache.tika.mime.MimeTypes;

import ets.wmts10.core.util.ServiceMetadataUtils;
import ets.wmts10.core.util.WMTS_SOAPcontainer;
import ets.wmts10.core.util.request.WmtsKvpRequestBuilder;

import ets.wmts10.testsuite.AbstractBaseGetFixture;
import sun.misc.BASE64Decoder;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a> (original)
 * @author Jim Beatty (modified/fixed May/Jun/Jul-2017 for WMS and/or WMTS)
 */
@SuppressWarnings("restriction")
public abstract class AbstractBaseGetTileFixture extends AbstractBaseGetFixture
{
	private final String MIME_FILENAME = "mime.types";
	
    private final String SUBDIRECTORY = "GetTileTests";
    
    private final String DISCRIMINATOR = DateTime.now().toString("yyyyMMddHHmm");
    
    private Path imageDirectory;
    
    // ---

    /**
     * Builds a {WmtsKvpRequest} representing a GetTile request.
     * @throws XPathExpressionException in case bad XPath
     */
    @BeforeClass
    public void buildGetTileRequest() 
    		throws XPathExpressionException 
    {
		this.reqEntity = WmtsKvpRequestBuilder.buildGetTileRequest(wmtsCapabilities, layerInfo);
    }

    // ---

    @BeforeClass
    public void setResultDirectory(ITestContext testContext)
    {
        String outputDirectory = retrieveSessionDir(testContext);
        TestSuiteLogger.log(Level.INFO, "Directory to store GetTile responses: " + outputDirectory);
        try
        {
            Path resultDir = Paths.get(outputDirectory);
            imageDirectory = createDirectory(resultDir, SUBDIRECTORY+"_" + DISCRIMINATOR); // --- create a unique directory name
        } 
        catch (IOException e) 
        {
            TestSuiteLogger.log(Level.WARNING, "Could not create directory for GetTile response.", e);
        }
    }

    // ---
    
    @Test
    public void verifyGetTileSupported() 
    {
    	Node getTileEntry = null;
    	try
    	{
    		getTileEntry = (Node)ServiceMetadataUtils.getNode(wmtsCapabilities, "//ows:OperationsMetadata/ows:Operation[@name = 'GetTile']");
		} 
    	catch (XPathExpressionException e) 
    	{
		}
    	assertNotNull( getTileEntry, "GetTile is not supported by this WMTS" );
    }
    // --- -------
    
    /**
     * If an image format supporting transparency is not supported by the WMTS a
     * {@link SkipException} is thrown.
     * 
     * @return image format supporting transparency, never <code>null</code>
     */
    /*--
    protected String findRequiredImageFormatWithTransparencySupport()
    {
        String imageFormat = getSupportedTransparentFormat(wmtsCapabilities, WMTS_Constants.GET_TILE);
        if (imageFormat == null)
            throw new SkipException("WMTS does not support an image format supporting transparency.");
        return imageFormat;
    }
    --*/

    /**
     * Stores the image in a the output directory of the testsuite:
     * testSUiteOutputDirectory/testGroup/testName.extension
     * 
     * @param rsp
     *            containing the image, rsp.getEntityInputStream() is used to
     *            retrieve the content as stream, never <code>null</code>
     * @param testGroup
     *            name of the test group (will be the name of the directory to
     *            create), never <code>null</code>
     * @param testName
     *            name of the test (will be the name of the file to create),
     *            never <code>null</code>
     * @param requestFormat
     *            the mime type of the image, never <code>null</code>
     */
    protected void storeResponseImage(ClientResponse rsp, String testGroup, String testName, String requestFormat)
    {
        if (imageDirectory == null)
        {
            TestSuiteLogger.log(Level.WARNING,
                    "Directory to store GetTile responses is not set. GetTile response is not written!");
            return;
        }
        writeIntoFile(rsp, testGroup, testName, requestFormat);
    }

    // ---
    
    protected void storeSoapResponseImage(SOAPMessage soapResponse, String testGroup, String testName, String requestFormat)
    {
        if (imageDirectory == null)
        {
            TestSuiteLogger.log(Level.WARNING,
                    "Directory to store GetTile responses is not set. GetTile response is not written!");
            return;
        }
        writeIntoFile(soapResponse, testGroup, testName, requestFormat);
    }

    // --- -------
    
    // ---
    
    private void writeIntoFile(ClientResponse rsp, String testGroup, String testName, String requestFormat) 
    {
		try 
        {
			Path testClassDirectory = createDirectory(imageDirectory, testGroup);
			InputStream imageStream = rsp.getEntityInputStream();
            
            String fileExtension = detectFileExtension(requestFormat);
            if (( fileExtension != null ) && ( !fileExtension.startsWith(".") ))
            {
            	fileExtension = "." + fileExtension;
            }
			String fileName = testName + fileExtension;
			Path imageFile = testClassDirectory.resolve(fileName);
			Integer indx = -1;
			while (Files.exists(imageFile, java.nio.file.LinkOption.NOFOLLOW_LINKS))
			{
				fileName = testName + (++indx).toString() + "." + fileExtension;
				imageFile = testClassDirectory.resolve(fileName);
			}					
			
			Files.copy(imageStream, imageFile);
        } 
        catch (IOException ioe)
        {
			TestSuiteLogger.log(Level.WARNING, "IO:  Writing the GetTile response into file failed.", ioe);
        }
    }

   // ---
    
	private void writeIntoFile(SOAPMessage soapResponse, String testGroup, String testName, String requestFormat) 
    {
		try 
        {
			Path testClassDirectory = createDirectory(imageDirectory, testGroup);
            
            String fileExtension = detectFileExtension(requestFormat);
            if (( fileExtension != null ) && ( fileExtension.startsWith(".") ))
            {
            	fileExtension = fileExtension.substring(1);
            }

			String fileName = testName + "." + fileExtension;
			Path imageFile = testClassDirectory.resolve(fileName);
			Integer indx = -1;
			while (Files.exists(imageFile, java.nio.file.LinkOption.NOFOLLOW_LINKS))
			{
				fileName = testName + (++indx).toString() + "." + fileExtension;
				imageFile = testClassDirectory.resolve(fileName);
			}					
			
			Document soapDocument = WMTS_SOAPcontainer.makeResponseDocument(soapResponse);

			//String formatStr = (String)createXPath().evaluate("//wmts:BinaryPayload/wmts:Format", soapDocument,XPathConstants.STRING);						
			//String imageString = (String)createXPath().evaluate("//wmts:BinaryPayload/wmts:BinaryContent", soapDocument,XPathConstants.STRING);
			String imageString = (String)ServiceMetadataUtils.getNodeText(soapDocument, "//wmts:BinaryPayload/wmts:BinaryContent");

			BufferedImage bufferedImage = null;
		    byte[] imageByte;

		    BASE64Decoder decoder = new BASE64Decoder();
		    imageByte = decoder.decodeBuffer(imageString);
		    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
		    bufferedImage = ImageIO.read(bis);
		    bis.close();

		    OutputStream imageOutputFile = Files.newOutputStream(imageFile);
		    ImageIO.write(bufferedImage, fileExtension, imageOutputFile);
		    imageOutputFile.close();
        } 
        catch (IOException ioe)
        {
        	System.out.println(ioe.getMessage());
        	TestSuiteLogger.log(Level.WARNING, "IO:  Writing the GetTile response into file failed.", ioe);
			//ioe.printStackTrace();
        }
		catch (XPathExpressionException xpe)//| XPathFactoryConfigurationException xpe)
		{	
			System.out.println(xpe.getMessage());
			//assertTrue(false, "Saving SOAP content image failed:  "+ e1.getMessage());
			TestSuiteLogger.log(Level.WARNING, "SOAP converted document contains an error (no or corrupt BinaryContent)", xpe);
			//xpe.printStackTrace();
        }
    }
    // --- 
    
    private String detectFileExtension(String requestFormat) //throws MimeTypeException
    {
    	String extension = null;
        try
        {
        	BufferedReader br = new BufferedReader( new InputStreamReader( this.getClass().getResourceAsStream( MIME_FILENAME ), "UTF-8" ) ) ; 
            String mimeLine = null;
            
            do
            {
            	mimeLine = br.readLine();
            	 
            	 if (( mimeLine != null) && ( mimeLine.indexOf(':') > 0 ))
            	 {
            		 int indx = mimeLine.indexOf(':');
            		 String mime = mimeLine.substring(0, indx);
            		 String m_ext = mimeLine.substring(indx+1);
            		 
            		 if ( mime.equalsIgnoreCase(requestFormat) )
            		 {
            			 extension = m_ext;
            		 }
            	 }
            }
            while ((mimeLine != null) && ( extension == null ));    
            br.close(); 
        } 
        catch (IOException e)
        {
			TestSuiteLogger.log(Level.WARNING, "Cannot find MIME Types.", e);
        }

        return extension;
    }
    
    // ---
    
    private Path createDirectory(Path parent, String child) throws IOException 
    {
    	Path testClassDirectory = parent.resolve(child);
    	Files.createDirectories(testClassDirectory);
    	return testClassDirectory;
    }

    // ---

    /**
     * Gets the location of the output directory from the test run context.
     * 
     * @param testContext
     *            Information about a test run.
     * @return A String that identifies the directory containing test run
     *         results.
     */
    
    private String retrieveSessionDir(ITestContext testContext)
    {
        File outputDir = new File(testContext.getOutputDirectory());
        return outputDir.getPath();
    }
    
    // ---
    
    /*---   */
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
  /* ---*/
		

	// --- 
/*---	   
	private XPath createXPath()
               throws XPathFactoryConfigurationException
	{
		XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext( NS_BINDINGS );
		return xpath;
	}
 ---*/
}
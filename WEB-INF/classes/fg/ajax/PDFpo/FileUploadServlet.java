package fg.ajax.PDFpo;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fg.ajax.PDFpo.FileUploadListener.FileUploadStats;
import fg.ajax.PDFpo.FileUploadListener.FileUploadStatus;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import fg.ajax.PDFpo.MonitoredDiskFileItemFactory;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.List;
import java.util.Iterator;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import javax.imageio.metadata.IIOMetadata;
import java.util.ArrayList;

import java.io.IOException;
import java.util.HashMap;

import java.io.FileInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.Overlay;

public class FileUploadServlet extends HttpServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }
	
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        long maxSizeMB =            100; //Max file size (MegaBytes)
        long maxSize =              maxSizeMB * 1024 * 1024; //Max file size (bytes)
		String baseFolder =         getServletContext().getRealPath("/") + "temp/PDFpo/";
        String m =                 "";

        FileUploadListener listener = new FileUploadListener(request.getContentLength());
        request.getSession().setAttribute("FILE_UPLOAD_STATS", listener.getFileUploadStats());

        listener.getFileUploadStats().setCurrentStatus(FileUploadStatus.NONE);
        listener.getFileUploadStats().setDirectory("");
		listener.getFileUploadStats().setN(0);
        listener.getFileUploadStats().setFilename("");
        listener.getFileUploadStats().setErrorMsg("");
        listener.getFileUploadStats().setBytesRead(0);
        listener.getFileUploadStats().setTotalSize(0);

        if (request.getContentLength() > maxSize)
        {
            listener.getFileUploadStats().setErrorMsg("Error: file too large (max : " + maxSizeMB + " MB).");
        }
        else
        {
            try
            {
				int nFiles = 0;
				int retTotal = 0;
				boolean error = false;
				ArrayList<String> listFiles = new ArrayList<String>();
										
                DiskFileItemFactory factory = new MonitoredDiskFileItemFactory(listener);

                //factory.setRepository(new File(saveFilePath));
                ServletFileUpload upload = new ServletFileUpload(factory);

                // maximum size before a FileUploadException will be thrown
                upload.setSizeMax(maxSize);
                // maximum size that will be stored in memory
                factory.setSizeThreshold(4096);

                List items = upload.parseRequest(request);
                //if this process of writing to the file system could take a while,
                //you'd want to add another listener and possibly show progress of this as well.

                for (Iterator i = items.iterator(); i.hasNext();)
                {
                    FileItem fileItem = (FileItem) i.next();
                    if (fileItem.isFormField())
                    {
                        if (fileItem.getFieldName().equals("m")) m = fileItem.getString();
                    }
                    else
                    {
                        //File fullFile  = new File(fileItem.getName());
                        if ((m.compareTo("") == 0) )
                        {
                            listener.getFileUploadStats().setErrorMsg("Error: Invalid parameters.");
                        }
                        else
                        {
							String modelFilename = getServletContext().getRealPath("/") + "models/PDF_overlay_" + m + ".pdf";
							if(!(new File(modelFilename).exists()))
							{
								listener.getFileUploadStats().setErrorMsg("Fatal error: model file [PDF_overlay_" + m + ".pdf] does not exist");
							}
							else
							{
								// ============ documento ============
								String fileName = fileItem.getName();
								
								if ((fileName.length() > 3) && (fileName.substring(fileName.length() - 4).equalsIgnoreCase(".pdf")))
								{
									if (fileItem.getSize() > 0)
									{
										nFiles++;
										fileName = FilenameUtils.getName(fileName);
										
										if(!(new File(baseFolder).exists())) (new File(baseFolder)).mkdir();
										if(!(new File(baseFolder + request.getSession().getId()).exists())) (new File(baseFolder + request.getSession().getId())).mkdir();
										if(!(new File(baseFolder + request.getSession().getId() + "/out").exists())) (new File(baseFolder + request.getSession().getId() + "/out")).mkdir();
																			

										File file = new File(baseFolder + request.getSession().getId() , fileName);
										fileItem.write(file);

										if (file.isFile()) 
										{
											String finalFilename = fileName.substring(0, fileName.length() - 4) + "_po.pdf";
											if (!m.equals("")) 
											{
												if (!overlayPDF(baseFolder + request.getSession().getId(), fileName, modelFilename, finalFilename)) 
													error = true;
												else
													retTotal++;
											}
											if (!error)
											{
												Files.copy(	new File(baseFolder + request.getSession().getId() + "/" + finalFilename).toPath(),
															new File(baseFolder + request.getSession().getId() + "/out/" + finalFilename.substring(0, finalFilename.length() - 7) + ".pdf").toPath(), 
															StandardCopyOption.REPLACE_EXISTING);
												listFiles.add(finalFilename.substring(0, finalFilename.length() - 7) + ".pdf");
											}
										}
										else
										{
											listener.getFileUploadStats().setErrorMsg("Fatal error: temp file not saved");
										}
									}
									else
									{
										listener.getFileUploadStats().setErrorMsg("Error: File with 0 bytes!");
									}
								}
								else
								{
									listener.getFileUploadStats().setErrorMsg("Error: not a .PDF file!");
								}
							}
                        }
                        
                    }
                }
				
				if (error)
				{
					listener.getFileUploadStats().setErrorMsg("Fatal error proccessing PDF" + (nFiles <= 1 ? "" : "s"));
					
				}
				else
				{
					listener.getFileUploadStats().setDirectory(request.getSession().getId() + "/out");
					listener.getFileUploadStats().setErrorMsg("");
					listener.getFileUploadStats().setN(retTotal);
					listener.getFileUploadStats().setCurrentStatus(FileUploadStatus.PROCESSED);
					if (listFiles.size() == 0)
					{
						listener.getFileUploadStats().setFilename("error");
						listener.getFileUploadStats().setErrorMsg("No files processed!");
					}
					else if (listFiles.size() == 1)
					{
						listener.getFileUploadStats().setFilename(listFiles.get(0));
					}
					else
					{
						try 
						{
							FileOutputStream fos = new FileOutputStream(baseFolder + request.getSession().getId() + "/out/files.zip");
							ZipOutputStream zos = new ZipOutputStream(fos);
							for (String f : listFiles)
							{
								addToZipFile(baseFolder + request.getSession().getId() + "/out/" + f, zos);
							}
							zos.close();
							fos.close();

						} 
						catch (FileNotFoundException e) 
						{
							listener.getFileUploadStats().setErrorMsg("Fatal error creating ZIP file (FileNotFoundException)");
							//e.printStackTrace();
						} 
						catch (IOException e) 
						{
							listener.getFileUploadStats().setErrorMsg("Fatal error creating ZIP file (IOException)");
							//e.printStackTrace();
						}						
						listener.getFileUploadStats().setFilename("files.zip");
					}
				}				
			}
            catch (Exception e)
            {
                listener.getFileUploadStats().setErrorMsg("Fatal error (fn): " + e.getMessage() + ".");
                e.printStackTrace();
            }
        }
        //response.getOutputStream().print("<html><head><script type='text/javascript'>window.parent.killUpdate_po('" + listener.getFileUploadStats().getErrorMsg() + "');</script></body></html>");
    }
    // =========================================================================
	boolean overlayPDF(String workingDir, String filenameIn, String filenameOverlay, String filenameOut)
	{
		try 
		{
			PDDocument realDoc = PDDocument.load(workingDir + "/" + filenameIn); 

			//for all the pages, you can add overlay guide, indicating watermark the original pages with the watermark document.
			HashMap<Integer, String> overlayGuide = new HashMap<Integer, String>();
			for(int i = 0; i < realDoc.getPageCount(); i++)
			{
				overlayGuide.put(i + 1, filenameOverlay);
			}
			Overlay overlay = new Overlay();
			overlay.setInputPDF(realDoc);
			overlay.setOutputFile(workingDir + "/" + filenameOut);
			overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
			overlay.overlay(overlayGuide, false);

			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	// =========================================================================
	public static void addToZipFile(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException 
	{
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(file.getName());
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) 
		{
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}
}

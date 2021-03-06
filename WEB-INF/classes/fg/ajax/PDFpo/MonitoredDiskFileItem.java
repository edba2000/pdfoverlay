package fg.ajax.PDFpo;

import org.apache.commons.fileupload.disk.DiskFileItem;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;

import fg.ajax.PDFpo.FileUploadListener;

public class MonitoredDiskFileItem extends DiskFileItem
{
  private MonitoredOutputStream mos = null;
  private OutputStreamListener listener;

  public MonitoredDiskFileItem(String fieldName, String contentType, boolean isFormField, String fileName, int sizeThreshold, File repository, OutputStreamListener listener)
  {
    super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
    this.listener = listener;
  }

  public OutputStream getOutputStream() throws IOException
  {
    if (mos == null)
    {
      mos = new MonitoredOutputStream(super.getOutputStream(), listener);
	  ((FileUploadListener)listener).getFileUploadStats().setBytesRead(0);
	  
    }
    return mos;
  }
}

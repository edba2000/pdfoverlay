package fg.ajax.PDFpo;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fg.ajax.PDFpo.FileUploadListener.FileUploadStats;
import fg.ajax.PDFpo.FileUploadListener.FileUploadStatus;


public class AjaxUploadStatusServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    public String scanURL(String input)
    {
            input = input.replaceAll("&", "%26");
            input = input.replaceAll("'", "%60");
            input = input.replaceAll(":", "%3A");
            input = input.replaceAll("<", "%3C");
            input = input.replaceAll(">", "%3E");
            input = input.replaceAll(";", "%3B");
            return input;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		response.setHeader("Cache-Control", "post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");

		response.setHeader("Expires", "-1");			

		response.setHeader("Content-Type", "text/xml");
		response.setContentType("text/xml");
			
        FileUploadStats stats = (FileUploadStats) request.getSession().getAttribute("FILE_UPLOAD_STATS");
        if (stats != null)
        {
            long bytesProcessed = stats.getBytesRead();
            long sizeTotal = stats.getTotalSize();
            DecimalFormat decFormatter = new DecimalFormat(".00");
            long elapsedTimeInMilliseconds = stats.getElapsedTimeInMilliseconds();
            double bytesPerMillisecond = bytesProcessed / (elapsedTimeInMilliseconds + 0.00001);
            long estimatedMillisecondsLeft = (long)((sizeTotal - bytesProcessed) / (bytesPerMillisecond + 0.00001));
            String timeLeft = null;

            if ((estimatedMillisecondsLeft / 3600) > 24)
            {
                timeLeft = (long)(estimatedMillisecondsLeft / 3600) + " hours";
            } 
            else
            {
                Calendar c = new GregorianCalendar();
                long ad =  estimatedMillisecondsLeft - (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));
                timeLeft = new SimpleDateFormat("HH:mm:ss").format(ad);
            }

            PrintWriter out = response.getWriter();

            out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.print("<response>\n");
            out.print("\t<bytesProcessed>" + bytesProcessed + "</bytesProcessed>\n");
            out.print("\t<sizeTotal>" + sizeTotal + "</sizeTotal>\n");
            out.print("\t<speed>" +  decFormatter.format(bytesPerMillisecond) + " kb/sec</speed>\n");
            out.print("\t<timeLeft>" + timeLeft + "</timeLeft>\n");
            out.print("\t<status>" + stats.getCurrentStatus() + "</status>\n");
            out.print("\t<errormsg>" + stats.getErrorMsg() + "</errormsg>\n");
            out.print("\t<directory>" + stats.getDirectory() + "</directory>\n");
			out.print("\t<n>" + stats.getN() + "</n>\n");
            out.print("\t<filename>" + scanURL(stats.getFilename()) + "</filename>\n");
            out.print("</response>\n");

            out.flush();
			
			if (stats.getCurrentStatus() == FileUploadStatus.PROCESSED)
			{
				stats.setCurrentStatus(FileUploadStatus.NONE);
				stats.setDirectory("");
				stats.setN(0);
				stats.setFilename("");
				stats.setErrorMsg("");
				stats.setBytesRead(0);
				stats.setTotalSize(0);
			}
        } 
        else
        {
            //System.out.println("stats null");
        }
    }
}
package fg.ajax.PDFpo;

import fg.ajax.PDFpo.OutputStreamListener;

public class FileUploadListener implements OutputStreamListener
{
    private FileUploadStats fileUploadStats = new FileUploadStats();

    public FileUploadListener(long totalSize)
    {
        fileUploadStats.setTotalSize(totalSize);
        fileUploadStats.setBytesRead(0);
    }

    public void start()
    {
        fileUploadStats.setCurrentStatus(FileUploadStatus.START);
    }

    public void bytesRead(int byteCount)
    {
        fileUploadStats.incrementBytesRead(byteCount);
        fileUploadStats.setCurrentStatus(FileUploadStatus.READING);
    }

    public void error(String s)
    {
        fileUploadStats.setErrorMsg(s);
        fileUploadStats.setCurrentStatus(FileUploadStatus.ERROR);
    }

    public void done()
    {
        fileUploadStats.setBytesRead(fileUploadStats.getTotalSize());
        fileUploadStats.setCurrentStatus(FileUploadStatus.DONE);
    }

    public FileUploadStats getFileUploadStats()
    {
        return fileUploadStats;
    }
    //==========================================================================
    public static class FileUploadStats
    {

        private long totalSize = 0;
        private long bytesRead = 0;
        private long startTime = System.currentTimeMillis();
        private FileUploadStatus currentStatus = FileUploadStatus.NONE;
        private String errorMsg = "undef";
        private String directory;
        private String filename;
		private int n;

        public long getTotalSize()
        {
            return totalSize;
        }

        public void setTotalSize(long totalSize)
        {
            this.totalSize = totalSize;
        }

        public void setDirectory(String directory)
        {
            this.directory = directory;
        }

        public void setFilename(String filename)
        {
            this.filename = filename;
        }
		
		public void setN(int n)
        {
            this.n = n;
        }
		
        public int getN()
        {
            return n;
        }		

        public String getDirectory()
        {
            return directory;
        }

        public String getFilename()
        {
            return filename;
        }

        public long getBytesRead()
        {
            return bytesRead;
        }

        public long getElapsedTimeInMilliseconds()
        {
            return (System.currentTimeMillis() - startTime);
        }

        public FileUploadStatus getCurrentStatus()
        {
            return currentStatus;
        }

        public void setErrorMsg(String s)
        {
            this.errorMsg = s;
            this.currentStatus = FileUploadStatus.ERROR;
        }

        public String getErrorMsg()
        {
            return this.errorMsg;
        }

        public void setCurrentStatus(FileUploadStatus currentStatus)
        {
            this.currentStatus = currentStatus;
        }

        public void setBytesRead(long b)
        {
            this.bytesRead = b;
        }

        public void incrementBytesRead(int byteCount)
        {
            this.bytesRead += byteCount;
        }
    }

    enum FileUploadStatus
    {
        START("start"),
        NONE("none"),
        READING("reading"),
        ERROR("error"),
        DONE("done"),
        PROCESSED("processed");

        private String type;
        FileUploadStatus(String type)
        {
            this.type = type;
        }
        public String getType()
        {
            return type;
        }
    }
}

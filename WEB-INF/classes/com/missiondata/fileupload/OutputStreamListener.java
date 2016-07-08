/**
 * Orginally from:
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package com.missiondata.fileupload;

public interface OutputStreamListener
{
  public void start();

  public void bytesRead(int bytesRead);

  public void error(String message);

  public void done();
}

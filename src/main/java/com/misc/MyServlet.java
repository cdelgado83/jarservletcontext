package com.misc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @see https://github.com/gwtproject/gwt/blob/master/user/src/com/google/gwt/user/server/rpc/RemoteServiceServlet.java
 */
@SuppressWarnings("serial")
class MyServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String filePath = "/module/afile.rpc";
    ServletContext servletContext = getServletContext();
    // log("getResourcePaths: " + servletContext.getResourcePaths(filePath));
    // log("getRealPath: " + servletContext.getRealPath(filePath));
    try (InputStream is = servletContext.getResourceAsStream(filePath)) {
      if (is == null) {
        log("ERROR: '" + filePath + "' NOT found");
        throw new FileNotFoundException(filePath);
      }
      log("SUCCESS: '" + filePath + "' found");
      copy(is, resp.getOutputStream());
    }
  }

  private void copy(InputStream from, OutputStream to) throws IOException {
    byte[] buf = new byte[4096];
    int n;
    while ((n = from.read(buf)) > 0) {
      to.write(buf, 0, n);
    }
  }

}

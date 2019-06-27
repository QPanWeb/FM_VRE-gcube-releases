package org.gcube.application.perform.service.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class MyAuthException extends WebApplicationException  {
    /**
   *
   */
  private static final long serialVersionUID = 1L;

  public MyAuthException(Throwable cause) {
      super(cause, Status.FORBIDDEN);
  }

}
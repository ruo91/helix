package com.linkedin.helix.webapp.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.linkedin.helix.PropertyType;
import com.linkedin.helix.ZNRecord;
import com.linkedin.helix.manager.zk.ZkClient;
import com.linkedin.helix.tools.ClusterSetup;
import com.linkedin.helix.webapp.RestAdminApplication;

public class ControllerStatusUpdateResource extends Resource
{
  public ControllerStatusUpdateResource(Context context, Request request, Response response)
  {
    super(context, request, response);
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));
  }

  public boolean allowGet()
  {
    return true;
  }

  public boolean allowPost()
  {
    return false;
  }

  public boolean allowPut()
  {
    return false;
  }

  public boolean allowDelete()
  {
    return false;
  }

  public Representation represent(Variant variant)
  {
    StringRepresentation presentation = null;
    try
    {
      String zkServer = (String) getContext().getAttributes().get(RestAdminApplication.ZKSERVERADDRESS);
      String clusterName = (String) getRequest().getAttributes().get("clusterName");
      String messageType = (String) getRequest().getAttributes().get("MessageType");
      String messageId = (String) getRequest().getAttributes().get("MessageId");
      
      presentation = getControllerStatusUpdateRepresentation(zkServer, clusterName, messageType, messageId);
    }
    catch (Exception e)
    {
      String error = ClusterRepresentationUtil.getErrorAsJsonStringFromException(e);
      presentation = new StringRepresentation(error, MediaType.APPLICATION_JSON);
      
      e.printStackTrace();
    }
    return presentation;
  }

  StringRepresentation getControllerStatusUpdateRepresentation(String zkServerAddress, String clusterName, String messageType, String messageId) throws JsonGenerationException, JsonMappingException, IOException
  {
    String message = 
        ClusterRepresentationUtil.getPropertyAsString(zkServerAddress, clusterName, PropertyType.STATUSUPDATES_CONTROLLER, MediaType.APPLICATION_JSON, messageType, messageId);
    StringRepresentation representation = new StringRepresentation(message, MediaType.APPLICATION_JSON);
    return representation;
  }
}
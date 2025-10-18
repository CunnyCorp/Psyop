package monster.psyop.client.impl.modules.misc.rpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RichPresence {
  private String name;

  private String details;
  
  private String state;
  
  private Assets assets;
  
  private Timestamps timestamps;
  
  public void setName(String name) {
    this.name = name;
  }

  public void setDetails(String details) {
    this.details = details;
  }
  
  public void setState(String state) {
    this.state = state;
  }
  
  public void setLargeImage(String key, String text) {
    if (this.assets == null)
      this.assets = new Assets(); 
    this.assets.large_image = key;
    this.assets.large_text = text;
  }
  
  public void setSmallImage(String key, String text) {
    if (this.assets == null)
      this.assets = new Assets(); 
    this.assets.small_image = key;
    this.assets.small_text = text;
  }
  
  public void setStart(long time) {
    if (this.timestamps == null)
      this.timestamps = new Timestamps(); 
    this.timestamps.start = Long.valueOf(time);
  }
  
  public void setEnd(long time) {
    if (this.timestamps == null)
      this.timestamps = new Timestamps(); 
    this.timestamps.end = Long.valueOf(time);
  }
  
  public JsonObject toJson() {
    JsonObject o = new JsonObject();
    if (this.name != null)
      o.addProperty("name", this.name);
    if (this.details != null)
      o.addProperty("details", this.details); 
    if (this.state != null)
      o.addProperty("state", this.state); 
    if (this.assets != null) {
      JsonObject a = new JsonObject();
      if (this.assets.large_image != null)
        a.addProperty("large_image", this.assets.large_image); 
      if (this.assets.large_text != null)
        a.addProperty("large_text", this.assets.large_text); 
      if (this.assets.small_image != null)
        a.addProperty("small_image", this.assets.small_image); 
      if (this.assets.small_text != null)
        a.addProperty("small_text", this.assets.small_text); 
      o.add("assets", (JsonElement)a);
    } 
    if (this.timestamps != null) {
      JsonObject t = new JsonObject();
      if (this.timestamps.start != null)
        t.addProperty("start", this.timestamps.start); 
      if (this.timestamps.end != null)
        t.addProperty("end", this.timestamps.end); 
      o.add("timestamps", (JsonElement)t);
    } 
    return o;
  }
  
  public static class Assets {
    public String large_image;
    
    public String large_text;
    
    public String small_image;
    
    public String small_text;
  }
  
  public static class Timestamps {
    public Long start;
    
    public Long end;
  }
}
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import processing.core.PImage;

public class ResourceManager {
  List<PImage>             image;
  HashMap<String, Integer> imageId;
  List<Mesh3D>             mesh;
  HashMap<String, Integer> meshId;
  
  ResourceManager() {
    this.image = new ArrayList<>();
    this.imageId = new HashMap<>();
    this.mesh = new ArrayList<>();
    this.meshId = new HashMap<>();
  }
  
  int addImage(String path) {
    Integer id = imageId.get(path);
    
    if (id == null) {
      try {
        InputStream stream = new FileInputStream(path);
        BufferedImage bimg = ImageIO.read(stream);
        PImage pimg = new PImage(bimg.getWidth(), bimg.getHeight());
        bimg.getRGB(0, 0, pimg.width, pimg.height, pimg.pixels, 0, pimg.width);
        id = image.size();
        image.add(pimg);
        imageId.put(path, id);
      } catch (IOException e) {
        e.printStackTrace();
        id = -1;
      }
    }
    
    return id;
  }
  
  PImage getImage(String name) {
    return image.get(imageId.get(name));
  }
  
  PImage getImage(int id) {
    return image.get(id);
  }
  
  int addMesh(String path) {
    Integer id = meshId.get(path);
    
    if (id == null) {
      Mesh3D m = null;
      if (path.endsWith(".obj"))
        m = Mesh3D.readFromOBJ(path);
      else if (path.endsWith(".lwo"))
        m = Mesh3D.readFromLWO(path);
      if (m == null)
        return -1;
   
      m.load(this);
      id = mesh.size();
      mesh.add(m);
      meshId.put(path, id);
    }
    
    return id;
  }
  
  Mesh3D getMesh(String name) {
    return mesh.get(meshId.get(name));
  }
  
  Mesh3D getMesh(int id) {
    return mesh.get(id);
  }
}
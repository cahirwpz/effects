import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class IffFile {
  String id;
  ArrayList<Chunk> chunks;
  
  IffFile(String id) {
    this.id = id;
    this.chunks = new ArrayList<>();
  }

  public class Chunk {
    String id;
    ByteBuffer data;
    
    Chunk(String id, ByteBuffer data) {
      this.id = id;
      this.data = data;
    }
    
    int size() {
      return data.capacity();
    }
    
    boolean hasRemaining() {
      return data.hasRemaining();
    }
    
    String getId() {
      byte[] buf = new byte[4];
      data.get(buf);
      return new String(buf);
    }
    
    int getShort() {
      return data.getShort();
    }
    
    float getFloat() {
      return data.getFloat();
    }
    
    String getString() {
      int start = data.position();
      while (data.get() != 0);
      int end = data.position();
      
      byte[] b = new byte[end - start];
      data.position(start);
      data.get(b);
      
      if ((data.position() & 1) != 0)
        data.get();
      
      return new String(b);
    }
    
    Vector3D getVector3D() {
      return new Vector3D(data.getFloat(), data.getFloat(), data.getFloat()); 
    }

    ArrayList<Chunk> parseMiniChunks() {
      ArrayList<Chunk> miniChunks = new ArrayList<>();
      while (hasRemaining()) {
        String id = getId(); 
        byte[] buf = new byte[getShort()];
        data.get(buf);
        miniChunks.add(new Chunk(id, ByteBuffer.wrap(buf)));
      }
      return miniChunks;
    }
    
    public boolean isType(String id) {
      return this.id.equals(id);
    }
  };
  
  private static String getId(ByteBuffer bb) {
    byte[] buf = new byte[4];
    bb.get(buf);
    return new String(buf);
  }
  
  static IffFile read(String path) {
    FileInputStream stream = null;
    IffFile iff = null;
    
    try {
      stream = new FileInputStream(path);
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      
      if (!getId(bb).equals("FORM"))
        throw new IOException();
      
      bb.getInt();
      
      iff = new IffFile(getId(bb));
      
      while (bb.hasRemaining()) {
        String id = getId(bb); 
        byte[] buf = new byte[bb.getInt()];
        bb.get(buf);
        iff.chunks.add(iff.new Chunk(id, ByteBuffer.wrap(buf)));
      }
      
      stream.close();
    } catch(IOException e) {
    }
    
    return iff;
  }
  
  boolean isType(String id) {
    return this.id.equals(id);
  }
}
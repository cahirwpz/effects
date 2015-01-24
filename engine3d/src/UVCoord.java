public class UVCoord {
  float u, v;
  
  UVCoord(float u, float v) {
    this.u = u;
    this.v = v;
  }

  public UVCoord copy() {
    return new UVCoord(u, v);
  }
}
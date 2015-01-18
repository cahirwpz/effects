public class Vector3D {
  public float x, y, z, w;

  Vector3D() { x = 0.0f; y = 0.0f; z = 0.0f; w = 1.0f; }
  
  Vector3D(float x, float y, float z) {
    this.x = x; this.y = y; this.z = z; this.w = 1.0f;
  }

  Vector3D(float x, float y, float z, float w) {
    this.x = x; this.y = y; this.z = z; this.w = w;
  }
  
  Vector3D copy() {
    return new Vector3D(x, y, z, w);
  }

  public String toString() {
    return String.format("<%f, %f, %f>", x, y, z);
  }
  
  static Vector3D add(Vector3D a, Vector3D b) {
    return new Vector3D(a.x + b.x, a.y + b.y, a.z + b.z);
  }
  
  public void add(Vector3D a) {
    x += a.x; y += a.y; z += a.z;
  }

  static Vector3D sub(Vector3D a, Vector3D b) {
    return new Vector3D(a.x - b.x, a.y - b.y, a.z - b.z);
  }

  public void sub(Vector3D a) {
     x -= a.x; y -= a.y; z -= a.z;
  }
  
  static Vector3D cross(Vector3D a, Vector3D b) {
    return new Vector3D(a.y * b.z - b.y * a.z,
                        a.z * b.x - b.z * a.x,
                        a.x * b.y - b.x * a.y);
  }

  static float dot(Vector3D a, Vector3D b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
  }

  float length() {
    return (float)Math.sqrt(x * x + y * y + z * z);
  }
  
  static float dist(Vector3D a, Vector3D b) {
    return sub(a, b).length();
  }

  static Vector3D scale(Vector3D a, float s) {
    return new Vector3D(a.x * s, a.y * s, a.z * s);
  }

  public void scale(float s) {
    x *= s; y *= s; z *= s;
  }

  static Vector3D normalize(Vector3D a, float l) {
    return scale(a, l / a.length());
  }

  public void normalize(float l) {
    scale(l / length());
  }
}

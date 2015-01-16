import java.lang.*;

public class Vector3D {
  public float x, y, z;

  Vector3D() { x = 0.0f; y = 0.0f; z = 0.0f;}
  
  Vector3D(float x, float y, float z) {
    this.x = x; this.y = y; this.z = z;
  }

  public String toString() {
    return String.format("<%f, %f, %f>", x, y, z);
  }
  
  static Vector3D add(Vector3D a, Vector3D b) {
    return new Vector3D(a.x + b.x, a.y + b.y, a.z + b.z);
  }

  static Vector3D sub(Vector3D a, Vector3D b) {
    return new Vector3D(a.x - b.x, a.y - b.y, a.z - b.z);
  }

  static Vector3D cross(Vector3D a, Vector3D b) {
    return new Vector3D(a.y * b.z - b.y * a.z,
                        a.z * b.x - b.z * a.x,
                        a.x * b.y - b.x * a.y);
  }

  static float dot(Vector3D a, Vector3D b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
  }

  static float length(Vector3D a) {
    return (float)Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);
  }

  static float dist(Vector3D a, Vector3D b) {
    return length(sub(a,b));
  }

  static Vector3D scale(Vector3D a, float s) {
    return new Vector3D(a.x * s, a.y * s, a.z * s);
  }

  static Vector3D normalize(Vector3D a, float l) {
    return scale(a, l / length(a));
  }
}

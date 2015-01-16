import java.lang.*;

public class Matrix3D {
  float[][] mx; /* [row][col] */
  
  Matrix3D() {
    mx = new float[4][];
    for (int i = 0; i < 4; i++)
      mx[i] = new float[4];

    reset();
  }

  void reset() {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        mx[i][j] = (i == j) ? 1.0f : 0.0f;
  }

  public String toString() {
    String row[] = new String[4];
    
    for (int i = 0; i < 4; i++)
      row[i] = String.format("%+12f, %+12f, %+12f, %+12f", mx[i][0], mx[i][1], mx[i][2], mx[i][3]);
    
    return String.format("[ %s\n  %s\n  %s\n  %s ]", row[0], row[1], row[2], row[3]);
  }

  static Matrix3D loadScaling(float scaleX, float scaleY, float scaleZ) {
    Matrix3D d = new Matrix3D();

    d.mx[0][0] = scaleX;
    d.mx[1][1] = scaleY;
    d.mx[2][2] = scaleZ;
    
    return d;
  }

  static Matrix3D loadRotation(float angleX, float angleY, float angleZ) {
    angleX = (float)Math.toRadians(angleX);
    angleY = (float)Math.toRadians(angleY);
    angleZ = (float)Math.toRadians(angleZ);

    float sinX = (float)Math.sin(angleX);
    float cosX = (float)Math.cos(angleX);
    float sinY = (float)Math.sin(angleY);
    float cosY = (float)Math.cos(angleY);
    float sinZ = (float)Math.sin(angleZ);
    float cosZ = (float)Math.cos(angleZ);

    Matrix3D d = new Matrix3D();

    d.mx[0][0] = cosY * cosZ;
    d.mx[0][1] = cosY * sinZ;
    d.mx[0][2] = -sinY;
    d.mx[1][0] = sinX * sinY * cosZ - cosX * sinZ;
    d.mx[1][1] = sinX * sinY * sinZ + cosX * cosZ;
    d.mx[1][2] = sinX * cosY;
    d.mx[2][0] = cosX * sinY * cosZ + sinX * sinZ;
    d.mx[2][1] = cosX * sinY * sinZ - sinX * cosZ;
    d.mx[2][2] = cosX * cosY;
    
    return d;
  }
  
  static Matrix3D loadFrustum(float left, float right,
                              float top, float bottom,
                              float near, float far)
  {
    Matrix3D d = new Matrix3D();
    
    assert near > 0 && far > 0;
  
    d.mx[0][0] = 2 * near / (right - left);
    d.mx[0][2] = (right + left) / (right - left);
    d.mx[1][1] = 2 * near / (top - bottom);
    d.mx[1][2] = (top + bottom) / (top - bottom);
    d.mx[2][2] = - (far + near) / (far - near);
    d.mx[2][3] = -2 * far * near / (far - near);
    d.mx[3][2] = -1;
    d.mx[3][3] = 0;
    
    return d;
  }
  
  static Matrix3D loadPerspective(float fovy, float aspect, float near, float far) {
    float fH = (float)Math.tan(Math.toRadians(fovy / 2)) * near;
    float fW = fH * aspect;

    return loadFrustum(-fW, fW, -fH, fH, near, far);
  }
  
  static Matrix3D mult(Matrix3D a, Matrix3D b) {
    Matrix3D d = new Matrix3D();
    
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        d.mx[i][j] = 0.0f;

        for (int k = 0; k < 4; k++)
          d.mx[i][j] += b.mx[i][k] * a.mx[k][j];
      }
    }
    
    return d;
  }
  
  void copy(Matrix3D s) {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        mx[i][j] = s.mx[i][j];
  }
  
  void apply(Matrix3D m) {
    copy(mult(this, m));
  }

  void scale(float scaleX, float scaleY, float scaleZ) {
    apply(loadScaling(scaleX, scaleY, scaleZ));
  }
  
  void rotate(float angleX, float angleY, float angleZ) {
    apply(loadRotation(angleX, angleY, angleZ));
  }

  void translate(float x, float y, float z) {
    mx[0][3] += x;
    mx[1][3] += y;
    mx[2][3] += z;
  }
  
  void perspective(float fovy, float aspect, float near, float far) {
    apply(loadPerspective(fovy, aspect, near, far));
  }

  Vector3D[] transform(Vector3D src[]) {
    Vector3D dst[] = new Vector3D[src.length];
    
    for (int i = 0; i < src.length; i++) {
      Vector3D u = src[i];
      Vector3D v = new Vector3D();
      
      v.x = mx[0][0] * u.x + mx[0][1] * u.y + mx[0][2] * u.z + mx[0][3];
      v.y = mx[1][0] * u.x + mx[1][1] * u.y + mx[1][2] * u.z + mx[1][3];
      v.z = mx[2][0] * u.x + mx[2][1] * u.y + mx[2][2] * u.z + mx[2][3];
      
      float w = mx[3][0] * u.x + mx[3][1] * u.y + mx[3][2] * u.z + mx[3][3];

      //System.out.println(w);

      dst[i] = Vector3D.scale(v, 1.0f / w);
    }
  
    return dst;
  }
};

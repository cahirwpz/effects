public class Color {
  float r, g, b;
  
  Color(float r, float g, float b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  static Color black() {
    return new Color(0.0f, 0.0f, 0.0f);
  }

  static Color white() {
    return new Color(1.0f, 1.0f, 1.0f);
  }
  
  int toInteger() {
    return ((int) (r * 255.0f) << 16)
         | ((int) (g * 255.0f) << 8)
         | (int) (b * 255.0f);
  }
}
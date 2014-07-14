class BentPlane implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);
    
    p.u = 0.1 * y / (0.11 + r * 0.15);
    p.v = 0.1 * x / (0.11 + r * 0.15);
  }
}

class WeirdTunnel implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);
    
    p.u = 0.3 / (r + 0.5 * x);
    p.v = 3.0 * a / PI;
  }
}

class Twist implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = pow(r, 1.0 / 2.0);
    p.v = a / PI + r;
  }
}

class Swirl implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = x * cos(2.0 * r) - y * sin(2.0 * r);
    p.v = y * cos(2.0 * r) + x * sin(2.0 * r);
  }
}

class InnerCycle implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);
    
    p.u = cos(a) / (3.0 * r);
    p.v = sin(a) / (3.0 * r);
  }
}

class FancyEye implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);
    
    p.u = 0.04 * y + 0.06 * cos(a * 3.0) / r;
    p.v = 0.04 * x + 0.06 * sin(a * 3.0) / r;
  }
}

class Some6 implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = 0.5 * a / PI + 0.25 * r;
    p.v = pow(r, 0.25);
  }
}

class Some7 implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = 0.5 * a / PI;
    p.v = sin(5.0 * r);
  }
}

class Some8 implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = 3.0 * a / PI;
    p.v = sin(6.0 * r) + 0.5 * cos(7.0 * a);
  }
}

class Some9 implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = x * log(0.5 * sq(r));
    p.v = y * log(0.5 * sq(r));
  }
}

class Some10 implements UVGenerator {
  void calculate(UVCoord p, float x, float y) {
    float a = atan2(x, y);
    float r = dist(x, y, 0.0, 0.0);

    p.u = 8 * x * sq(1.5 - r);
    p.v = 8 * y * sq(1.5 - r);
  }
}

class MyShape {
  PShape s;
  float x, y;
  float velocity;
  float size;
  float blend;

  void make() {
    s = createShape();
    s.beginShape();
    s.fill(255, random(128, 196));
    s.noStroke();
    s.vertex( -4, -1);
    s.vertex( -4,  1);
    s.vertex(  0,  1);
    s.vertex(  0,  2);
    s.vertex(  3,  0);
    s.vertex(  0, -2);
    s.vertex(  0, -1);
    s.endShape();
    
    x = random(0, height - 1);
    y = random(0, width - 1);
    size = random(8, 24);
    velocity = (24 - size) / 8;
  }

  void move() {
    x += velocity;
    
    if (x > width + size * 4) {
      make();
      x = - size * 3;
    }
  }
  
  void display() {
    pushMatrix();
    translate(x, y);
    scale(size);
    shape(s);
    popMatrix();
  }
};

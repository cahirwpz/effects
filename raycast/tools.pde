void arrow(float x1, float y1, float z1, float x2, float y2, float z2) {
  beginShape(LINES);
  vertex(x1, y1, z1);
  vertex(x2, y2, z2);
  endShape();
}

void drawCameraPlane() { 
  float cameraY = height / 2.0;
  float fov = (width / 2.0) / float(width) * PI/2;
  float cameraZ = cameraY / tan(fov / 2.0);
  float aspect = float(width) / float(height);

  pushMatrix();
  perspective(fov, aspect, cameraZ / 10.0, cameraZ * 10.0);
  translate(width / 2, height / 2, -2000);
  
  rotateX(TWO_PI * yaw);
  rotateY(TWO_PI * pitch);
  rotateZ(TWO_PI * roll);

  blendMode(ADD);
  stroke(255, 255, 255);
  fill(128, 128, 128);
 
  beginShape(QUADS);
  vertex(- width / 2, - height / 2);
  vertex(width / 2, - height / 2);
  vertex(width / 2, height / 2);
  vertex(- width / 2, height / 2);
  endShape();

  stroke(255, 0, 0);
  arrow(0, 0, 0, width / 2, 0, 0);
  stroke(0, 255, 0);
  arrow(0, 0, 0, 0, height / 2, 0);
  stroke(0, 0, 255);
  arrow(0, 0, 0, 0, 0, height / 2);
  
  popMatrix();
}

/*
 * Shamelessly copied from:
 * https://bel.fi/alankila/rotzoomer.html
 */

int colors[] = { 192, 64, 128 };

void putRandomPixel(int x, int y) {
  for (int i = 0; i < 3; i ++) {
    colors[i] += int(random(-6, 6));
    colors[i] &= 0xff;
  }
  set(x, y, color(colors[0], colors[1], colors[2]));
}

final int pieceSize = 32;
final boolean dither = true;
final int rotation = -1;
final int zoom = -1;
final int centerX = 286;
final int centerY = 302;
int pos;

void setup() {
  frameRate(25);
  size(512, 512);
  background(0);
}

void draw() {
  /* XXX work out how to compensate for the bias.
   * the bias is really a function of where the stationary center
   * of the image is combined with the average properties of the
   * center shuffling mechanism. The center is defined as the point
   * which is moved as little as possible by the deformations. */
  int r = (rotation < 0) ? 16 : 0;

  for (int y = 0; y < 2; y++)
    for (int x = 0; x < 2; x++)
      putRandomPixel(centerX + x - r, centerY + y - r);

  /* shuffle image center around deterministically */
  pos++;
  int shift = 0;
  if (dither) {
    for (int i = 0; i < 5; i ++) {
      shift <<= 1;
      shift |= (pos >> i) & 1;
    }
    /* inject some long-term variation */
    shift = shift ^ (pos >> 5) & 0x1f;
  }

  /* now move graphics around. */
  PImage buffer = get();

  for (int y = 0; y < 17; y++) {
    for (int x = 0; x < 17; x++) {
      PImage tile = buffer.get(
           x * pieceSize + shift + (16 - y) * rotation + x * zoom,
           y * pieceSize + shift + x * rotation - (16 - y) * zoom,
           pieceSize, pieceSize);
      image(tile,
            x * pieceSize + shift + 8 * rotation + 8 * zoom,
            y * pieceSize + shift + 8 * rotation - 8 * zoom);
    }
  }
}

void keyPressed() {
  int r = (rotation < 0) ? 16 : 0;
  fill(color(random(255), random(255), random(255)));
  rect(centerX - 32 - r, centerY - 32 - r, 64, 64);
}

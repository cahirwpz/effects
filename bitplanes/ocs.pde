// This file contains an emulator of very basic features of
// Amiga Original Chipset display, with some extra tools.

class Bitplane {
  // Display class won't respect the real size of a bitplane,
  // it will treat it as raw data. By contrast, drawing routines
  // make use of size embedded into Bitplane class.
  int width;
  int height;
  // each word stores only 16 bits
  int data[];  

  Bitplane(int _width, int _height) {
    assert _width % 16 == 0; // must be multiple of 16

    width = _width;
    height = _height;

    data = new int[width * height / 16];
  }

  int bit(int x) {
    return 1 << (x & 15);
  }

  int pos(int x, int y) {
    return ((y * width) >> 4) + (x >> 4);
  }

  boolean get(int x, int y) {
    return boolean(data[pos(x, y)] & bit(x));
  }

  void set(int x, int y, boolean value) {
    if (value) bset(x, y);
    else bclr(x, y);
  }

  void bset(int x, int y) {
    data[pos(x, y)] |= bit(x);
  }

  void bclr(int x, int y) {
    data[pos(x, y)] &= ~bit(x);
  }

  void bxor(int x, int y) {
    data[pos(x, y)] ^= bit(x);
  }

  void clear() {
    for (int i = 0; i < data.length; i++)
      data[i] = 0;
  }

  void ones() {
    for (int i = 0; i < data.length; i++)
      data[i] = ~0;
  }
  
  void copy(Bitplane src, int x, int y) {
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        set(x + i, y + j, src.get(i, j));
      }
    }
  }

  void and(Bitplane src) {
    for (int i = 0; i < data.length; i++)
      data[i] &= src.data[i];
  }

  void and(Bitplane src, int x, int y) {
    for (int j = 0; j < src.height; j++)
      for (int i = 0; i < src.width; i++)
        set(x + i, y + j, get(x + i, y + j) & src.get(i, j)); 
  }

  void or(Bitplane src) {
    for (int i = 0; i < data.length; i++)
      data[i] |= src.data[i];
  }
  
  void or(Bitplane src, int x, int y) {
    for (int j = 0; j < src.height; j++)
      for (int i = 0; i < src.width; i++)
        set(x + i, y + j, get(x + i, y + j) | src.get(i, j));
  }

  void not() {
    for (int i = 0; i < data.length; i++)
      data[i] = ~data[i];
  }

  void add(Bitplane src, int x, int y, Bitplane carry) {
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        boolean a = get(x + i, y + j);
        boolean b = src.get(i, j);
        boolean d1 = (a & !b) | (!a & b);
        boolean d2 = a & b;
        set(x + i, y + j, d1);
        carry.set(i, j, d2);
      }
    }
  }

  void addx(Bitplane src, Bitplane carry_in, int x, int y, Bitplane carry_out) {
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        boolean a = get(x + i, y + j);
        boolean b = src.get(i, j);
        boolean c = carry_in.get(i, j);
        boolean d1 = (!a & !b & c) | (!a & b & !c) | (a & !b & !c) | (a & b & c);
        boolean d2 = (!a & b & c) | (a & !b & c) | (a & b & !c) | (a & b & c);
        set(x + i, y + j, d1);
        carry_out.set(i, j, d2);
      }
    }
  }

  void sub(Bitplane src, Bitplane borrow) {
    for (int i = 0; i < data.length; i++) {
      int a = data[i];
      int b = src.data[i];
      data[i] = (a & ~b) | (~a & b);
      borrow.data[i] = ~a & b;
    }
  }
  
  void sub(Bitplane src, int x, int y, Bitplane borrow) {
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        boolean a = get(x + i, y + j);
        boolean b = src.get(i, j);
        boolean d1 = (a & !b) | (!a & b);
        boolean d2 = !a & b;
        set(x + i, y + j, d1);
        borrow.set(i, j, d2);
      }
    }
  }

  void subx(Bitplane src, Bitplane borrow_in, int x, int y, Bitplane borrow_out) {
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        boolean a = get(x + i, y + j);
        boolean b = src.get(i, j);
        boolean c = borrow_in.get(i, j);
        boolean d1 = (!a & !b & c) | (!a & b & !c) | (a & !b & !c) | (a & b & c);
        boolean d2 = (!a & !b & c) | (!a & b & !c) | (a & b & c);
        set(x + i, y + j, d1);
        borrow_out.set(i, j, d2);
      }
    }
  }

  void rshift(int n) {
    assert n < 16;
    
    for (int j = 0; j < height; j++) {
      int i = width - 1;
      for (; i >= n; i--)
        set(i, j, get(i - n, j));
      for (; i >= 0; i--)
        bclr(i, j);
    }
  }
  
  void fill() {
    for (int y = 0; y < height; y++) {
      boolean p = false;
      for (int x = width - 1; x >= 0; x--) {
        boolean q = get(x, y);
        p ^= q;
        set(x, y, p);
      }
    }
  }

  void line(int xs, int ys, int xe, int ye) {
    if (ys > ye) {
      int xt = xs; xs = xe; xe = xt;
      int yt = ys; ys = ye; ye = yt;
    }

    int s = (xs < xe) ? 1 : -1;
    int dx = abs(xe - xs);
    int dy = ye - ys;
    int dg1 = 2 * dx;
    int dg2 = 2 * dy;

    if (dx < dy) {
      int dg = 2 * dx - dy;

      do {
        bset(xs, ys);

        if (dg > 0) {
          xs += s;
          dg += dg1 - dg2;
        } else {
          dg += dg1;
        }
        ys++;
      } while (--dy > 0);
    } else {
      int dg = 2 * dy - dx;

      do {
        bset(xs, ys);

        if (dg > 0) {
          ys++;
          dg += dg2 - dg1;
        } else {
          dg += dg2;
        }
        xs += s;
      } while (--dx > 0);
    }
  }

  void lineE(int x1, int y1, int x2, int y2) {
    if (y1 > y2) {
      int xt = x1; x1 = x2; x2 = xt;
      int yt = y1; y1 = y2; y2 = yt;
    }

    int dx = x2 - x1;
    int dy = y2 - y1;

    if (dy == 0)
      return;

    int di = dx / dy;
    int df = abs(dx) % dy;
    int xi = x1;
    int xf = 0;
    int s = (dx >= 0) ? 1 : -1;

    while (y1 < y2) {
      bxor(xi, y1++);
      xi += di;
      xf += df;
      if (xf > dy) {
        xf -= dy;
        xi += s;
      }
    }
  }

  void circle(int x0, int y0, int r) {
    int x = -r;
    int y = 0;
    int err = 2 * (1 - r);

    do {
      bxor(x0 - x, y0 + y);
      bxor(x0 - y, y0 - x);
      bxor(x0 + x, y0 - y);
      bxor(x0 + y, y0 + x);

      if (err <= y) {
        y++;
        err += y * 2 + 1;
      }
      if (err > x) {
        x++;
        err += x * 2 + 1;
      }
    } while (x < 0);
  }

  void circleE(int x0, int y0, int r) {
    int x = -r;
    int y = 0;
    int err = 2 * (1 - r);

    do {
      if (err <= y) {
        bxor(x0 - x, y0 - y);
        bxor(x0 + x, y0 - y);
        if (y != 0) {
          bxor(x0 + x, y0 + y);
          bxor(x0 - x, y0 + y);
        }
        y++;
        err += 2 * y + 1;
      }
      if (err > x) {
        x++;
        err += 2 * x + 1;
      }
    } while (x < 0);
  }

  /* Generating Conic Sections Using an Efficient Algorithms
   * by Abdul-Aziz Solyman Khalil */
  void ellipse(int xc, int yc, int rx, int ry) {
    int rx2, ry2, tworx2, twory2, x_slop, y_slop;
    int d, mida, midb;
    int x, y;

    x = 0; 
    y = ry;
    rx2 = rx * rx; 
    ry2 = ry * ry;
    tworx2 = 2 * rx2; 
    twory2 = 2 * ry2;
    x_slop = 2 * twory2; 
    y_slop = 2 * tworx2 * (y - 1);
    mida = rx2 / 2;
    midb = ry2 / 2;
    d = twory2 - rx2 - y_slop / 2 - mida;

    while (d <= y_slop) {
      bset(xc + x, yc + y);
      bset(xc - x, yc + y);
      bset(xc + x, yc - y);
      bset(xc - x, yc - y);
      if (d > 0) {
        d -= y_slop;
        y--;
        y_slop -= 2 * tworx2;
      }
      d += twory2 + x_slop;
      x++;
      x_slop += 2 * twory2;
    }

    d -= (x_slop + y_slop) / 2 + (ry2 - rx2) + (mida - midb);

    while (y >= 0) {
      bset(xc + x, yc + y);
      bset(xc - x, yc + y);
      bset(xc + x, yc - y);
      bset(xc - x, yc - y);

      if (d <= 0) {
        d += x_slop;
        x++;
        x_slop += 2 * twory2;
      }
      d += tworx2 - y_slop;
      y--;
      y_slop -= 2 * tworx2;
    }
  }

  void hyperbola(int xc, int yc, int rx, int ry, int bound) {
    int x, y, d, mida, midb;
    int tworx2, twory2, rx2, ry2;
    int x_slop, y_slop;

    x = rx; 
    y = 0;
    rx2 = rx * rx; 
    ry2 = ry * ry;
    tworx2 = 2 * rx2; 
    twory2 = 2 * ry2;
    x_slop = 2 * twory2 * ( x + 1 );
    y_slop = 2 * tworx2;
    mida = x_slop / 2; 
    midb = y_slop / 2;
    d = tworx2 - ry2 * (1 + 2 * rx) + midb;

    while ((d < x_slop) && (y <= bound)) {
      bset(xc + x, yc + y);
      bset(xc + x, yc - y);
      bset(xc - x, yc + y);
      bset(xc - x, yc - y);
      if (d >= 0) {
        d -= x_slop;
        x++;
        x_slop += 2 * tworx2;
      }
      d += tworx2 + y_slop;
      y++;
      y_slop += 2 * tworx2;
    }

    d -= (x_slop + y_slop) / 2 + (rx2 + ry2) - (mida + midb);

    if (rx > ry) {
      while (y <= bound) {
        bset(xc + x, yc + y);
        if (d <= 0) {
          d += y_slop;
          y++;
          y += 2 * tworx2;
        }
        d -= twory2 - x_slop;
        x++;
        x_slop += 2 * twory2;
      }
    }
  }

  void parabola(int x0, int y0, int p, int bound) {
    int p2 = 2 * p;
    int p4 = 2 * p2;
    int x = 0;
    int y = 0;
    int d = 1 - p;

    while ( (y < p) && (x <= bound)) {
      bset(x0 + x, y0 - y);
      bset(x0 + x, y0 + y);
      if (d >= 0) {
        x++;
        d -= p2;
      }
      y++;
      d += 2 * y + 1;
    }

    d = 1 - (d == 1 ? p4 : p2);

    while (x <= bound) {
      bset(x0 + x, y0 - y);
      bset(x0 + x, y0 + y);
      if (d <= 0) {
        y++;
        d += 4 * y;
      }
      x++;
      d -= p4;
    }
  }
};

// Limited representation of copper instruction.
class CopIns {
  int n;
  color c;

  CopIns(int _n, color _c) {
    n = _n;
    c = _c;
  }
};

// Limits color space to OCS 12-bit RGB.
color rgb12(color c) {
  int r = (c & 0xf00000) >> 20;
  int g = (c & 0x00f000) >> 12;
  int b = (c & 0x0000f0) >> 4;
  return color((r << 4) | r, (g << 4) | g, (b << 4) | b);
}

Bitplane bpl[];
// Number of bitplanes.
int depth;
// Original chipset allows to set up 32 colors. In EHB-mode,
// Amiga can display 64 colors, where second half is at half
// the brightness of first half.
color palette[];
// Amiga Copper can perform many different actions every 8 pixels.
// We are interested only in changing one of 32 colors.
CopIns copperList[]; 

void initOCS(int _depth) {
  depth = _depth;
  bpl = new Bitplane[depth];
  for (int i = 0; i < depth; i++)
    bpl[i] = new Bitplane(width, height);
  palette = new color[32];
  copperList = new CopIns[(width / 8) * height];
}

void copperClear() {
  for (int i = 0; i < copperList.length; i++)
    copperList[i] = null;
}

void copper(int x, int y, int n, int c) {
  assert (x & 7) == 0;
  assert x >= 0 && x < width;
  assert y >= 0 && y < height;
  copperList[(x / 8) + (y * width / 8)] = new CopIns(n & 0x1f, c);
}

void updateOCS() {
  // Copy colors to working palette that will change while
  // rendering view port.
  color _palette[] = new color[32];
  for (int i = 0; i < 32; i++)
    _palette[i] = rgb12(palette[i]);

  for (int y = 0, s = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
      if ((x & 7) == 0) {
        CopIns slot = copperList[s];
        if (slot != null)
          _palette[slot.n] = rgb12(slot.c);
        s++;
      }

      int i = 0;

      for (int d = 0; d < depth; d++)
        i |= int(bpl[d].get(x, y)) << d;

      set(x, y, _palette[i]);
    }
  }

  updatePixels();
}


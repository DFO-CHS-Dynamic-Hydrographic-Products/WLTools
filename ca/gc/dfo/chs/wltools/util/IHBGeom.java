package ca.gc.dfo.chs.wltools.util;

public interface IHBGeom {

  int LON_IDX= 0;
  int LAT_IDX= 1;

  double UNDEFINED_COORD= -77777.0;

  // --- For indexing b. boxes corners coordnates
  //     in HashMap objects.
  enum BBoxCornersId {
    SOUTH_WEST,
    NORTH_WEST,
    NORTH_EAST,
    SOUTH_EAST
  }
}

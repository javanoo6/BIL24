package test;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class SeatInfo {
  private long id;
  private String sector;
  private String sectorName;
  private String row;
  private String rowName;
  private String seat;
  private String seatName;
  private final String fullSector;
  private final String fullRow;
  private final String fullSeat;

  public SeatInfo(long id, String sector, String sectorName, String row,
                  String rowName, String seat, String seatName) {
    this.id = id;
    this.sector = sector;
    this.sectorName = sectorName;
    this.row = row;
    this.rowName = rowName;
    this.seat = seat;
    this.seatName = seatName;
    this.fullSector = calculateFullSector();
    this.fullRow = calculateFullRow();
    this.fullSeat = calculateFullSeat();
  }

  private String calculateFullSector() {
    return Optional.ofNullable(sector)
            .flatMap(s -> Optional.ofNullable(sectorName)
                    .filter(name -> !name.isEmpty())
                    .map(name -> "сектор".equalsIgnoreCase(s) ? name : s + " " + name))
            .orElse(Optional.ofNullable(sector).orElse(""));
  }

  private String calculateFullRow() {
    return Optional.ofNullable(row)
            .flatMap(r -> Optional.ofNullable(rowName).map(name -> r + " " + name))
            .orElse("");
  }

  private String calculateFullSeat() {
    return Optional.ofNullable(seat)
            .flatMap(s -> Optional.ofNullable(seatName).map(name -> s + " " + name))
            .orElse("");
  }
}

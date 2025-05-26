package test;

import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Не нужно будет городить конструктор с исключением
@UtilityClass
public final class SeatParser {
  // Предкомпилированный паттерн - thread-safe и переиспользуемый
  private static final Pattern SEAT_PATTERN;
  /*Маппинг кириллических символов на латинские для быстрого поиска
  Использование параллельных массивов для O(1) поиска вместо HashMap
  O(n) в данном случае будет быстрее О(1) из-за малого кол-ва эл-ов*/
  private static final char[] CYRILLIC_CHARS;
  private static final char[] LATIN_CHARS;

  static {
    SEAT_PATTERN = Pattern.compile(
        "(?:(?<sector>.*?)\\s+(?<sectorName>(?:\\p{Lu}|\\d).*?)|(?<sectorWithoutName>.*))\\s+(?<row>Ряд)\\s+(?<rowName>.*?)\\s+(?<seat>Место)\\s+(?<seatName>.*)"
    );
    CYRILLIC_CHARS = new char[]{'С', 'Е', 'Т', 'Н', 'У', 'О', 'Р', 'Х', 'А', 'В', 'К', 'М'};
    LATIN_CHARS = new char[]{'C', 'E', 'T', 'H', 'Y', 'O', 'P', 'X', 'A', 'B', 'K', 'M'};
  }

  /**
   * Парсит информацию о месте из отформатированной строки.
   */
  public static Optional<SeatInfo> parse(String seatName, long id) {
    return Optional.ofNullable(seatName)
        .filter(name -> !name.trim().isEmpty())
        .map(SEAT_PATTERN::matcher)
        .filter(Matcher::find)
        .flatMap(matcher -> extractInfo(matcher, id));
  }

  /**
   * Извлечение информации о месте из совпавшего regex паттерна.
   */
  private static Optional<SeatInfo> extractInfo(Matcher matcher, long id) {
    var sector = Optional.ofNullable(matcher.group("sectorWithoutName"))
        .orElse(matcher.group("sector"));
    var sectorName = Optional.ofNullable(matcher.group("sectorName"))
        .map(SeatParser::convertCyrillicChars).orElse("");
    var row = matcher.group("row");
    var rowName = matcher.group("rowName");
    var seat = matcher.group("seat");
    var seatNameValue = matcher.group("seatName");

    if (Stream.of(sector, row, rowName, seat, seatNameValue).anyMatch(Objects::isNull)) {
      return Optional.empty();
    }

    return Optional.of(new SeatInfo(id, sector, sectorName, row, rowName, seat, seatNameValue));
  }

  /**
   * Заменяет кириллические символы на латинские эквиваленты.
   * Использует манипуляции с массивом символов вместо множественных замен строк.
   * Временная сложность: O(n) где n - длина строки
   */
  private static String convertCyrillicChars(String input) {
    return Optional.ofNullable(input)
        .filter(s -> !s.isEmpty())
        .map(SeatParser::performCyrillicReplacement)
        .orElse(input);
  }

  /**
   * Выполняет фактическую логику замены символов.
   */
  private static String performCyrillicReplacement(String input) {
    var chars = input.toCharArray();
    var isModified = false;

    for (int i = 0; i < chars.length; i++) {
      var current = chars[i];
      for (int j = 0; j < CYRILLIC_CHARS.length; j++) {
        if (current == CYRILLIC_CHARS[j]) {
          chars[i] = LATIN_CHARS[j];
          isModified = true;
          break;
        }
      }
    }

    return isModified ? new String(chars) : input;
  }
}
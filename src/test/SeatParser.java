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
    // Компилировать регулярку при каждом вызове метода-неэффективно
    private static final Pattern SEAT_PATTERN;
    // O(n) в данном случае будет быстрее О(1) из-за малого кол-ва эл-ов
    private static final char[] CYRILLIC_CHARS;
    private static final char[] LATIN_CHARS;
    // При таком объявлении, мне кажется будет чуть читабельнее
    static {
        SEAT_PATTERN = Pattern.compile(
                "(?:(?<sector>.*?)\\s+(?<sectorName>(?:\\p{Lu}|\\d).*?)|(?<sectorWithoutName>.*))\\s+(?<row>Ряд)\\s+(?<rowName>.*?)\\s+(?<seat>Место)\\s+(?<seatName>.*)"
        );
        CYRILLIC_CHARS = new char[]{'С', 'Е', 'Т', 'Н', 'У', 'О', 'Р', 'Х', 'А', 'В', 'К', 'М'};
        LATIN_CHARS = new char[]{'C', 'E', 'T', 'H', 'Y', 'O', 'P', 'X', 'A', 'B', 'K', 'M'};
    }
    // Явно возвращать null не стоит, лучше обработать дальше Optional.ofNullable в методе вызвавший `parseSeat`
    public static Optional<SeatInfo> parseSeat(String seatName, long id) {
        return Optional.ofNullable(seatName)
                .filter(name -> !name.trim().isEmpty())
                .map(SEAT_PATTERN::matcher)
                .filter(Matcher::find)
                .flatMap(matcher -> extractSeatInfo(matcher, id));
    }

    private static Optional<SeatInfo> extractSeatInfo(Matcher matcher, long id) {
        var sector = Optional.ofNullable(matcher.group("sectorWithoutName")).orElse(matcher.group("sector"));
        var sectorName = Optional.ofNullable(matcher.group("sectorName")).map(SeatParser::replaceCyrillicChars).orElse("");
        var row = matcher.group("row");
        var rowName = matcher.group("rowName");
        var seat = matcher.group("seat");
        var seatNameValue = matcher.group("seatName");

        if (Stream.of(sector, row, rowName, seat, seatNameValue).anyMatch(Objects::isNull)) {
            return Optional.empty();
        }

        return Optional.of(new SeatInfo(id, sector, sectorName, row, rowName, seat, seatNameValue));
    }

    private static String replaceCyrillicChars(String input) {
        return Optional.ofNullable(input)
                .filter(s -> !s.isEmpty())
                .map(SeatParser::performCyrillicReplacement)
                .orElse(input);
    }
    // String replace не эффективно, тк это O(n*m), где n - длина строки, m - кол-во замен https://stackoverflow.com/a/47209477/22503114
    // этот варик O(n)
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
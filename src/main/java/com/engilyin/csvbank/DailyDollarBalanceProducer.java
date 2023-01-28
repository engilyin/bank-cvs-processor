package com.engilyin.csvbank;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DailyDollarBalanceProducer {

    private final DateTimeFormatter dotDateFormat = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.US);

    public static void main(String[] args) {
        var processor = new DailyDollarBalanceProducer();

        processor.process();
    }

    @SneakyThrows
    private void process() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("csv");

        var operations = listOperations(resource);
        log.info("{} Operation found", operations.length);

        ExchangeRate[] rates = retrieveRates();
        log.info("{} Rates found", rates.length);

        generateDollarList(operations, rates);

    }

    private void generateDollarList(BalanceInfo[] operations, ExchangeRate[] rates) {

        int currentOperationIndex = 0;
        BalanceInfo currentOperation = operations[currentOperationIndex];

        double max = 0.0;
        LocalDate maxDate = null;

        for (ExchangeRate dayRate : rates) {
            var currentDate = LocalDate.parse(dayRate.getExchangedate(), dotDateFormat);
            if (currentDate.isAfter(currentOperation.getFrom())) {
                currentOperationIndex++;
                if (currentOperationIndex < operations.length) {
                    currentOperation = operations[currentOperationIndex];
                } else {
                    currentOperation = BalanceInfo.builder()
                            .from(LocalDate.parse("01.01.2023", dotDateFormat))
                            .in(currentOperation.getOut())
                            .build();
                }
                
                log.info("Operation: {}", currentOperation);

            }
            var currentUsd = currentOperation.getIn() / dayRate.getRate();

            log.info("Date: {} UAH: {} USD: {} Rate: {}", currentDate, currentOperation.getIn(),
                    String.format("%.2f", currentUsd), dayRate.getRate());

            if (max < currentUsd) {
                max = currentUsd;
                maxDate = currentDate;
            }

        }

        log.info("Max USD: {} at {}", String.format("%.2f", max), maxDate);

    }

    private ExchangeRate[] retrieveRates() {
        return new NBUExchangeRate().generate();
    }

    private BalanceInfo[] listOperations(URL resource) throws IOException, URISyntaxException {
        return Files.walk(Paths.get(resource.toURI()))
                .filter(Files::isRegularFile)
                .flatMap(this::lines)
                .map(line -> line.split(";"))
                .map(f -> BalanceInfo.builder()
                        .from(parseDate(f[0]))
                        .to(parseDate(f[1]))
                        .in(Double.parseDouble(f[5].replace(',', '.')))
                        .out(Double.parseDouble(f[9].replace(',', '.')))
                        .build())
                .sorted()
                .toArray(BalanceInfo[]::new);
    }

    @SneakyThrows
    private Stream<String> lines(Path file) {
        return Files.lines(file).skip(1);
    }

    private LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, dotDateFormat);
    }
}

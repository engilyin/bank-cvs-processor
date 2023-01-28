package com.engilyin.csvbank;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class NBUExchangeRate {
    
    private final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        var n = new NBUExchangeRate();

        n.generate();
    }

    public ExchangeRate[] generate() {
      
        
        LocalDate start = LocalDate.parse("2022-01-01", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US));
        LocalDate end = LocalDate.parse("2022-12-31", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US));
        
        ExchangeRate[] rates = retrieve(start, end);
        
//        Arrays.stream(rates).forEach(System.out::println);
//        
        

//        start.datesUntil(LocalDate.parse("2023-01-01", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US)))
//                .forEach(v -> retrieve(v));

        return rates;
    }
    
    @SneakyThrows
    private ExchangeRate[] retrieve(LocalDate start, LocalDate end) {
       //var url = String.format("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=%s&valcode=USD&json=true", forDay.toString().replace("-", ""));
       var url = String.format("https://bank.gov.ua/NBU_Exchange/exchange_site?start=%s&end=%s&valcode=usd&sort=exchangedate&order=asc&json", start.toString().replace("-", ""), end.toString().replace("-", ""));
       
       
       
       HttpRequest request = HttpRequest.newBuilder()
               .uri(new URI(url))
               .version(HttpClient.Version.HTTP_2)
               .GET()
               .build();
       
       HttpResponse<String> response = HttpClient.newHttpClient()
               .send(request, HttpResponse.BodyHandlers.ofString());
       
       return mapper.readValue(response.body(), ExchangeRate[].class);
    }

}

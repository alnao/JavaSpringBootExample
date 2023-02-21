package it.alnao.examples;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;

import static org.mockito.Mockito.*;

import java.io.IOException;

/**
 * https://github.com/cloudacademy/java-tdd-bitcoinconverter/blob/step3/src/test/java/com/cloudacademy/bitcoin/ExampleMicro20mockitoTest.java
 */
public class ExampleMicro20mockitoTest{
    private CloseableHttpClient client;
    private CloseableHttpResponse response;
    private StatusLine statusLine;
    private HttpEntity entity;
    private InputStream stream;

    @BeforeEach
    public void setUp() {
        client = mock(CloseableHttpClient.class);
        response = mock(CloseableHttpResponse.class);
        statusLine = mock(StatusLine.class);
        entity = mock(HttpEntity.class);

        stream = new ByteArrayInputStream("{\"time\": {\"updated\": \"Oct 15, 2020 22:55:00 UTC\",\"updatedISO\": \"2020-10-15T22:55:00+00:00\",\"updateduk\": \"Oct 15, 2020 at 23:55 BST\"},\"chartName\": \"Bitcoin\",\"bpi\": {\"USD\": {\"code\": \"USD\",\"symbol\": \"&#36;\",\"rate\": \"11,486.5341\",\"description\": \"United States Dollar\",\"rate_float\": 11486.5341},\"GBP\": {\"code\": \"GBP\",\"symbol\": \"&pound;\",\"rate\": \"8,900.8693\",\"description\": \"British Pound Sterling\",\"rate_float\": 8900.8693},\"EUR\": {\"code\": \"EUR\",\"symbol\": \"&euro;\",\"rate\": \"9,809.3278\",\"description\": \"Euro\",\"rate_float\": 9809.3278}}}".getBytes());
    }
    public void globalArrange() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(stream);
        when(client.execute(any(HttpGet.class))).thenReturn(response);
    }
    

    @Test
    public void getExchangeRate_USD_ReturnsUSDExchangeRate() throws IOException {
        //arrange
    	globalArrange();

        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.getExchangeRate("USD");

        //assert
        double expected = 11.486;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void getExchangeRate_GBP_ReturnsGBPExchangeRate() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.getExchangeRate("GBP");
    
        //assert
        double expected = 8.9;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void getExchangeRate_EUR_ReturnsEURExchangeRate() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.getExchangeRate("EUR");
    
        //assert
        double expected = 9.809;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_1BitCoinToUSD_ReturnsUSDDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("USD", 1);
    
        //assert
        double expected = 11.486;
        Assertions.assertEquals(expected, actual);
    }
    
    @Test
    public void convertBitcoins_2BitCoinToUSD_ReturnsUSDDollars() throws IOException {
        //arrange
    	globalArrange();

        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("USD", 2);
    
        //assert
        double expected = 22.972;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_0BitCoinToUSD_ReturnsZeroUSDDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("USD", 0);
    
        //assert
        double expected = 0;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_1BitCoinToGBP_ReturnsGBPDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("GBP", 1);
    
        //assert
        double expected = 8.9;
        Assertions.assertEquals(expected, actual);
    }
    
    @Test
    public void convertBitcoins_2BitCoinToGBP_ReturnsGBPDollars() throws IOException {
        //arrange
    	globalArrange();
    	
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("GBP", 2);
    
        //assert
        double expected = 17.8;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_0BitCoinToGBP_ReturnsZeroGBPDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("GBP", 0);
    
        //assert
        double expected = 0;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_1BitCoinToEUR_ReturnsEURDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("EUR", 1);
    
        //assert
        double expected = 9.809;
        Assertions.assertEquals(expected, actual);
    }
    
    @Test
    public void convertBitcoins_2BitCoinToEUR_ReturnsEURDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("EUR", 2);
    
        //assert
        double expected = 19.618;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void convertBitcoins_0BitCoinToEUR_ReturnsZeroEURDollars() throws IOException {
        //arrange
    	globalArrange();
    
        //act
        ExampleMicro20mockito ExampleMicro20mockito = new ExampleMicro20mockito(client);
        var actual = ExampleMicro20mockito.convertBitcoins("EUR", 0);
    
        //assert
        double expected = 0;
        Assertions.assertEquals(expected, actual);
    }
}
package it.alnao.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

//import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc //@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ExampleMicro20mockitoController.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ExampleMicro20mockitoApplication.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExampleMicro20mockitoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExampleMicro20mockitoService serviceMock;
    
    @BeforeEach
    public void setup() {
        Mockito.when(serviceMock.convertBitcoins("EUR", 1)  ).thenReturn(new Double(42.84));
    }
    @Test
    public void canRetrieveValue() throws Exception {
    	//arrange
    	String value="42.84";

        //act when
        MockHttpServletResponse response = mockMvc.perform(
                get("/api/valore?currency=EUR&coin=1")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        //assert // then
        assertThat(response.getStatus()).isEqualTo( 200 );
        assertThat(response.getContentAsString()).isEqualTo(
                value
        );
    }
	
}

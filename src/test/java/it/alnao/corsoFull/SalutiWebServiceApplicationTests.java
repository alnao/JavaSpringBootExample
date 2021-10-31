package it.alnao.corsoFull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import it.alnao.corsoFull.controller.SalutiWebController;

import org.springframework.http.MediaType;

@RunWith(SpringRunner.class)
@WebMvcTest(SalutiWebController.class)
//@SpringBootTest
class SalutiWebServiceApplicationTests {

	@Autowired
	private MockMvc mvc;
	
	@Test
	void A() throws Exception {
		mvc.perform( get("/api/saluti")//chiama a url 
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$")
					.value("Saluti da AlNao"))
			.andDo(print());//esito
	}
	@Test
	void B() throws Exception {
		mvc.perform( get("/api/saluti2/Alberto")//chiama a url 
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$")
					.value("Saluti a Alberto"))
			.andDo(print());//esito
	}
	
}
